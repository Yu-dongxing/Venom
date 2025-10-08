package com.wzz.venom.controller.user;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.service.user.UserFundFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户资金账本接口控制器
 * 模块：充值 / 提现 / 账本查询 / 提现状态变更
 */
@RestController
@RequestMapping("/api/user/fund")
public class UserFundController {

    private final UserFundFlowService userFundFlowService;

    // 使用构造函数注入Service
    @Autowired
    public UserFundController(UserFundFlowService userFundFlowService) {
        this.userFundFlowService = userFundFlowService;
    }

    /**
     * 用户提交充值申请
     * 对应业务：记录一笔正向流水
     */
    @PostMapping("/recharge")
    public Result<?> userSubmitsRechargeRequest(@RequestParam String user, @RequestParam Double amount) {
        // 调用通用的增加金额接口，并明确描述
        boolean success = userFundFlowService.increaseUserUnderstandingOfTransactionAmount(user, amount);
        return success ? Result.success("充值申请成功，待处理") : Result.error("充值申请失败");
    }

    /**
     * 用户提交提现申请
     * 对应业务：记录一笔负向流水，状态通常为“处理中”
     */
    @PostMapping("/withdraw")
    public Result<?> userSubmitsWithdrawalRequest(@RequestParam String user, @RequestParam Double amount) {
        // 注意：这里的业务与简单的“减少金额”不同，它有状态。
        // 我们需要手动构建一个状态为“处理中”的流水记录。
        if (amount <= 0) {
            return Result.error("提现金额必须为正数");
        }

        // TODO: 实际业务中，此处应先查询并冻结用户余额。
        // 为简化，我们直接记录流水。

        UserFundFlow withdrawFlow = new UserFundFlow();
        withdrawFlow.setUserName(user);
        withdrawFlow.setAmount(BigDecimal.valueOf(amount).negate()); // 提现是负向流水
        withdrawFlow.setFundType("WITHDRAW"); // 类型：提现
        withdrawFlow.setDescription("用户发起提现申请");
        withdrawFlow.setStatus(1); // 状态：1-处理中

        // 使用通用的新增接口来创建这条特定状态的记录
        boolean success = userFundFlowService.addUserFinancialStatements(withdrawFlow);
        return success ? Result.success("提现申请已提交") : Result.error("提现申请失败");
    }

    /**
     * 用户查看资金流水
     * 对应业务：查询指定用户的所有流水记录
     */
    @GetMapping("/flow")
    public Result<List<UserFundFlow>> userFundFlow(@RequestParam String user) {
        List<UserFundFlow> flowList = userFundFlowService.queryTheDesignatedUserSFinancialStatementList(user);
        return Result.success(flowList);
    }

    /**
     * 查询所有提现交易信息
     * 对应业务：需要Service层支持按类型查询，此处为演示
     */
    @GetMapping("/withdrawRecords")
    public Result<?> queryAllWithdrawalTransactionInformation() {
        // TODO: 当前的Service层没有提供根据fundType查询的方法。
        // 在实际开发中，你需要在Service和Mapper中添加一个类似 queryByFundType("WITHDRAW") 的方法。
        // 此处返回一个提示信息。
        return Result.error("该功能尚未实现，请在Service层添加按类型查询流水的方法");
    }

    /**
     * 修改提现状态 (例如：由管理员审核通过或拒绝)
     * @param transactionId 提现流水的唯一ID
     * @param status 目标状态 (例如: 0-成功, 2-失败)
     */
    @PostMapping("/modifyWithdrawStatus")
    public Result<?> modifyUserWithdrawalStatus(@RequestParam Long transactionId,
                                                @RequestParam Integer status) {
        // 构建一个只包含主键和要更新字段的对象
        UserFundFlow statementToUpdate = new UserFundFlow();
        statementToUpdate.setId(transactionId); // BaseEntity中的id
        statementToUpdate.setStatus(status);

        boolean success = userFundFlowService.updateUserFinancialStatements(statementToUpdate);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败，请检查流水ID是否存在");
    }

