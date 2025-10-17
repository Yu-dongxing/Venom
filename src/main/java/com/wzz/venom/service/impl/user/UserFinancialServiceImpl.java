package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserFinancial;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.mapper.UserFinancialMapper;
import com.wzz.venom.service.user.UserFinancialService;
import com.wzz.venom.service.user.UserFundFlowService; // 新增导入
import com.wzz.venom.service.user.UserService;
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

    @Autowired
    private UserService userService;

    // --- 新增注入 UserFundFlowService ---
    @Autowired
    private UserFundFlowService userFundFlowService;

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
     * 扣减用户理财余额 (逻辑已修改)
     * 现在此方法会原子性地完成两件事：
     * 1. 扣减理财账户余额。
     * 2. 在用户资金流水中增加一笔等额的收入记录。
     *
     * @param user   用户名
     * @param amount 扣减金额
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 使用事务确保数据一致性
    public boolean reduceUserFinancialBalance(String user, Double amount) {
        // 1. 参数校验
        if (user == null || amount == null || amount <= 0) {
            // 在实际项目中，更建议抛出异常而不是返回 false
            throw new BusinessException(0, "无效的参数！");
        }

        BigDecimal deductionAmount = BigDecimal.valueOf(amount);

        // 2. 使用 UpdateWrapper 原子性地扣减理财余额，并确保余额充足
        UpdateWrapper<UserFinancial> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq("user_name", user)  // **修正：字段名为 user_name**
                .ge("amount", deductionAmount) // 关键：确保余额充足 (amount >= deductionAmount)
                .setSql("amount = amount - " + deductionAmount.toPlainString()); // 执行SQL扣减

        int affectedRows = userFinancialMapper.update(null, updateWrapper);

        // 3. 检查扣减是否成功
        if (affectedRows <= 0) {
            // 如果影响行数为0，说明余额不足，抛出异常，事务将回滚
            throw new BusinessException(0, "理财账户余额不足，转出失败！");
        }

        // 4. 理财余额扣减成功后，为用户增加一条资金流水记录
        String description = "理财资金转出至账户余额";
        boolean flowAdded = userFundFlowService.increaseUserTransactionAmount(user, amount, description);

        // 如果增加流水失败（例如，increaseUserTransactionAmount内部抛出异常），整个事务也会回滚
        if (!flowAdded) {
            throw new BusinessException(0, "创建资金流水失败，操作已回滚！");
        }

        // 5. 所有操作成功
        return true;
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
     * 增加用户理财余额 (转入理财)
     * <p>
     * 修改后的逻辑 (原子操作):
     * 1. 从用户主账户余额中扣除相应金额，并生成一条支出流水。
     * 2. 如果扣款成功，则查询用户的理财记录。
     * 3. 如果理财记录不存在，则为该用户创建一条新的理财记录。
     * 4. 如果理财记录已存在，则在原有金额上增加指定数额。
     * </p>
     *
     * @param user   用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 确保任何异常都会回滚整个事务
    public boolean increaseUserFinancialBalance(String user, Double amount) {
        // 1. 参数校验
        if (user == null || amount == null || amount <= 0) {
            throw new BusinessException(0, "无效的转入金额！");
        }

        // 2.【关键步骤】从用户主账户扣款，并生成资金流水
        // 如果余额不足，reduceUserTransactionAmount 方法会抛出异常，此事务将自动回滚
        String description = "资金转入理财账户";
        userFundFlowService.reduceUserTransactionAmount(user, amount, description);

        // 3. 主账户扣款成功后，为用户的理财账户增加余额
        List<UserFinancial> financials = queryTheDesignatedUserSFinancialInformation(user);
        BigDecimal increaseAmount = BigDecimal.valueOf(amount);

        if (CollectionUtils.isEmpty(financials)) {
            // 如果用户没有理财账户，创建一个新的
            UserFinancial newUserFinancial = new UserFinancial();
            newUserFinancial.setUserName(user);
            newUserFinancial.setAmount(increaseAmount);
            return userFinancialMapper.insert(newUserFinancial) > 0;
        } else {
            // 如果已有理财账户，直接增加金额
            UserFinancial userFinancial = financials.get(0);
            BigDecimal newAmount = userFinancial.getAmount().add(increaseAmount);

            UpdateWrapper<UserFinancial> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", userFinancial.getId())
                    .set("amount", newAmount);
            return userFinancialMapper.update(null, updateWrapper) > 0;
        }
    }

    /**
     * 增加用户理财余额 (理财收益)
     * <p>
     * 修改后的逻辑 (原子操作):
     * 1. 从用户主账户余额中扣除相应金额，并生成一条支出流水。
     * 2. 如果扣款成功，则查询用户的理财记录。
     * 3. 如果理财记录不存在，则为该用户创建一条新的理财记录。
     * 4. 如果理财记录已存在，则在原有金额上增加指定数额。
     * </p>
     *
     * @param user   用户名
     * @param amount 增加金额
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean increaseUserFinancialBalanceIncome(String user, Double amount) {
        // 1. 参数校验
        if (user == null || amount == null || amount <= 0) {
            throw new BusinessException(0, "无效的转入金额！");
        }
        // 3. 主账户扣款成功后，为用户的理财账户增加余额
        List<UserFinancial> financials = queryTheDesignatedUserSFinancialInformation(user);
        BigDecimal increaseAmount = BigDecimal.valueOf(amount);

        if (CollectionUtils.isEmpty(financials)) {
            // 如果用户没有理财账户，创建一个新的
            UserFinancial newUserFinancial = new UserFinancial();
            newUserFinancial.setUserName(user);
            newUserFinancial.setAmount(increaseAmount);
            return userFinancialMapper.insert(newUserFinancial) > 0;
        } else {
            // 如果已有理财账户，直接增加金额
            UserFinancial userFinancial = financials.get(0);
            BigDecimal newAmount = userFinancial.getAmount().add(increaseAmount);

            UpdateWrapper<UserFinancial> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", userFinancial.getId())
                    .set("amount", newAmount);
            return userFinancialMapper.update(null, updateWrapper) > 0;
        }
    }

    /**
     * [新增] 查询所有用户理财信息 (供管理端使用)
     * @return 所有理财信息列表
     */
    @Override
    public List<UserFinancial> findAll() {
        LambdaQueryWrapper<UserFinancial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(UserFinancial::getUpdateTime);
        // 调用 mybatis-plus 的 selectList 方法，传入 null 查询所有
        return userFinancialMapper.selectList(queryWrapper);
    }
    /**
     * [新增] 分页查询所有用户理财信息 (供管理端使用)
     * @param page 分页参数对象
     * @return 分页结果对象
     */
    @Override
    public Page<UserFinancial> findAllByPage(Page<UserFinancial> page) {
        LambdaQueryWrapper<UserFinancial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(UserFinancial::getUpdateTime);
        return userFinancialMapper.selectPage(page, queryWrapper);
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

    //todo 返回字段 计算总共收益以及昨日收益/所有列表
    @Override
    public List<UserFinancial> queryTheDesignatedUserSFinancialInformationByuserId(Long userId, String currentUser) {
        User s = userService.queryUserByUserId(userId);
        if (s==null){
            throw  new BusinessException(0,"查询不到该用户");
        }
        return this.queryTheDesignatedUserSFinancialInformation(s.getUserName());
    }
}