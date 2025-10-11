package com.wzz.venom.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.UserProduct;
import com.wzz.venom.service.user.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 产品管理接口
 * 模块：产品查询 / 更新 / 删除
 */
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    private final UserProductService userProductService;

    // 使用构造函数注入，是Spring推荐的最佳实践
    @Autowired
    public AdminProductController(UserProductService userProductService) {
        this.userProductService = userProductService;
    }

    /**
     * 查询所有未到期产品列表 (status = 0)
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/unexpired")
    public Result<List<UserProduct>> queryTheListOfProductsThatHaveNotYetExpired() {
        // 使用 QueryWrapper 构建查询条件
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0); // 状态为0表示持有中（未到期）
        List<UserProduct> productList = userProductService.list(queryWrapper);
        return Result.success(productList);
    }

    /**
     * 查询所有已到期产品列表 (status = 1)
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/expired")
    public Result<List<UserProduct>> queryTheListOfExpiredProducts() {
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 状态为1表示已结束（已到期）
        List<UserProduct> productList = userProductService.list(queryWrapper);
        return Result.success(productList);
    }

    /**
     * 查询指定用户未到期产品 (status = 0)
     * @param user 用户名
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/user/unexpired")
    public Result<List<UserProduct>> queryTheListOfUnexpiredProductsForTheSpecifiedUser(@RequestParam String user) {
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user)
                .eq("status", 0);
        List<UserProduct> productList = userProductService.list(queryWrapper);
        return Result.success(productList);
    }

    /**
     * 查询指定用户已到期产品 (status = 1)
     * @param user 用户名
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/user/expired")
    public Result<List<UserProduct>> queryTheListOfExpiredProductsForTheSpecifiedUser(@RequestParam String user) {
        // 直接调用Service中已有的方法，该方法逻辑完全符合需求
        List<UserProduct> productList = userProductService.searchForEndOfUserTransactionProductsBasedOnUserSearch(user);
        return Result.success(productList);
    }

    /**
     * 更新指定编号的产品信息
     * @param userProduct 包含产品ID和待更新信息的产品对象
     * @return Result
     */
    @PostMapping("/update")
    // 将 @RequestBody Object 修改为 @RequestBody UserProduct，代码更健壮
    public Result<?> updateProductInformationWithTheSpecifiedNumber(@RequestBody UserProduct userProduct) {
        if (userProduct.getId() == null) {
            return Result.error("更新失败：产品ID不能为空");
        }
        boolean isSuccess = userProductService.updateUserProducts(userProduct);
        return isSuccess ? Result.success("产品信息更新成功") : Result.error("产品信息更新失败，请检查产品是否存在");
    }

    /**
     * 删除指定编号的产品
     * @param id 产品ID
     * @return Result
     */
    @PostMapping("/delete")
    public Result<?> deleteTheDesignatedProductNumber(@RequestParam Long id) {
        boolean isSuccess = userProductService.deleteUserProductsBasedOnTheirIdentificationNumbers(id);
        return isSuccess ? Result.success("产品删除成功") : Result.error("产品删除失败，请检查产品是否存在");
    }
    /**
     * 查询所有用户产品列表
     */
    @GetMapping("/find/all")
    public Result<?> findAll(){
        return Result.success("查询成功！",userProductService.list());
    }
}