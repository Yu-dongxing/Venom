package com.wzz.venom.service.user;

import com.wzz.venom.domain.dto.UserIncomeStatsDto;
import com.wzz.venom.domain.entity.UserFinancialStatement;

import java.util.List;

public interface UserFinancialStatementService {
    /**
     * 新增用户理财流水记录
     * @param statement 理财流水对象
     * @return 是否成功
     */
    boolean addUserFinancialStatements(UserFinancialStatement statement);

    /**
     * 更新用户理财流水记录
     * @param statement 理财流水对象
     * @return 是否成功
     */
    boolean updateUserFinancialStatements(UserFinancialStatement statement);

    /**
     * 查询指定用户的理财流水列表
     * @param user 用户名
     * @return 理财流水列表
     */
    List<UserFinancialStatement> queryTheDesignatedUserSFinancialStatementList(String user);

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

    UserIncomeStatsDto getIncomeStatistics(String userName);
}
