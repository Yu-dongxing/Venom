package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wzz.venom.domain.entity.UserFinancial;
import com.wzz.venom.mapper.UserFinancialMapper;
import com.wzz.venom.service.user.UserFinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.math.BigDecimal;
import java.util.List;

@Service
public class UserFinancialServiceImpl implements UserFinancialService {

    @Autowired
    private UserFinancialMapper userFinancialMapper;

    /**
     * 新增用户理财信息
     * @param financial 理财信息对象
     * @return 是否成功
     */
    @Override
    @Transactional // 建议添加事务注解
    public boolean addUserFinancialInformation(UserFinancial financial) {
        // 可以增加一些业务校验，例如检查关键字段是否为空
        if (financial == null || financial.getUserName() == null || financial.getAmount() == null) {
            // 在实际项目中，这里应该抛出自定义异常或返回更详细的错误信息
            return false;
        }
        // 调用MyBatis-Plus的insert方法，insert > 0 表示插入成功
        return userFinancialMapper.insert(financial) > 0;
    }

    /**
     * 更新用户理财信息
     * @param financial 理财信息对象
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateUserFinancialInformation(UserFinancial financial) {
        // 校验传入的对象和主键ID是否存在
        if (financial == null || financial.getId() == null) {
            return false;
        }
        // 调用MyBatis-Plus的updateById方法， a > 0 表示更新成功
        return userFinancialMapper.updateById(financial) > 0;
    }

    /**
     * 查询用户理财信息（按用户与金额）
     * @param user 用户名
     * @param amount 理财金额
     * @return 匹配的理财记录
     */
    @Override
    public List<UserFinancial> searchForUserFinancialInformation(String user, Double amount) {
        // 使用QueryWrapper构造查询条件
        QueryWrapper<UserFinancial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user);
        // 注意：由于实体类中amount是BigDecimal，这里进行转换
        // 直接用Double进行等值查询可能因精度问题导致查不到数据，建议使用范围查询或确保数据库字段类型兼容
        if (amount != null) {
            queryWrapper.eq("amount", BigDecimal.valueOf(amount));
        }
        return userFinancialMapper.selectList(queryWrapper);
    }

    /**
     * 扣减用户理财余额
     * 这是一个简化实现，假设一个用户只有一条理财记录。
     * 如果一个用户可以有多条，逻辑会更复杂，需要明确扣减哪一条记录。
     * @param user 用户名
     * @param amount 扣减金额
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean reduceUserFinancialBalance(String user, Double amount) {
        if (user == null || amount == null || amount <= 0) {
            return false;
        }

        // 1. 先查询出用户的理财信息
        List<UserFinancial> financials = queryTheDesignatedUserSFinancialInformation(user);
        if (CollectionUtils.isEmpty(financials)) {
            // 用户没有理财信息
            return false;
        }

        // 假设只处理第一条理财记录
        UserFinancial userFinancial = financials.get(0);
        BigDecimal deductionAmount = BigDecimal.valueOf(amount);

        // 2. 检查余额是否充足
        if (userFinancial.getAmount().compareTo(deductionAmount) < 0) {
            // 余额不足
            return false;
        }

        // 3. 计算新余额并更新
        BigDecimal newAmount = userFinancial.getAmount().subtract(deductionAmount);

        // 使用UpdateWrapper进行更新，可以附带乐观锁等防止并发问题
        UpdateWrapper<UserFinancial> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userFinancial.getId()) // 定位到具体记录
                .set("amount", newAmount); // 设置新值

        return userFinancialMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 查询指定用户的全部理财信息
     * @param user 用户名
     * @return 理财信息列表
     */
    @Override
    public List<UserFinancial> queryTheDesignatedUserSFinancialInformation(String user) {
        QueryWrapper<UserFinancial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user);
        return userFinancialMapper.selectList(queryWrapper);
    }

    /**
     * 删除指定用户理财信息
     * @param user 用户名
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteDesignatedUserSFinancialInformation(String user) {
        QueryWrapper<UserFinancial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user);
        // delete > 0 表示删除成功
        return userFinancialMapper.delete(queryWrapper) > 0;
    }

    /**
     * 增加用户理财余额
     * 同样是简化实现，假设一个用户只有一条理财记录。
     * @param user 用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean increaseUserFinancialBalance(String user, Double amount) {
        if (user == null || amount == null || amount <= 0) {
            return false;
        }

        List<UserFinancial> financials = queryTheDesignatedUserSFinancialInformation(user);
        if (CollectionUtils.isEmpty(financials)) {
            return false;
        }

        // 假设只处理第一条记录
        UserFinancial userFinancial = financials.get(0);
        BigDecimal increaseAmount = BigDecimal.valueOf(amount);
        BigDecimal newAmount = userFinancial.getAmount().add(increaseAmount);

        UpdateWrapper<UserFinancial> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userFinancial.getId())
                .set("amount", newAmount);

        return userFinancialMapper.update(null, updateWrapper) > 0;
    }

    /**
     * [新增] 查询所有用户理财信息 (供管理端使用)
     * @return 所有理财信息列表
     */
    @Override
    public List<UserFinancial> findAll() {
        // 调用 mybatis-plus 的 selectList 方法，传入 null 查询所有
        return userFinancialMapper.selectList(null);
    }

    /**
     * [新增] 根据ID删除指定理财信息 (供管理端使用)
     * @param id 理财记录ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteFinancialInformationById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return userFinancialMapper.deleteById(id) > 0;
    }
}