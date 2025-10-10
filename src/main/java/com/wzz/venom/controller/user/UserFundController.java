package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.common.Result; // 假设这是您的统一返回结果类
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户资金账本接口控制器
 * 模块：充值 / 提现 / 账本查询 / 提现状态变更
 *
 * @author (Your Name)
 */
@RestController
@RequestMapping("/api/user/fund")
public class UserFundController {

    @Autowired
    private UserFundFlowService userFundFlowService;

    @Autowired
    private UserService userService;

    /**
     * 用户提交充值申请
     * @param amount 充值金额
     * @return Result
     */
    @PostMapping("/recharge")
    public Result<?> userSubmitsRechargeRequest(@RequestParam Double amount) {
        if (amount <= 0) {
            return Result.error("充值金额必须大于0");
        }
        try {
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User u =  userService.queryUserByUserId(userId);
            if (u==null){
                return Result.error("无法查询该用户！");
            }
            boolean success = userFundFlowService.increaseUserTransactionAmount(u.getUserName(), amount, "用户在线充值");
            return success ? Result.success("充值成功") : Result.error("充值失败，请稍后重试");
        }catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户提交提现申请
     * 注意：提现申请应该生成一条“处理中”的流水，而不是直接扣款。
     * 因此，这里直接调用 `reduceUserTransactionAmount` 可能不完全符合业务。
     * 一个更优的设计是在Service层提供一个 `requestWithdrawal` 方法。
     * 此处我们遵循现有接口，假设 `reduceUserTransactionAmount` 用于此目的。
     * @param amount 提现金额
     * @return Result
     */
    @PostMapping("/withdraw")
    public Result<?> userSubmitsWithdrawalRequest(@RequestParam Double amount) {
        if (amount <= 0) {
            return Result.error("提现金额必须大于0");
        }

        try {
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User u =  userService.queryUserByUserId(userId);
            if (u==null){
                return Result.error("无法查询该用户！");
            }
            boolean success = userFundFlowService.reduceUserTransactionAmountWITHDRA(u.getUserName(), amount, "用户申请提现");
            return success ? Result.success("提现申请已提交") : Result.error("提现申请失败");
        }catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户查看自己的资金流水
     * @param user 用户名
     * @return Result<List<UserFundFlow>>
     */
    @GetMapping("/flow")
    public Result<List<UserFundFlow>> userFundFlow(@RequestParam String user) {
        List<UserFundFlow> flowList = userFundFlowService.queryTheUserSFundFlowList(user);
        return Result.success(flowList);
    }

    /**
     * (管理员)查询所有提现交易信息
     * @return Result<List<UserFundFlow>>
     */
    @GetMapping("/withdrawRecords")
    public Result<List<UserFundFlow>> queryAllWithdrawalTransactionInformation() {
        // 安全注意：此接口应仅对管理员角色开放
        List<UserFundFlow> records = userFundFlowService.queryAllWithdrawalTransactionInformation();
        return Result.success(records);
    }

    /**
     * (管理员)修改提现状态
     * @param user 用户名
     * @param status 状态（1申请，2通过，3拒绝）
     * @return Result
     */
    @PostMapping("/modifyWithdrawStatus")
    public Result<?> modifyUserWithdrawalStatus(@RequestParam String user, @RequestParam Integer status) {
        // 安全注意：此接口应仅对管理员角色开放
        if (status == null || status < 1 || status > 3) {
            return Result.error("无效的状态码");
        }
        boolean success = userFundFlowService.modifyUserWithdrawalStatus(user, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败，可能未找到处理中的提现记录");
    }

    /**
     * (管理员)增加用户交易金额
     * @param user 用户名
     * @param amount 金额
     * @param describe 描述
     * @return Result
     */
    @PostMapping("/increase")
    public Result<?> increaseUserTransactionAmount(@RequestParam String user,
                                                   @RequestParam Double amount,
                                                   @RequestParam String describe) {
        // 安全注意：此接口应仅对管理员角色开放，用于后台调账、发放奖励等
        if (amount <= 0) {
            return Result.error("增加的金额必须大于0");
        }
        boolean success = userFundFlowService.increaseUserTransactionAmount(user, amount, describe);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    /**
     * (管理员)减少用户交易金额
     * @param user 用户名
     * @param amount 金额
     * @param describe 描述
     * @return Result
     */
    @PostMapping("/reduce")
    public Result<?> reduceUserTransactionAmount(@RequestParam String user,
                                                 @RequestParam Double amount,
                                                 @RequestParam String describe) {
        // 安全注意：此接口应仅对管理员角色开放，用于后台扣款等
        if (amount <= 0) {
            return Result.error("减少的金额必须大于0");
        }
        try {
            boolean success = userFundFlowService.reduceUserTransactionAmount(user, amount, describe);
            return success ? Result.success("操作成功") : Result.error("操作失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * (管理员)拒绝提现并返还余额
     * 注意：此功能已整合到 `modifyUserWithdrawalStatus` 接口中。
     * 当 status=3 (拒绝) 时，Service层会自动返还余额。
     * 此接口可作为补充或单独的补偿机制存在。
     * @param user 用户名
     * @param amount 金额
     * @return Result
     */
    @PostMapping("/refuse")
    public Result<?> refuseToWithdrawAndReturnBalance(@RequestParam String user, @RequestParam Double amount) {
        // 安全注意：此接口应仅对管理员角色开放
        if (amount <= 0) {
            return Result.error("返还的金额必须大于0");
        }
        boolean success = userFundFlowService.refuseToWithdrawAndReturnBalance(user, amount);
        return success ? Result.success("余额返还成功") : Result.error("余额返还失败");
    }
}