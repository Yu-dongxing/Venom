package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.mapper.UserFinancialStatementMapper;
import com.wzz.venom.service.user.UserFinancialStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户理财流水服务实现类
 */
@Service
public class UserFinancialStatementServiceImpl implements UserFinancialStatementService {

    // 推荐使用构造函数注入
    private final UserFinancialStatementMapper userFinancialStatementMapper;

    @Autowired
    public UserFinancialStatementServiceImpl(UserFinancialStatementMapper userFinancialStatementMapper) {
        this.userFinancialStatementMapper = userFinancialStatementMapper;
    }

    /**
     * 新增用户理财流水记录
     * @param statement 理财流水对象
     * @return 是否成功
     */
    @Override
    @Transactional // 开启事务
    public boolean addUserFinancialStatements(UserFinancialStatement statement) {
        // 调用MyBatis-Plus的insert方法，插入成功返回影响的行数（1）
        return userFinancialStatementMapper.insert(statement) > 0;
    }

    /**
     * 更新用户理财流水记录
     * @param statement 理财流水对象
     * @return 是否成功
     */
    @Override
    @Transactional // 开启事务
    public boolean updateUserFinancialStatements(UserFinancialStatement statement) {
        // 根据主键ID更新，更新成功返回影响的行数（1）
        return userFinancialStatementMapper.updateById(statement) > 0;
    }

    /**
     * 查询指定用户的理财流水列表
     * @param user 用户名
     * @return 理财流水列表
     */
    @Override
    public List<UserFinancialStatement> queryTheDesignatedUserSFinancialStatementList(String user) {
        // 使用QueryWrapper构造查询条件
        QueryWrapper<UserFinancialStatement> queryWrapper = new QueryWrapper<>();
        // 查询条件：user_name 字段等于传入的 user 参数
        queryWrapper.eq("user_name", user);
        // 可以根据需要添加排序等其他条件，例如按创建时间降序
        // queryWrapper.orderByDesc("create_time");
        return userFinancialStatementMapper.selectList(queryWrapper);
    }

    /**
     * 删除指定编号的理财流水记录
     * @param id 流水ID
     * @return 是否成功
     */
    @Override
    @Transactional // 开启事务
    public boolean deleteSpecifiedNumberInformation(Long id) {
        // 根据主键ID删除，删除成功返回影响的行数（1）
        return userFinancialStatementMapper.deleteById(id) > 0;
    }

    /**
     * 增加用户交易金额（记录正向流水）
     * @param user 用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    @Override
    @Transactional // 开启事务
    public boolean increaseUserUnderstandingOfTransactionAmount(String user, Double amount) {
        if (amount == null || amount <= 0) {
            return false;
        }

        UserFinancialStatement statement = new UserFinancialStatement();
        statement.setUserName(user);
        statement.setAmount(BigDecimal.valueOf(amount));

        return userFinancialStatementMapper.insert(statement) > 0;
    }

    /**
     * 减少用户交易金额（记录负向流水）
     * @param user 用户名
     * @param amount 减少金额
     * @return 是否成功
     */
    @Override
    @Transactional // 开启事务
    public boolean reduceUserUnderstandingOfTransactionAmounts(String user, Double amount) {
        if (amount == null || amount <= 0) {
            // 金额不合法
            return false;
        }

        UserFinancialStatement statement = new UserFinancialStatement();
        statement.setUserName(user);
        statement.setAmount(BigDecimal.valueOf(amount).negate());
        return userFinancialStatementMapper.insert(statement) > 0;
    }
}