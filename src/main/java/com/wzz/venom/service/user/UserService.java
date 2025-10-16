package com.wzz.venom.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wzz.venom.domain.dto.*;
import com.wzz.venom.domain.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    /**
     * 新增用户
     * @param user 用户实体
     * @return 是否成功
     */
    boolean addUser(User user);

    @Transactional(rollbackFor = Exception.class)
    boolean addUserByCode(User user, String code);

    /**
     * 更新用户信息
     * @param user 用户实体
     * @return 是否成功
     */
    boolean updateUserInformation(User user);

    /**
     * 查询全部用户
     * @return 用户列表
     */
    List<User> queryUserList();

    /**
     * 查询指定用户
     * @param userName 用户名
     * @return 用户信息
     */
    User queryUser(String userName);

    /**
     * 删除用户
     * @param userName 用户名
     * @return 是否成功
     */
    boolean deleteUser(String userName);

    /**
     * 验证登录密码
     * @param userName 用户名
     * @param password 登录密码
     * @return 是否匹配
     */
    boolean verifyUserPassword(String userName, String password);

    /**
     * 验证提现密码
     * @param userName 用户名
     * @param withdrawalPassword 提现密码
     * @return 是否匹配
     */
    boolean verifyUserWithdrawalPassword(String userName, String withdrawalPassword);

    /**
     * 修改账户余额
     * @param userName 用户名
     * @param amount 修改金额（正负值）
     * @return 是否成功
     */
    boolean modifyUserBalance(String userName, Double amount);

    Boolean updateByUserBalance(String userName, Double amount);

    /**
     * 修改信用分
     * @param userName 用户名
     * @param creditScore 新信用分
     * @return 是否成功
     */
    boolean modifyUserReputationScore(String userName, Integer creditScore);

    /**
     * 修改银行卡号
     * @param userName 用户名
     * @param bankCard 新银行卡号
     * @return 是否成功
     */
    boolean modifyTheUserSBankCard(String userName, String bankCard);

    /**
     * 修改登录密码
     * @param userName 用户名
     * @param password 新密码
     * @return 是否成功
     */
    boolean changeUserPassword(String userName, String password);

    /**
     * 修改提现密码
     * @param userName 用户名
     * @param withdrawalPassword 新提现密码
     * @return 是否成功
     */
    boolean changeUserWithdrawalPassword(String userName, String withdrawalPassword);

    User selectByUserName(UserDTO userDTO);

    User queryUserByUserId(Long id);

    @Transactional(rollbackFor = Exception.class)
    boolean addBankCardForUser(Long userId, String bankCard);

    @Transactional(rollbackFor = Exception.class)
    boolean addBankDetails(Long userId, String realName, String bankName, String bankBranch,String bankCard);

    @Transactional
    boolean toggleUserFreezeStatus(String userName, boolean freeze);
}
