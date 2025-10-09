package com.wzz.venom.service.user;

import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.domain.entity.UserFundFlow;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 用户资金流水（账本）服务接口
 */
public interface UserFundFlowService {

    /**
     * 新增用户资金流水记录
     * @param fundFlow 资金流水对象
     * @return 是否成功
     */
    boolean addUserFundFlow(UserFundFlow fundFlow);

    /**
     * 更新用户资金流水记录
     * @param fundFlow 资金流水对象
     * @return 是否成功
     */
    boolean updateUserFundFlow(UserFundFlow fundFlow);

    /**
     * 查询指定用户的资金流水列表
     * @param user 用户名
     * @return 资金流水列表
     */
    List<UserFundFlow> queryTheUserSFundFlowList(String user);

    /**
     * 查询所有提现相关流水记录
     * @return 提现记录列表
     */
    List<UserFundFlow> queryAllWithdrawalTransactionInformation();

    /**
     * 修改用户提现状态
     * @param user 用户名
     * @param status 状态（1申请，2通过，3拒绝）
     * @return 是否成功
     */
    boolean modifyUserWithdrawalStatus(String user, Integer status);

    /**
     * 增加用户交易金额（生成正向资金流水）
     * @param user 用户名
     * @param amount 金额
     * @param describe 描述
     * @return 是否成功
     */
    boolean increaseUserTransactionAmount(String user, Double amount, String describe);

    /**
     * 减少用户交易金额（生成负向资金流水）
     * @param user 用户名
     * @param amount 金额
     * @param describe 描述
     * @return 是否成功
     */
    boolean reduceUserTransactionAmount(String user, Double amount, String describe);

    /**
     * 提现拒绝并返还余额
     * @param user 用户名
     * @param amount 金额
     * @return 是否成功
     */
    boolean refuseToWithdrawAndReturnBalance(String user, Double amount);
}