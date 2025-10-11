package com.wzz.venom.service.impl.user;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wzz.venom.domain.dto.UserDTO;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.enums.UserStatusEnum;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.mapper.UserMapper;
import com.wzz.venom.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 新增用户
     * @param user 用户实体
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(User user) {
        // 校验用户名是否已存在
        User existingUser = this.queryUser(user.getUserName());
        if (Objects.nonNull(existingUser)) {
            throw new BusinessException(0,"用户名 '" + user.getUserName() + "' 已存在");
        }
        user.setBalance(BigDecimal.valueOf(0.0));
        user.setCreditScore(100); // 默认信用分
        return userMapper.insert(user) > 0;
    }

    /**
     * 更新用户信息
     * @param user 用户实体
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInformation(User user) {
        if (user.getId() == null){
            throw new BusinessException(0,"用户ID不能为空");
        }
        Assert.notNull(user.getId(), "用户ID不能为空");
        User dbUser = userMapper.selectById(user.getId());

        if (dbUser == null){
            throw new BusinessException(0,"更新的用户不存在");
        }
        Assert.notNull(dbUser, "更新的用户不存在");
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setBankCard(user.getBankCard());
        updateUser.setUserName(user.getUserName());
        updateUser.setBalance(user.getBalance());
        updateUser.setWithdrawalPassword(user.getWithdrawalPassword());
        updateUser.setCreditScore(user.getCreditScore());
        return userMapper.updateById(updateUser) > 0;
    }

    /**
     * 查询全部用户
     * @return 用户列表
     */
    @Override
    public List<User> queryUserList() {
        return userMapper.selectList(null);
    }

    /**
     * 查询指定用户
     * @param userName 用户名
     * @return 用户信息
     */
    @Override
    public User queryUser(String userName) {
        if (userName==null){
            throw new BusinessException(0,"用户名不能为空");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, userName);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 删除用户（逻辑删除）
     * @param userName 用户名
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String userName) {
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }

        // 逻辑删除：将用户状态设置为注销
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserName, userName)
                .set(User::getAccountStatus, UserStatusEnum.CANCELLED);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 验证登录密码
     * @param userName 用户名
     * @param password 登录密码
     * @return 是否匹配
     */
    @Override
    public boolean verifyUserPassword(String userName, String password) {
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
        return Objects.equals(password, user.getPassword());
    }

    /**
     * 验证提现密码
     * @param userName 用户名
     * @param withdrawalPassword 提现密码
     * @return 是否匹配
     */
    @Override
    public boolean verifyUserWithdrawalPassword(String userName, String withdrawalPassword) {
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }

        // 在实际项目中，应使用加密匹配
        // return passwordEncoder.matches(withdrawalPassword, user.getWithdrawalPassword());
        return Objects.equals(withdrawalPassword, user.getWithdrawalPassword());
    }

    /**
     * 修改账户余额
     * @param userName 用户名
     * @param amount 修改金额（正为增加，负为减少）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modifyUserBalance(String userName, Double amount) {
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
//        Assert.notNull(user, "用户 '{}' 不存在", userName);

        // 使用 BigDecimal 保证精度
        BigDecimal currentBalance = user.getBalance();
        BigDecimal changeAmount = BigDecimal.valueOf(amount);
        BigDecimal newBalance = currentBalance.add(changeAmount);

        // 校验余额是否充足
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(0,"账户余额不足");
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserName, userName)
                .set(User::getBalance, newBalance.doubleValue());

        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 更新用户余额
     */
    @Override
    public Boolean updateByUserBalance(String userName, Double amount) {
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
        user.setBalance(BigDecimal.valueOf(amount));
        return userMapper.updateById(user)>0;
    }

    /**
     * 修改信用分
     * @param userName 用户名
     * @param creditScore 新信用分
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modifyUserReputationScore(String userName, Integer creditScore) {
        Assert.notNull(creditScore, "信用分不能为空");
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
//        Assert.notNull(user, "用户 '{}' 不存在", userName);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserName, userName)
                .set(User::getCreditScore, creditScore);

        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 修改银行卡号
     * @param userName 用户名
     * @param bankCard 新银行卡号
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean modifyTheUserSBankCard(String userName, String bankCard) {
        Assert.notBlank(bankCard, "银行卡号不能为空");
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
//        Assert.notNull(user, "用户 '{}' 不存在", userName);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserName, userName)
                .set(User::getBankCard, bankCard);

        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 修改登录密码
     * @param userName 用户名
     * @param password 新密码
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserPassword(String userName, String password) {
        Assert.notBlank(password, "新密码不能为空");
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
//        Assert.notNull(user, "用户 '{}' 不存在", userName);

        // 在实际项目中，密码必须经过加密处理
        // String encodedPassword = passwordEncoder.encode(password);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserName, userName)
                .set(User::getPassword, password); // 实际应为 encodedPassword

        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 修改提现密码
     * @param userName 用户名
     * @param withdrawalPassword 新提现密码
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeUserWithdrawalPassword(String userName, String withdrawalPassword) {
        Assert.notBlank(withdrawalPassword, "新提现密码不能为空");
        User user = this.queryUser(userName);
        if (user==null){
            throw new BusinessException(0,"用户 {"+userName+"} 不存在");
        }
//        Assert.notNull(user, "用户 '{}' 不存在", userName);

        // 在实际项目中，密码必须经过加密处理
        // String encodedPassword = passwordEncoder.encode(withdrawalPassword);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserName, userName)
                .set(User::getWithdrawalPassword, withdrawalPassword); // 实际应为 encodedPassword

        return userMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public User selectByUserName(UserDTO userDTO) {

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserName,userDTO.getUserName());
        return userMapper.selectOne(lambdaQueryWrapper);
    }
    /**
     * 根据id查询用户消息
     */
    @Override
    public User queryUserByUserId(Long id) {
        User s = userMapper.selectById(id);
        if (s==null){
            throw new BusinessException(0,"查询不到该用户");
        }
        return s;
    }
}