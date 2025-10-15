package com.wzz.venom.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzz.venom.domain.entity.UserProduct;
import com.wzz.venom.mapper.UserProductMapper;
import com.wzz.venom.service.impl.product.ProductSchedulingServiceImpl;
import com.wzz.venom.service.product.ProductSchedulingService;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 建议对写操作添加事务管理

import java.util.List;

/**
 * 用户持有产品服务实现类
 *
 * @author (你的名字)
 * @date (当前日期)
 */
@Service
public class UserProductServiceImpl extends ServiceImpl<UserProductMapper, UserProduct> implements UserProductService {

    // ServiceImpl<M, T> 泛型分别是 Mapper 和 Entity
    // 继承 ServiceImpl 可以简化大量的CRUD操作，是MyBatis-Plus的推荐用法
    // 但为了清晰地对应你的接口，我将逐一实现所有方法

    @Autowired // JSR-250规范的注解，Spring推荐使用
    private UserProductMapper userProductMapper;

    /**
     * 查询全部用户产品
     * @return 产品列表
     */
    @Override
    public List<UserProduct> findAllProducts() {
        LambdaQueryWrapper<UserProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(UserProduct::getUpdateTime);
        // 调用BaseMapper的selectList方法，传入null查询所有
        return userProductMapper.selectList(queryWrapper);
    }

    /**
     * 根据状态查询产品列表
     */
    @Override
    public List<UserProduct> findProductByStatus(Integer status) {
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("update_time");
        return userProductMapper.selectList(queryWrapper);
    }

    /**
     * 更新用户产品信息
     * @param product 产品对象
     * @return 是否成功
     */
    @Override
    @Transactional // 保证数据一致性
    public boolean updateUserProducts(UserProduct product) {
        // updateById会根据ID更新实体，返回影响的行数
        // 影响行数 > 0 表示更新成功
        return userProductMapper.updateById(product) > 0;
    }

    // 新增：注入用户资金流水服务
    @Autowired
    private UserFundFlowService userFundFlowService;

    @Lazy
    @Autowired
    private ProductSchedulingService productSchedulingService;
    /**
     * 添加用户产品 (已重构，增加扣款逻辑)
     * @param product 产品对象
     * @return 是否成功
     */
    @Override
    // 确保整个方法在一个事务中执行。如果任何步骤失败（包括资金扣除），所有数据库操作都将回滚。
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserProducts(UserProduct product) {
        // 步骤1：扣除用户账户资金
        String description = String.format("购买理财产品：%s", product.getProductName());
        productSchedulingService.reschedulePendingTasksOnStartup();
        userFundFlowService.reduceUserTransactionAmount(
                product.getUserName(),
                product.getAmount(), // DTO传递过来的amount是Double类型
                description
        );

        return userProductMapper.insert(product) > 0;
    }

    /**
     * 根据用户查询持有产品
     * @param userName 用户名
     * @return 用户产品列表
     */
    @Override
    public List<UserProduct> searchForProductsHeldByUsersBasedOnTheirOwnership(String userName) {
        // 使用QueryWrapper构造查询条件
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        return userProductMapper.selectList(queryWrapper);
    }

    /**
     * 查询用户交易结束产品
     * @param userName 用户名
     * @return 已结束产品列表
     */
    @Override
    public List<UserProduct> searchForEndOfUserTransactionProductsBasedOnUserSearch(String userName) {
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        // 链式调用，构建复合查询条件
        // WHERE user_name = 'xxx' AND status = 1
        queryWrapper.eq("user_name", userName)
                .eq("status", 1); // 1 代表 "已结束"
        return userProductMapper.selectList(queryWrapper);
    }

    /**
     * 根据产品编号修改状态类型
     * @param id 产品ID
     * @param status 状态（0-持有中，1-已结束）
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean modifyTheProductStatusTypeBasedOnTheNumber(Long id, Integer status) {
        // 1. 先根据ID查询出实体
        UserProduct userProduct = userProductMapper.selectById(id);
        if (userProduct == null) {
            // 如果产品不存在，可以根据业务需求返回false或抛出异常
            return false;
        }

        // 2. 修改状态
        userProduct.setStatus(status);

        // 3. 更新实体
        return userProductMapper.updateById(userProduct) > 0;
    }

    /**
     * 根据产品编号删除用户产品
     * @param id 产品ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteUserProductsBasedOnTheirIdentificationNumbers(Long id) {
        // deleteById方法根据主键ID删除记录
        return userProductMapper.deleteById(id) > 0;
    }

    @Override
    public List<UserProduct> list(QueryWrapper<UserProduct> queryWrapper) {
        return userProductMapper.selectList(queryWrapper);
    }

}