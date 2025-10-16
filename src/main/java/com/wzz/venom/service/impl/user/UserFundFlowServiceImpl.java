package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wzz.venom.domain.dto.UserDTO;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.mapper.UserFundFlowMapper;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger log = LogManager.getLogger(UserFundFlowServiceImpl.class);
    @Autowired
    private UserFundFlowMapper userFundFlowMapper;

    @Autowired
    private UserService userService;

    // --- 定义常量以避免魔术值 ---
    /** 资金类型：充值 */
    private static final String FUND_TYPE_RECHARGE = "RECHARGE";
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

    /** [新增] 生效状态：未生效/待审核 */
    private static final int EFFECTIVE_STATUS_PENDING = 0;
    /** [新增] 生效状态：已生效 */
    private static final int EFFECTIVE_STATUS_ACTIVE = 1;

    /** [新增] 生效状态：拒绝 */
    private static final int EFFECTIVE_STATUS_REFUSE= 2;


    // --- 充值审核相关新方法 ---

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean requestRecharge(String userName, Double amount, String description) {
        BigDecimal transactionAmount = BigDecimal.valueOf(amount);
        if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(0, "充值金额必须为正数");
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(userName);
        User uu =  userService.selectByUserName(userDTO);
        if (uu==null){
            throw new BusinessException(0,"用户不存在");
        }

        // 构建新的资金流水实体
        UserFundFlow newFlow = new UserFundFlow();
        newFlow.setUserId(uu.getId());
        newFlow.setUserName(userName);
        newFlow.setAmount(transactionAmount);
        newFlow.setFundType(FUND_TYPE_RECHARGE);
        newFlow.setDescription(description);
        newFlow.setStatus(STATUS_PROCESSING); // 状态为"处理中"
        newFlow.setIsEffective(EFFECTIVE_STATUS_PENDING); // 核心：设置为"未生效"

        // 注意：此处不更新用户主余额，也不计算新余额，因为资金尚未实际到账
        // 插入待审核的流水记录
        return userFundFlowMapper.insert(newFlow) > 0;
    }



    @Override
    public List<UserFundFlow> getPendingRecharges() {
        return userFundFlowMapper.selectList(
                new QueryWrapper<UserFundFlow>()
                        .eq("fund_type", FUND_TYPE_RECHARGE)
                        .eq("is_effective", EFFECTIVE_STATUS_PENDING)
                        .orderByAsc("create_time")
        );
    }

    /**
     * 拒绝充值
     * @param flowId
     * @return
     */
    @Override
    public boolean refuseRecharge(Long flowId) {

        // 1. 查找待审核的充值记录
        UserFundFlow pendingRecharge = userFundFlowMapper.selectById(flowId);

        // 2. 校验记录的合法性
        if (pendingRecharge == null) {
            throw new BusinessException(0, "未找到指定的充值记录");
        }
        if (!FUND_TYPE_RECHARGE.equals(pendingRecharge.getFundType()) ||
                !Objects.equals(EFFECTIVE_STATUS_PENDING, pendingRecharge.getIsEffective())) {
            throw new BusinessException(0, "该记录不是一条待审核的充值申请");
        }

        // 3. 获取当前最新的【生效】余额，以计算新余额
//        UserFundFlow lastEffectiveFlow = userFundFlowMapper.selectOne(
//                new QueryWrapper<UserFundFlow>()
//                        .eq("user_name", pendingRecharge.getUserName())
//                        .eq("is_effective", EFFECTIVE_STATUS_ACTIVE) // 确保只基于生效记录计算
//                        .orderByDesc("id")
//                        .last("limit 1")
//        );
//        BigDecimal currentBalance = (lastEffectiveFlow != null) ? lastEffectiveFlow.getBalance() : BigDecimal.ZERO;
//        BigDecimal newBalance = currentBalance.add(pendingRecharge.getAmount());

        // 4. 更新这条记录，使其拒绝
        pendingRecharge.setIsEffective(EFFECTIVE_STATUS_REFUSE);
        pendingRecharge.setStatus(STATUS_SUCCESS);
//        pendingRecharge.setBalance(newBalance); // 记录下操作后的最终余额
        pendingRecharge.setDescription(pendingRecharge.getDescription() + " (审核拒绝)");

        // 5. 更新用户主表中的余额
//        boolean balanceSuccess = userService.updateByUserBalance(pendingRecharge.getUserName(), newBalance.doubleValue());
//        if (!balanceSuccess) {
//            throw new BusinessException(0, "更新用户主表余额失败，事务已回滚");
//        }

        // 6. 将更新后的流水记录持久化到数据库
        return userFundFlowMapper.updateById(pendingRecharge) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean approveRecharge(Long flowId) {
        // 1. 查找待审核的充值记录
        UserFundFlow pendingRecharge = userFundFlowMapper.selectById(flowId);

        // 2. 校验记录的合法性
        if (pendingRecharge == null) {
            throw new BusinessException(0, "未找到指定的充值记录");
        }
        if (!FUND_TYPE_RECHARGE.equals(pendingRecharge.getFundType()) ||
                !Objects.equals(EFFECTIVE_STATUS_PENDING, pendingRecharge.getIsEffective())) {
            throw new BusinessException(0, "该记录不是一条待审核的充值申请");
        }

        // 3. 获取当前最新的【生效】余额，以计算新余额
        UserFundFlow lastEffectiveFlow = userFundFlowMapper.selectOne(
                new QueryWrapper<UserFundFlow>()
                        .eq("user_name", pendingRecharge.getUserName())
                        .eq("is_effective", EFFECTIVE_STATUS_ACTIVE) // 确保只基于生效记录计算
                        .orderByDesc("id")
                        .last("limit 1")
        );
        BigDecimal currentBalance = (lastEffectiveFlow != null) ? lastEffectiveFlow.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(pendingRecharge.getAmount());

        // 4. 更新这条记录，使其生效
        pendingRecharge.setIsEffective(EFFECTIVE_STATUS_ACTIVE);
        pendingRecharge.setStatus(STATUS_SUCCESS);
        pendingRecharge.setBalance(newBalance); // 记录下操作后的最终余额
        pendingRecharge.setDescription(pendingRecharge.getDescription() + " (审核通过)");

        // 5. 更新用户主表中的余额
        boolean balanceSuccess = userService.updateByUserBalance(pendingRecharge.getUserName(), newBalance.doubleValue());
        if (!balanceSuccess) {
            throw new BusinessException(0, "更新用户主表余额失败，事务已回滚");
        }

        // 6. 将更新后的流水记录持久化到数据库
        return userFundFlowMapper.updateById(pendingRecharge) > 0;
    }


    // --- 现有方法修改 ---

    /**
     * 核心私有方法：新增一条【生效的】资金流水记录
     * [重要修改] 此方法现在只用于创建立即生效的流水，并且在计算余额时会过滤掉未生效的记录。
     */
    private boolean addFlowRecord(String userName, BigDecimal transactionAmount, String fundType, String description) {
        // 1. 获取用户最新的【已生效】资金流水，以确定当前余额
        UserFundFlow lastFlow = userFundFlowMapper.selectOne(
                new QueryWrapper<UserFundFlow>()
                        .eq("user_name", userName)
                        .eq("is_effective", EFFECTIVE_STATUS_ACTIVE) // 关键变更：只查询生效的流水
                        .orderByDesc("id")
                        .last("limit 1")
        );

        BigDecimal currentBalance = (lastFlow != null) ? lastFlow.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(transactionAmount);

        if (transactionAmount.compareTo(BigDecimal.ZERO) < 0 && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(0,"账户余额不足，操作失败！");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(userName);
        User uu =  userService.selectByUserName(userDTO);
        if (uu==null){
            throw new BusinessException(0,"用户不存在");
        }

        UserFundFlow newFlow = new UserFundFlow();
        newFlow.setUserName(userName);
        newFlow.setAmount(transactionAmount);
        newFlow.setBalance(newBalance);
        newFlow.setFundType(fundType);
        newFlow.setUserId(uu.getId());
        newFlow.setDescription(description);
        newFlow.setIsEffective(EFFECTIVE_STATUS_ACTIVE); // 默认设为已生效

        if (FUND_TYPE_WITHDRAW.equals(fundType)){
            newFlow.setStatus(STATUS_PROCESSING);
        } else if (FUND_TYPE_WITHDRAW_REFUND.equals(fundType)){
            newFlow.setStatus(STATUS_SUCCESS); // 提现拒绝返还应为成功状态
        } else {
            newFlow.setStatus(STATUS_SUCCESS);
        }

        boolean balanceSuccess = userService.updateByUserBalance(userName, newBalance.doubleValue());
        if (!balanceSuccess) {
            throw new BusinessException(0, "更新用户余额失败");
        }

        return userFundFlowMapper.insert(newFlow) > 0;
    }


    @Override
    @Transactional
    public boolean addUserFundFlow(UserFundFlow fundFlow) {
        return userFundFlowMapper.insert(fundFlow) > 0;
    }

    @Override
    @Transactional
    public boolean updateUserFundFlow(UserFundFlow fundFlow) {
        if (fundFlow.getId() == null) {
            return false;
        }
        return userFundFlowMapper.updateById(fundFlow) > 0;
    }

    @Override
    public List<UserFundFlow> queryTheUserSFundFlowList(String user) {
        QueryWrapper<UserFundFlow> queryWrapper = new QueryWrapper<>();
        // 查询时，只向用户展示已生效或用户自己发起的记录
        queryWrapper.eq("user_name", user)
                .and(qw -> qw.eq("is_effective", EFFECTIVE_STATUS_ACTIVE).or().eq("fund_type", FUND_TYPE_RECHARGE))
                .orderByDesc("create_time");
        return userFundFlowMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserFundFlow> queryAllWithdrawalTransactionInformation() {
        QueryWrapper<UserFundFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("fund_type", FUND_TYPE_WITHDRAW, FUND_TYPE_WITHDRAW_REFUND)
                .orderByDesc("create_time");
        return userFundFlowMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询所有提现相关的交易信息 (包括提现和提现退款)
     * @param page 分页对象，包含 current (当前页) 和 size (每页数量)
     * @return 包含分页结果的 Page 对象
     */
    @Override
    public IPage<UserFundFlow> queryAllWithdrawalTransactionInformationByPage(IPage<UserFundFlow> page) {
        QueryWrapper<UserFundFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("fund_type", FUND_TYPE_WITHDRAW, FUND_TYPE_WITHDRAW_REFUND);
        queryWrapper.orderByDesc("create_time");

        return userFundFlowMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean modifyUserWithdrawalStatus(String user, Integer status) {
        UserFundFlow latestWithdrawal = userFundFlowMapper.selectOne(
                new QueryWrapper<UserFundFlow>()
                        .eq("user_name", user)
                        .eq("fund_type", FUND_TYPE_WITHDRAW)
                        .eq("status", STATUS_PROCESSING)
                        .orderByDesc("create_time")
                        .last("limit 1")
        );
        if (Objects.isNull(latestWithdrawal)) {
            return false;
        }

        if (WITHDRAW_STATUS_REJECTED == status) {
            refuseToWithdrawAndReturnBalance(user, latestWithdrawal.getAmount().abs().doubleValue());
        }

        latestWithdrawal.setStatus(status);
        return userFundFlowMapper.updateById(latestWithdrawal) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseUserTransactionAmount(String user, Double amount, String describe) {
        BigDecimal transactionAmount = BigDecimal.valueOf(amount);
        return addFlowRecord(user, transactionAmount, FUND_TYPE_INCOME, describe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reduceUserTransactionAmount(String user, Double amount, String describe) {
        BigDecimal transactionAmount = BigDecimal.valueOf(amount).negate();
        return addFlowRecord(user, transactionAmount, FUND_TYPE_EXPENSE, describe);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean reduceUserTransactionAmountWITHDRA(String user, Double amount, String describe) {
        BigDecimal transactionAmount = BigDecimal.valueOf(amount).negate();
        return addFlowRecord(user, transactionAmount, FUND_TYPE_WITHDRAW, describe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refuseToWithdrawAndReturnBalance(String user, Double amount) {
        BigDecimal returnAmount = BigDecimal.valueOf(amount);
        return addFlowRecord(user, returnAmount, FUND_TYPE_WITHDRAW_REFUND, "提现拒绝，资金返还");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyWithdrawalStatusById(Long flowId, Integer status) {
        UserFundFlow withdrawalFlow = userFundFlowMapper.selectById(flowId);

        if (withdrawalFlow == null) {
            throw new BusinessException(0, "未找到ID为 " + flowId + " 的提现记录！");
        }
        if (!FUND_TYPE_WITHDRAW.equals(withdrawalFlow.getFundType())) {
            throw new BusinessException(0, "该记录不是提现申请，无法操作！");
        }
        if (!Objects.equals(STATUS_PROCESSING, withdrawalFlow.getStatus())) {
            throw new BusinessException(0, "该提现申请已被处理，请勿重复操作！");
        }

        if (Objects.equals(WITHDRAW_STATUS_REJECTED, status)) {
            BigDecimal returnAmount = withdrawalFlow.getAmount().abs();
            String description = String.format("提现申请[ID:%d]被管理员拒绝，资金返还（充值）", flowId);
            addFlowRecord(withdrawalFlow.getUserName(), returnAmount, "RECHARGE", description);
            withdrawalFlow.setStatus(STATUS_FAILED);

        } else if (Objects.equals(WITHDRAW_STATUS_APPROVED, status)) {
            withdrawalFlow.setStatus(STATUS_SUCCESS);
        } else {
            throw new BusinessException(0, "提供了无效的目标状态值！");
        }

        return userFundFlowMapper.updateById(withdrawalFlow) > 0;
    }


}