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
            // 金额不合法，可以根据业务需求抛出异常或返回false
            return false;
        }

        UserFinancialStatement statement = new UserFinancialStatement();
        statement.setUserName(user);
        // 将Double转换为BigDecimal，并确保是正数
        statement.setAmount(BigDecimal.valueOf(amount));

        // 注意：这里的 transactionType 和 financialId 字段没有在方法参数中提供
        // 在实际业务中，可能需要根据业务场景设置默认值或从其他地方获取
        // 例如，可以定义一个常量表示“通用增加”类型
        // statement.setTransactionType(1); // 假设1代表通用买入/增加

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
        // 将Double转换为BigDecimal，并使用 .negate() 方法取负值
        statement.setAmount(BigDecimal.valueOf(amount).negate());

        // 同样，这里的 transactionType 和 financialId 字段也需要注意
        // statement.setTransactionType(2); // 假设2代表通用赎回/减少

        return userFinancialStatementMapper.insert(statement) > 0;
    }
}