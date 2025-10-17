package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.annotation.CheckUserFrozen;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserFinancial;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserFinancialService;
import com.wzz.venom.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

/**
 * 用户理财接口控制器
 * 模块：理财转入 / 理财转出 / 收益查询
 */
@RestController
@RequestMapping("/api/user/financial")
public class UserFinancialController {

    @Autowired
    private UserFinancialService userFinancialService;

    @Autowired
    private UserService userService;

    /**
     * 用户转入理财
     * @param amount 转入金额
     * @return 操作结果
     */
    @CheckUserFrozen
    @PostMapping("/transferIn")
    public Result<?> userTransfersToFinancialManagement(@RequestParam Double amount) {
        try {
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User u =  userService.queryUserByUserId(userId);
            if (u==null){
                return Result.error("无法查询该用户！");
            }

            boolean success = userFinancialService.increaseUserFinancialBalance(u.getUserName(), amount);
            return success ? Result.success("转入成功") : Result.error("转入失败，请稍后重试");
        }catch (BusinessException e) {
            return Result.error(e.getMessage());
        }

    }

    /**
     * 用户转出理财
     * @param amount 转出金额
     * @return 操作结果
     */
    @CheckUserFrozen
    @PostMapping("/transferOut")
    public Result<?> userTransfersOutFinancialManagement(@RequestParam Double amount) {
        try {
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User u =  userService.queryUserByUserId(userId);
            if (u==null){
                return Result.error("无法查询该用户！");
            }
            if (!u.getIfOut()){
                return Result.error("资金未到期，不可转出 ");
            }
            boolean success = userFinancialService.reduceUserFinancialBalance(u.getUserName(), amount);
            return success ? Result.success("转出成功") : Result.error("转出失败，可能余额不足");
        }catch (BusinessException e) {
            return Result.error(e.getMessage());
        }

    }

    /**
     * 用户获取自己的理财信息列表
     * 注意：为了安全，此接口不接受 user 参数，强制查询当前登录用户的信息
     * @return 理财信息列表
     */
    @GetMapping("/incomeList")
    public Result<?> userObtainsFinancialIncomeList() {
        try{
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            List<UserFinancial> list = userFinancialService.queryTheDesignatedUserSFinancialInformationByuserId(userId,"text");
            return Result.success("查询成功！",list);
        }catch (BusinessException e){
            return Result.error(e.getMessage());
        }
    }
}