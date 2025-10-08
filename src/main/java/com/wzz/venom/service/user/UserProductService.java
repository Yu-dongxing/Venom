package com.wzz.venom.service.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.domain.entity.UserProduct;

import java.util.List;

public interface UserProductService {
    /**
     * 查询全部用户产品
     * @return 产品列表
     */
    List<UserProduct> findAllProducts();

    /**
     * 更新用户产品信息
     * @param product 产品对象
     * @return 是否成功
     */
    boolean updateUserProducts(UserProduct product);

    /**
     * 添加用户产品
     * @param product 产品对象
     * @return 是否成功
     */
    boolean addUserProducts(UserProduct product);

    /**
     * 根据用户查询持有产品
     * @param userName 用户名
     * @return 用户产品列表
     */
    List<UserProduct> searchForProductsHeldByUsersBasedOnTheirOwnership(String userName);

    /**
     * 查询用户交易结束产品
     * @param userName 用户名
     * @return 已结束产品列表
     */
    List<UserProduct> searchForEndOfUserTransactionProductsBasedOnUserSearch(String userName);

    /**
     * 根据产品编号修改状态类型
     * @param id 产品ID
     * @param status 状态（0-持有中，1-已结束）
     * @return 是否成功
     */
    boolean modifyTheProductStatusTypeBasedOnTheNumber(Long id, Integer status);

    /**
     * 根据产品编号删除用户产品
     * @param id 产品ID
     * @return 是否成功
     */
    boolean deleteUserProductsBasedOnTheirIdentificationNumbers(Long id);

    List<UserProduct> list(QueryWrapper<UserProduct> queryWrapper);
}
