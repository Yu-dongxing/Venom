package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.domain.dto.UserIncomeStatsDto;
import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.mapper.UserFinancialStatementMapper;
import com.wzz.venom.service.user.UserFinancialStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // 定义一个常量来表示“收益派发”的交易类型，避免使用魔法数字
    private static final int TRANSACTION_TYPE_INCOME = 3;

    @Override
    public UserIncomeStatsDto getIncomeStatistics(String userName) {
        // 1. 计算总收益
        BigDecimal totalIncome = calculateTotalIncome(userName);

        // 2. 计算昨日收益
        BigDecimal yesterdayIncome = calculateYesterdayIncome(userName);

        // 3. 查询所有收益记录列表
        List<UserFinancialStatement> incomeRecords = queryIncomeRecords(userName);

        // 4. 组装并返回结果
        return new UserIncomeStatsDto(totalIncome, yesterdayIncome, incomeRecords);
    }

    /**
     * 私有辅助方法：计算总收益
     */
    private BigDecimal calculateTotalIncome(String userName) {
        QueryWrapper<UserFinancialStatement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName)
                .eq("transaction_type", TRANSACTION_TYPE_INCOME)
                .select("SUM(amount) as totalAmount"); // 使用 SQL 的 SUM 函数

        // selectObjs 返回一个 Object 列表，这里我们只需要第一个
        List<Object> result = userFinancialStatementMapper.selectObjs(queryWrapper);
        if (result != null && !result.isEmpty() && result.get(0) != null) {
            return (BigDecimal) result.get(0);
        }
        return BigDecimal.ZERO; // 如果没有记录，则返回 0
    }

    /**
     * 私有辅助方法：计算昨日收益
     */
    private BigDecimal calculateYesterdayIncome(String userName) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        QueryWrapper<UserFinancialStatement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName)
                .eq("transaction_type", TRANSACTION_TYPE_INCOME)
                // 查询创建时间在昨天一整天范围内的数据
                .between("create_time", yesterday.atStartOfDay(), yesterday.atTime(LocalTime.MAX))
                .select("SUM(amount) as yesterdayAmount");

        List<Object> result = userFinancialStatementMapper.selectObjs(queryWrapper);
        if (result != null && !result.isEmpty() && result.get(0) != null) {
            return (BigDecimal) result.get(0);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 私有辅助方法：查询所有收益记录
     */
    private List<UserFinancialStatement> queryIncomeRecords(String userName) {
        QueryWrapper<UserFinancialStatement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName)
                .eq("transaction_type", TRANSACTION_TYPE_INCOME)
                .orderByDesc("create_time"); // 按创建时间降序排列

        return userFinancialStatementMapper.selectList(queryWrapper);
    }
}