    /**
     * 增加用户交易金额 (例如：活动奖励、手动补款)
     */
    @PostMapping("/increase")
    public Result<?> increaseUserTransactionAmount(@RequestParam String user,
                                                   @RequestParam Double amount,
                                                   @RequestParam String describe) {
        // 直接调用Service中的增加金额方法
        // 注意：原service方法没有describe参数，如果需要记录，需要手动构建对象
        if (amount <= 0) {
            return Result.error("增加的金额必须为正数");
        }

        // TODO: 查询用户当前余额
        BigDecimal currentBalance = BigDecimal.ZERO;

        UserFundFlow flow = new UserFundFlow();
        flow.setUserName(user);
        flow.setAmount(BigDecimal.valueOf(amount));
        flow.setFundType("MANUAL_INCREASE"); // 类型：手动增加
        flow.setDescription(describe); // 使用接口传入的描述
        flow.setStatus(0); // 状态：0-成功
        flow.setBalance(currentBalance.add(flow.getAmount()));

        boolean success = userFundFlowService.addUserFinancialStatements(flow);
        return success ? Result.success("操作成功") : Result.error("增加金额失败");
    }

    /**
     * 减少用户交易金额 (例如：手动扣款)
     */
    @PostMapping("/reduce")
    public Result<?> reduceUserTransactionAmount(@RequestParam String user,
                                                 @RequestParam Double amount,
                                                 @RequestParam String describe) {
        // 直接调用Service中的减少金额方法
        boolean success = userFundFlowService.reduceUserUnderstandingOfTransactionAmounts(user, amount);
        // 注意：原service方法同样没有describe参数。如果需要记录描述，应参照上面的 increase 方法，手动构建对象。
        return success ? Result.success("操作成功") : Result.error("减少金额失败，可能余额不足");
    }

    /**
     * 拒绝提现并返还余额
     * 这是一个组合操作：1. 更新提现记录状态为失败 2. 增加一笔正向流水把钱还给用户
     * 这个操作应该由Service层的一个事务方法来保证原子性。
     * @param transactionId 被拒绝的提现流水ID
     * @param user 用户名
     * @param amount 返还的金额
     */
    @PostMapping("/refuse")
    public Result<?> refuseToWithdrawAndReturnBalance(@RequestParam Long transactionId,
                                                      @RequestParam String user,
                                                      @RequestParam Double amount) {
        // TODO: 这是一个典型的事务场景，强烈建议在Service层创建一个新的方法，
        // 如 `refuseWithdrawal(Long transactionId)` 来封装以下两个操作。

        // 步骤1：更新原提现记录状态为“失败”
        UserFundFlow statementToUpdate = new UserFundFlow();
        statementToUpdate.setId(transactionId);
        statementToUpdate.setStatus(2); // 2-失败
        boolean updateSuccess = userFundFlowService.updateUserFinancialStatements(statementToUpdate);

        if (!updateSuccess) {
            return Result.error("更新提现记录状态失败，请检查流水ID");
        }

        // 步骤2：增加一笔正向流水，将冻结的金额返还给用户
        UserFundFlow refundFlow = new UserFundFlow();
        refundFlow.setUserName(user);
        refundFlow.setAmount(BigDecimal.valueOf(amount));
        refundFlow.setFundType("REFUND"); // 类型：退款/返还
        refundFlow.setDescription("提现申请被拒绝，金额返还。关联流水ID: " + transactionId);
        refundFlow.setStatus(0); // 成功
        // TODO: 此处需要重新计算用户余额

        boolean refundSuccess = userFundFlowService.addUserFinancialStatements(refundFlow);

        if (!refundSuccess) {
            // 这是一个危险的状态，原记录已改，但返款失败了。
            // 这就是为什么必须使用事务方法的原因。
            return Result.error(500, "严重错误：提现状态已更新，但金额返还失败！请联系技术人员处理。");
        }

        return Result.success("提现已拒绝，金额已返还。");
    }
}