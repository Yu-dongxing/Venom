package com.wzz.venom.task;

import com.wzz.venom.domain.entity.UserFinancial;
import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.service.config.SysConfigService;
import com.wzz.venom.service.user.UserFinancialService;
import com.wzz.venom.service.user.UserFinancialStatementService;
import com.wzz.venom.service.user.UserFundFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日理财收益定时计算任务
 */
@Component
public class FinancialIncomeCalculationTask {

    private static final Logger log = LoggerFactory.getLogger(FinancialIncomeCalculationTask.class);

    // --- 定义常量 ---
    private static final String SYS_CONFIG_NAME = "sys_config";
    private static final String FINANCIAL_RATE_KEY = "financial_management";
    /** 理财流水类型：3-收益派发 */
    private static final int TRANSACTION_TYPE_INCOME = 3;


    private final SysConfigService sysConfigService;
    private final UserFinancialService userFinancialService;
    private final UserFundFlowService userFundFlowService;
    private final UserFinancialStatementService userFinancialStatementService;

    @Autowired
    public FinancialIncomeCalculationTask(SysConfigService sysConfigService, UserFinancialService userFinancialService, UserFundFlowService userFundFlowService, UserFinancialStatementService userFinancialStatementService) {
        this.sysConfigService = sysConfigService;
        this.userFinancialService = userFinancialService;
        this.userFundFlowService = userFundFlowService;
        this.userFinancialStatementService = userFinancialStatementService;
    }

    /**
     * 每日零点执行理财收益计算
     * Cron 表达式 "0 0 0 * * ?" 表示每天的 00:00:00 执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailyFinancialIncome() {
        log.info("【每日理财收益计算任务】开始执行...");

        // 1. 从系统配置中获取日收益率
        Object rateValue = sysConfigService.getConfigValueByNameAndKey(SYS_CONFIG_NAME, FINANCIAL_RATE_KEY);
        if (rateValue == null) {
            log.error("【每日理财收益计算任务】执行失败：未在 sys_config 表中找到名为 '{}' 的配置项。使用默认值 0.2", FINANCIAL_RATE_KEY);
            rateValue = new BigDecimal("0.2");
        }

        BigDecimal dailyRate;
        try {
            // 将配置值转换为BigDecimal，用于精确计算
            dailyRate = new BigDecimal(rateValue.toString());
        } catch (NumberFormatException e) {
            log.error("【每日理财收益计算任务】执行失败：理财收益率配置值 '{}' 不是有效的数字。", rateValue);
            return;
        }

        if (dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("【每日理财收益计算任务】理财收益率配置为非正数（{}），任务终止。", dailyRate);
            return;
        }

        log.info("【每日理财收益计算任务】获取到当日理财收益率: {}", dailyRate);

        // 2. 查询所有持有理财金额的用户 (amount > 0)
        List<UserFinancial> activeFinancials = userFinancialService.findAll().stream()
                .filter(uf -> uf.getAmount() != null && uf.getAmount().compareTo(BigDecimal.ZERO) > 0 && uf.getStatus() == 0) // 确保金额大于0且状态为持有中
                .collect(Collectors.toList());

        if (activeFinancials.isEmpty()) {
            log.info("【每日理财收益计算任务】没有找到持有理财产品的用户，任务结束。");
            return;
        }

        log.info("【每日理财收益计算任务】发现 {} 个需要计算收益的用户。", activeFinancials.size());

        // 3. 遍历用户，逐一计算并发放收益
        int successCount = 0;
        int failCount = 0;
        for (UserFinancial userFinancial : activeFinancials) {
            try {
                // 调用包含事务的方法处理单个用户
                processUserIncome(userFinancial, dailyRate);
                successCount++;
            } catch (Exception e) {
                // 捕获异常，防止单个用户失败导致整个任务中断
                log.error("【每日理财收益计算任务】为用户 '{}' 计算收益时发生错误: {}", userFinancial.getUserName(), e.getMessage(), e);
                failCount++;
            }
        }

        log.info("【每日理财收益计算任务】执行完毕。成功处理 {} 个用户，失败 {} 个。", successCount, failCount);
    }


    /**
     * 处理单个用户的收益计算和入账，此方法包含事务
     * @param userFinancial 用户的理财信息
     * @param dailyRate     当日收益率
     */
    @Transactional(rollbackFor = Exception.class)
    public void processUserIncome(UserFinancial userFinancial, BigDecimal dailyRate) {
        String userName = userFinancial.getUserName();
        BigDecimal principal = userFinancial.getAmount(); // 理财本金

        // 4. 计算收益金额，结果保留两位小数，四舍五入
        BigDecimal earnings = principal.multiply(dailyRate).setScale(2, RoundingMode.HALF_UP);

        // 如果计算出的收益小于等于0.00，则无需记录
        if (earnings.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("用户 '{}' 的理财本金 {} 过低，计算收益为 {}，跳过本次派发。", userName, principal, earnings);
            return;
        }

        log.info("为用户 '{}' 计算理财收益：本金 {}, 收益率 {}, 收益 {}", userName, principal, dailyRate, earnings);

        // 5. 将收益记录到用户资金流水（主账户余额）
        // 注意：这里需要确保 UserFundFlowService 的方法接受 BigDecimal 或 Double
        boolean fundFlowSuccess = userFundFlowService.increaseUserTransactionAmount(userName, earnings.doubleValue(), "每日理财收益");
        if (!fundFlowSuccess) {
            // 抛出异常，触发事务回滚
            throw new RuntimeException("增加用户资金流水失败！");
        }

        // 6. 创建一条新的理财流水记录，用于对账
        UserFinancialStatement statement = new UserFinancialStatement();
        statement.setUserName(userName);
        statement.setFinancialId(userFinancial.getId()); // 关联理财主记录ID
        statement.setTransactionType(TRANSACTION_TYPE_INCOME); // 类型：收益派发
        statement.setAmount(earnings); // 金额：本次收益

        boolean statementSuccess = userFinancialStatementService.addUserFinancialStatements(statement);
        if (!statementSuccess) {
            // 抛出异常，触发事务回滚
            throw new RuntimeException("新增用户理财流水记录失败！");
        }
    }
}