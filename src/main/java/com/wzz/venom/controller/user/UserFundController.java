package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.annotation.CheckUserFrozen;
import com.wzz.venom.common.Result; // 假设这是您的统一返回结果类
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserService;
import com.wzz.venom.service.webSocket.WebSocketNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    @Autowired
    private WebSocketNotifyService webSocketNotifyService;

    /**
     * 用户提交充值申请
     * [重大修改] 此接口现在只创建一条"待审核"的记录，资金不会立即到账。
     * @param amount 充值金额
     * @return Result
     */
    @CheckUserFrozen
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
            // 调用新的充值申请服务
            boolean success = userFundFlowService.requestRecharge(u.getUserName(), amount, "用户在线充值申请");
            if (success){
                // 通知管理员有新的充值请求
                webSocketNotifyService.sendUserRechargeNotification(u.getUserName(),amount);
            }
            return success ? Result.success("充值申请提交成功，等待管理员审核") : Result.error("充值申请失败，请稍后重试");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * [新增] (管理员)查询待审核的充值列表
     * @return Result<List<UserFundFlow>>
     */
    @GetMapping("/recharge/pending")
    public Result<List<UserFundFlow>> getPendingRechargeRequests() {
        // 安全注意：此接口应仅对管理员角色开放
        // 例如: StpUtil.checkRole("admin");
        List<UserFundFlow> records = userFundFlowService.getPendingRecharges();
        return Result.success(records);
    }

    /**
     * [新增] (管理员)通过充值申请
     * @param flowId 资金流水ID
     * @return Result
     */
    @PostMapping("/recharge/approve")
    public Result<?> approveRechargeRequest(@RequestParam Long flowId) {
        if (flowId == null || flowId <= 0) {
            return Result.error("无效的流水ID");
        }
        try {
            boolean success = userFundFlowService.approveRecharge(flowId);
            // 可以在此处添加通知用户的逻辑
            return success ? Result.success("充值审核通过，操作成功") : Result.error("审核操作失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * [新增] (管理员)拒绝充值申请
     * @param flowId 资金流水ID
     * @return Result
     */
    @PostMapping("/recharge/refuse")
    public Result<?> refuseRechargeRequest(@RequestParam Long flowId) {
        if (flowId == null || flowId <= 0) {
            return Result.error("无效的流水ID");
        }
        try {
            boolean success = userFundFlowService.refuseRecharge(flowId);
            return success ? Result.success("充值审核拒绝，操作成功") : Result.error("拒绝审核操作失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }


    /**
     * 用户提交提现申请
     */
    @CheckUserFrozen
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
            if (!StringUtils.hasText(u.getRealName()) ||
                    !StringUtils.hasText(u.getBankCard()) ||
                    !StringUtils.hasText(u.getBankBranch()) ||
                    !StringUtils.hasText(u.getBankName())) {
                return Result.error("请先完善您的真实姓名、银行卡号、开户行和银行名称信息后再申请提现");
            }
            boolean success = userFundFlowService.reduceUserTransactionAmountWITHDRA(u.getUserName(), amount, "用户申请提现");
            if (success){
                webSocketNotifyService.sendUserWithdrawalNotification(u.getUserName(),amount);
            }
            return success ? Result.success("提现申请已提交") : Result.error("提现申请失败");
        }catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户查看自己的资金流水
     */
    @GetMapping("/flow")
    public Result<List<UserFundFlow>> userFundFlow() {
        try{
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User u =  userService.queryUserByUserId(userId);
            if (u==null){
                return Result.error("无法查询该用户！");
            }
            List<UserFundFlow> flowList = userFundFlowService.queryTheUserSFundFlowList(u.getUserName());
            return Result.success(flowList);
        }catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * (管理员)查询所有提现交易信息
     */
    @GetMapping("/withdrawRecords")
    public Result<List<UserFundFlow>> queryAllWithdrawalTransactionInformation() {
        List<UserFundFlow> records = userFundFlowService.queryAllWithdrawalTransactionInformation();
        return Result.success(records);
    }

    /**
     * (管理员)修改提现状态
     */
    @PostMapping("/modifyWithdrawStatus")
    public Result<?> modifyUserWithdrawalStatus(@RequestParam String user, @RequestParam Integer status) {
        if (status == null || status < 1 || status > 3) {
            return Result.error("无效的状态码");
        }
        boolean success = userFundFlowService.modifyUserWithdrawalStatus(user, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败，可能未找到处理中的提现记录");
    }

    /**
     * (管理员)增加用户交易金额
     */
    @PostMapping("/increase")
    public Result<?> increaseUserTransactionAmount(@RequestParam String user,
                                                   @RequestParam Double amount,
                                                   @RequestParam String describe) {
        if (amount <= 0) {
            return Result.error("增加的金额必须大于0");
        }
        boolean success = userFundFlowService.increaseUserTransactionAmount(user, amount, describe);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    /**
     * (管理员)减少用户交易金额
     */
    @PostMapping("/reduce")
    public Result<?> reduceUserTransactionAmount(@RequestParam String user,
                                                 @RequestParam Double amount,
                                                 @RequestParam String describe) {
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
     */
    @PostMapping("/refuse")
    public Result<?> refuseToWithdrawAndReturnBalance(@RequestParam String user, @RequestParam Double amount) {
        if (amount <= 0) {
            return Result.error("返还的金额必须大于0");
        }
        boolean success = userFundFlowService.refuseToWithdrawAndReturnBalance(user, amount);
        return success ? Result.success("余额返还成功") : Result.error("余额返还失败");
    }
}