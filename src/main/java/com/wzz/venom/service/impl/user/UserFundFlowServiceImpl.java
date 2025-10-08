package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.mapper.UserFundFlowMapper;
import com.wzz.venom.service.user.UserFundFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户资金流水服务实现类
 *
 * @author (Your Name)
 */
@Service
public class UserFundFlowServiceImpl implements UserFundFlowService {

    private final UserFundFlowMapper userFundFlowMapper;

    // 推荐使用构造函数注入，更符合Spring的最佳实践
    @Autowired
    public UserFundFlowServiceImpl(UserFundFlowMapper userFundFlowMapper) {
        this.userFundFlowMapper = userFundFlowMapper;
    }

    /**
     * 新增用户理财流水记录
     * 注意：接口定义为UserFinancialStatement，但根据上下文实体应为UserFundFlow
     * @param statement 理财流水对象
     * @return 是否成功
     */
    @Transactional
    @Override
    public boolean addUserFinancialStatements(UserFundFlow statement) {
        // MyBatis Plus的insert方法返回值是受影响的行数，大于0即为成功
        return userFundFlowMapper.insert(statement) > 0;
    }

    /**
     * 更新用户理财流水记录
     * @param statement 理财流水对象
     * @return 是否成功
     */
    @Transactional
    @Override
    public boolean updateUserFinancialStatements(UserFundFlow statement) {
        // 根据ID更新，返回受影响行数
        return userFundFlowMapper.updateById(statement) > 0;
    }

    /**
     * 查询指定用户的理财流水列表
     * @param user 用户名
     * @return 理财流水列表
     */
    @Override
    public List<UserFundFlow> queryTheDesignatedUserSFinancialStatementList(String user) {
        // 使用QueryWrapper构造查询条件
        QueryWrapper<UserFundFlow> queryWrapper = new QueryWrapper<>();
        // "user_name" 是数据库中的字段名
        queryWrapper.eq("user_name", user);
        // 按创建时间倒序排列，让最新的流水显示在最前面
        queryWrapper.orderByDesc("create_time");
        return userFundFlowMapper.selectList(queryWrapper);
    }

    /**
     * 删除指定编号的理财流水记录
     * @param id 流水ID
     * @return 是否成功
     */
    @Override
    @Transactional // 删除操作，建议加上事务
    public boolean deleteSpecifiedNumberInformation(Long id) {
        return userFundFlowMapper.deleteById(id) > 0;
    }

    /**
     * 增加用户交易金额（记录正向流水）
     * 方法名建议修改为 recordIncomeFlow 或类似名称
     * @param user 用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 资金操作，必须有事务，并指定回滚的异常类型
    public boolean increaseUserUnderstandingOfTransactionAmount(String user, Double amount) {
        if (amount <= 0) {
            // 金额必须是正数
            // 可以抛出自定义异常或返回false
            return false;
        }

        // TODO: 实际业务中，此处应该先查询用户当前余额，然后计算新余额。
        // 由于缺少用户余额表，这里仅作演示。
        BigDecimal currentBalance = BigDecimal.ZERO; // 假设从用户服务查到的当前余额为0

        UserFundFlow flow = new UserFundFlow();
        flow.setUserName(user);
        // 关键：将接口传入的Double转换为BigDecimal进行计算，避免精度问题
        flow.setAmount(BigDecimal.valueOf(amount));
        flow.setFundType("INCOME"); // 资金类型，建议使用枚举或常量定义
        flow.setDescription("用户收入"); // 交易描述
        flow.setStatus(0); // 状态：0-成功
        // 计算操作后余额
        flow.setBalance(currentBalance.add(flow.getAmount()));

        return userFundFlowMapper.insert(flow) > 0;
    }

    /**
     * 减少用户交易金额（记录负向流水）
     * 方法名建议修改为 recordExpenseFlow 或类似名称
     * @param user 用户名
     * @param amount 减少金额
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 资金操作，必须有事务
    public boolean reduceUserUnderstandingOfTransactionAmounts(String user, Double amount) {
        if (amount <= 0) {
            // 金额必须是正数
            return false;
        }

        // TODO: 实际业务中，应先查询用户当前余额，并判断余额是否充足。
        BigDecimal currentBalance = new BigDecimal("1000"); // 假设从用户服务查到的当前余额为1000

        BigDecimal expenseAmount = BigDecimal.valueOf(amount);

        // 判断余额是否足够
        if (currentBalance.compareTo(expenseAmount) < 0) {
            // 余额不足，可以抛出业务异常
            // throw new InsufficientBalanceException("用户余额不足");
            return false;
        }

        UserFundFlow flow = new UserFundFlow();
        flow.setUserName(user);
        // 支出流水的amount字段记录为负数
        flow.setAmount(expenseAmount.negate());
        flow.setFundType("EXPENSE"); // 资金类型
        flow.setDescription("用户支出"); // 交易描述
        flow.setStatus(0); // 状态：0-成功
        // 计算操作后余额
        flow.setBalance(currentBalance.subtract(expenseAmount));

        return userFundFlowMapper.insert(flow) > 0;
    }
}