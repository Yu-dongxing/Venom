package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.mapper.UserFundFlowMapper;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 用户资金流水服务实现类
 *
 * @author (Your Name)
 */
@Service
public class UserFundFlowServiceImpl implements UserFundFlowService {

    @Autowired
    private UserFundFlowMapper userFundFlowMapper;

    @Autowired
    private UserService userService;

    // --- 定义常量以避免魔术值 ---
    /** 资金类型：提现 */
    private static final String FUND_TYPE_WITHDRAW = "WITHDRAW";
    /** 资金类型：提现失败退款 */
    private static final String FUND_TYPE_WITHDRAW_REFUND = "WITHDRAW_REFUND";
    /** 资金类型：通用收入 */
    private static final String FUND_TYPE_INCOME = "INCOME";
    /** 资金类型：通用支出 */
    private static final String FUND_TYPE_EXPENSE = "EXPENSE";

    /** 状态：成功 */
    private static final int STATUS_SUCCESS = 0;
    /** 状态：处理中 */
    private static final int STATUS_PROCESSING = 1;
    /** 状态：失败 */
    private static final int STATUS_FAILED = 2;
    /** 状态：提现申请通过 */
    private static final int WITHDRAW_STATUS_APPROVED = 0;
    /** 状态：提现申请拒绝 */
    private static final int WITHDRAW_STATUS_REJECTED = 2;


    @Override
    @Transactional
    public boolean addUserFundFlow(UserFundFlow fundFlow) {
        return userFundFlowMapper.insert(fundFlow) > 0;
    }

    @Override
    @Transactional
    public boolean updateUserFundFlow(UserFundFlow fundFlow) {
        // 确保传入的对象有主键ID
        if (fundFlow.getId() == null) {
            return false;
        }
        return userFundFlowMapper.updateById(fundFlow) > 0;
    }

