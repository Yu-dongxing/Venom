package com.wzz.venom.controller.user;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.UserFinancial;
import com.wzz.venom.service.user.UserFinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户理财接口控制器
 * 模块：理财转入 / 理财转出 / 收益查询
 */
@RestController
@RequestMapping("/api/user/financial")
public class UserFinancialController {

    @Autowired
    private UserFinancialService userFinancialService;

    /**
     * 用户转入理财
     * @param amount 转入金额
     * @return 操作结果
     */
    @PostMapping("/transferIn")
    public Result<?> userTransfersToFinancialManagement(@RequestParam Double amount) {
        // 注意：在真实项目中，用户名应该从安全框架（如Spring Security）的上下文中获取
        // String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        // 此处为演示，硬编码一个用户名
        String currentUser = "testUser001";

        boolean success = userFinancialService.increaseUserFinancialBalance(currentUser, amount);
        return success ? Result.success("转入成功") : Result.error("转入失败，请稍后重试");
    }

    /**
     * 用户转出理财
     * @param amount 转出金额
     * @return 操作结果
     */
    @PostMapping("/transferOut")
    public Result<?> userTransfersOutFinancialManagement(@RequestParam Double amount) {
        // 同样，从安全上下文中获取当前用户
        String currentUser = "testUser001";

        boolean success = userFinancialService.reduceUserFinancialBalance(currentUser, amount);
        return success ? Result.success("转出成功") : Result.error("转出失败，可能余额不足");
    }

    /**
     * 用户获取自己的理财信息列表
     * 注意：为了安全，此接口不接受 user 参数，强制查询当前登录用户的信息
     * @return 理财信息列表
     */
    @GetMapping("/incomeList")
    public Result<List<UserFinancial>> userObtainsFinancialIncomeList() {
        // 同样，从安全上下文中获取当前用户
        String currentUser = "testUser001";

        List<UserFinancial> list = userFinancialService.queryTheDesignatedUserSFinancialInformation(currentUser);
        return Result.success(list);
    }
}