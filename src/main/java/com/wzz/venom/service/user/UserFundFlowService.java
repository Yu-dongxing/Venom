package com.wzz.venom.service.user;

import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.domain.entity.UserFundFlow;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserFundFlowService {

    @Transactional
        // 新增操作，建议加上事务
    boolean addUserFinancialStatements(UserFundFlow statement);

    @Transactional // 更新操作，建议加上事务
    boolean updateUserFinancialStatements(UserFundFlow statement);


    List<UserFundFlow> queryTheDesignatedUserSFinancialStatementList(String user);

    /**
     * 删除指定编号的理财流水记录
     * @param id 流水ID
     * @return 是否成功
     */
    boolean deleteSpecifiedNumberInformation(Long id);

    /**
     * 增加用户交易金额（记录正向流水）
     * @param user 用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    boolean increaseUserUnderstandingOfTransactionAmount(String user, Double amount);

    /**
     * 减少用户交易金额（记录负向流水）
     * @param user 用户名
     * @param amount 减少金额
     * @return 是否成功
     */
    boolean reduceUserUnderstandingOfTransactionAmounts(String user, Double amount);
}