    @Override
    public List<UserFundFlow> queryTheUserSFundFlowList(String user) {
        QueryWrapper<UserFundFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user)
                .orderByDesc("create_time"); // 按创建时间降序排序
        return userFundFlowMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserFundFlow> queryAllWithdrawalTransactionInformation() {
        QueryWrapper<UserFundFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("fund_type", FUND_TYPE_WITHDRAW,FUND_TYPE_WITHDRAW_REFUND)
                .orderByDesc("create_time");
        return userFundFlowMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean modifyUserWithdrawalStatus(String user, Integer status) {
        // 业务假设：修改用户最新一笔“处理中”的提现请求的状态
        UserFundFlow latestWithdrawal = userFundFlowMapper.selectOne(
                new QueryWrapper<UserFundFlow>()
                        .eq("user_name", user)
                        .eq("fund_type", FUND_TYPE_WITHDRAW)
                        .eq("status", STATUS_PROCESSING) // 查找“处理中”的记录
                        .orderByDesc("create_time")
                        .last("limit 1")
        );

        if (Objects.isNull(latestWithdrawal)) {
            // 没有找到符合条件的提现记录
            return false;
        }

        // 拒绝提现时，需要返还余额
        if (WITHDRAW_STATUS_REJECTED == status) {
            // 提现金额是负数，返还时应使用其绝对值
            refuseToWithdrawAndReturnBalance(user, latestWithdrawal.getAmount().abs().doubleValue());
        }

        latestWithdrawal.setStatus(status);
        return userFundFlowMapper.updateById(latestWithdrawal) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseUserTransactionAmount(String user, Double amount, String describe) {
        // 使用 BigDecimal 保证精度
        BigDecimal transactionAmount = BigDecimal.valueOf(amount);
        return addFlowRecord(user, transactionAmount, FUND_TYPE_INCOME, describe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reduceUserTransactionAmount(String user, Double amount, String describe) {
        // 减少金额，传入的 amount 为正数，内部处理为负
        BigDecimal transactionAmount = BigDecimal.valueOf(amount).negate();
        return addFlowRecord(user, transactionAmount, FUND_TYPE_EXPENSE, describe);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean reduceUserTransactionAmountWITHDRA(String user, Double amount, String describe) {
        // 减少金额，传入的 amount 为正数，内部处理为负
        BigDecimal transactionAmount = BigDecimal.valueOf(amount).negate();
        return addFlowRecord(user, transactionAmount, FUND_TYPE_WITHDRAW, describe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refuseToWithdrawAndReturnBalance(String user, Double amount) {
        // 返还余额，本质是增加一笔正向流水
        BigDecimal returnAmount = BigDecimal.valueOf(amount);
        return addFlowRecord(user, returnAmount, FUND_TYPE_WITHDRAW_REFUND, "提现拒绝，资金返还");
    }


    /**
     * 核心私有方法：新增一条资金流水记录
     * 封装了查询余额、计算新余额、检查余额、插入流水的原子操作
     *
     * @param userName 用户名
     * @param transactionAmount 交易金额（正为收入，负为支出）
     * @param fundType 资金类型
     * @param description 描述
     * @return 是否成功
     */
    private boolean addFlowRecord(String userName, BigDecimal transactionAmount, String fundType, String description) {
        // 1. 获取用户最新的资金流水，以确定当前余额
        // 使用 "order by id desc" 替代 "create_time" 在高并发下更可靠
        UserFundFlow lastFlow = userFundFlowMapper.selectOne(
                new QueryWrapper<UserFundFlow>()
                        .eq("user_name", userName)
                        .orderByDesc("id")
                        .last("limit 1")
        );

        // 2. 计算当前余额
        BigDecimal currentBalance = (lastFlow != null) ? lastFlow.getBalance() : BigDecimal.ZERO;

        // 3. 计算交易后新余额
        BigDecimal newBalance = currentBalance.add(transactionAmount);

        // 4. 如果是支出，检查余额是否充足
        if (transactionAmount.compareTo(BigDecimal.ZERO) < 0 && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            // 抛出异常，触发事务回滚
            throw new BusinessException(0,"账户余额不足，操作失败！");
        }

        // 5. 构建新的资金流水实体
        UserFundFlow newFlow = new UserFundFlow();
        newFlow.setUserName(userName);
        newFlow.setAmount(transactionAmount);
        newFlow.setBalance(newBalance); // 操作后的最终余额
        newFlow.setFundType(fundType);
        newFlow.setDescription(description);

        //如果资金类型为提现，那么状态设置为待处理
        if (FUND_TYPE_WITHDRAW.equals(fundType)){
            newFlow.setStatus(STATUS_PROCESSING);
        }else if (FUND_TYPE_WITHDRAW_REFUND.equals(fundType)){
            //如果资金类型为提现驳回，那么状态设置为失败
            newFlow.setStatus(STATUS_FAILED);
        }else {
            newFlow.setStatus(STATUS_SUCCESS);
        }

        // 6. 插入新的流水记录
        return userFundFlowMapper.insert(newFlow) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyWithdrawalStatusById(Long flowId, Integer status) {
        // 1. 根据ID查询提现记录
        UserFundFlow withdrawalFlow = userFundFlowMapper.selectById(flowId);

        // 2. 校验记录是否存在且合法
        if (withdrawalFlow == null) {
            throw new BusinessException(0, "未找到ID为 " + flowId + " 的提现记录！");
        }
        if (!FUND_TYPE_WITHDRAW.equals(withdrawalFlow.getFundType())) {
            throw new BusinessException(0, "该记录不是提现申请，无法操作！");
        }
        if (!Objects.equals(STATUS_PROCESSING, withdrawalFlow.getStatus())) {
            throw new BusinessException(0, "该提现申请已被处理，请勿重复操作！");
        }

        // 3. 根据新状态进行处理
        if (Objects.equals(WITHDRAW_STATUS_REJECTED, status)) {
            // 拒绝提现：需要返还冻结的余额
            // 提现金额是负数，返还时应使用其绝对值
            BigDecimal returnAmount = withdrawalFlow.getAmount().abs();
            String description = String.format("提现申请[ID:%d]被管理员拒绝，资金返还", flowId);

            // 调用核心方法增加一条“提现拒绝返还”类型的正向流水
            addFlowRecord(withdrawalFlow.getUserName(), returnAmount, FUND_TYPE_WITHDRAW_REFUND, description);

            // 更新原提现记录的状态为“失败”
            withdrawalFlow.setStatus(STATUS_FAILED);

        } else if (Objects.equals(WITHDRAW_STATUS_APPROVED, status)) {
            // 批准提现：仅更新状态为“成功”
            // 资金在申请时已经从余额中扣除，此处仅做状态变更，代表线下打款已完成
            withdrawalFlow.setStatus(STATUS_SUCCESS);

        } else {
            // 无效的状态值
            throw new BusinessException(0, "提供了无效的目标状态值！");
        }

        // 4. 更新数据库中的原始提现记录
        return userFundFlowMapper.updateById(withdrawalFlow) > 0;
    }
}