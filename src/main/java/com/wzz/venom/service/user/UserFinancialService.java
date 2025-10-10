package com.wzz.venom.service.user;

import com.wzz.venom.domain.entity.UserFinancial;

import java.util.List;

public interface UserFinancialService {
    /**
     * 新增用户理财信息
     * @param financial 理财信息对象
     * @return 是否成功
     */
    boolean addUserFinancialInformation(UserFinancial financial);

    /**
     * 更新用户理财信息
     * @param financial 理财信息对象
     * @return 是否成功
     */
    boolean updateUserFinancialInformation(UserFinancial financial);

    /**
     * 查询用户理财信息（按用户与金额）
     * @param user 用户名
     * @param amount 理财金额
     * @return 匹配的理财记录
     */
    List<UserFinancial> searchForUserFinancialInformation(String user, Double amount);

    /**
     * 扣减用户理财余额
     * @param user 用户名
     * @param amount 扣减金额
     * @return 是否成功
     */
    boolean reduceUserFinancialBalance(String user, Double amount);

    /**
     * 查询指定用户的全部理财信息
     * @param user 用户名
     * @return 理财信息列表
     */
    List<UserFinancial> queryTheDesignatedUserSFinancialInformation(String user);

    /**
     * 删除指定用户理财信息
     * @param user 用户名
     * @return 是否成功
     */
    boolean deleteDesignatedUserSFinancialInformation(String user);

    /**
     * 增加用户理财余额
     * @param user 用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    boolean increaseUserFinancialBalance(String user, Double amount);

    /**
     * [新增] 查询所有用户理财信息 (供管理端使用)
     * @return 所有理财信息列表
     */
    List<UserFinancial> findAll();

    /**
     * [新增] 根据ID删除指定理财信息 (供管理端使用)
     * @param id 理财记录ID
     * @return 是否成功
     */
    boolean deleteFinancialInformationById(Long id);

    List<UserFinancial> queryTheDesignatedUserSFinancialInformationByuserId(Long userId, String currentUser);
}
