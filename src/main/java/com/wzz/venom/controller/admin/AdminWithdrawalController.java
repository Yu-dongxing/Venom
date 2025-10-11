package com.wzz.venom.controller.admin;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.dto.UpdateWithdrawalStatusByIdDTO;
import com.wzz.venom.domain.dto.UpdateWithdrawalStatusDTO;
import com.wzz.venom.domain.entity.UserFundFlow;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserFundFlowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 提现与资金管理接口
 * 模块：提现查询 / 更新提现信息
 */
@RestController
@RequestMapping("/api/admin/withdrawal")
public class AdminWithdrawalController {

    @Autowired
    private UserFundFlowService userFundFlowService;

    private static final String FUND_TYPE_WITHDRAW = "WITHDRAW";

    /**
     * 查询所有提现列表
     * @return 返回所有 fund_type 为 'WITHDRAW' 的资金流水记录
     */
    @GetMapping("/list")
    public Result<?> findAllWithdrawalLists() {
        try {
            List<UserFundFlow> withdrawalList = userFundFlowService.queryAllWithdrawalTransactionInformation();
            return Result.success(withdrawalList);
        } catch (Exception e) {
            // 在实际项目中，建议使用全局异常处理器
            // log.error("查询所有提现列表失败", e);
            return Result.error("服务器内部错误，查询失败！");
        }
    }

    /**
     * 查询指定用户的提现记录
     * @param user 用户名
     * @return 返回指定用户的提现记录列表
     */
    @GetMapping("/user")
    public Result<?> searchForTheWithdrawalListOfTheSpecifiedUser(@RequestParam String user) {
        if (!StringUtils.hasText(user)) {
            return Result.error(400, "用户名不能为空");
        }
        try {
            // 调用通用的查询用户流水服务
            List<UserFundFlow> allFlows = userFundFlowService.queryTheUserSFundFlowList(user);

            // 在内存中过滤出提现记录
            // 优化建议：如果数据量大，可以在Service层增加一个专用的查询方法，直接从数据库筛选
            List<UserFundFlow> withdrawalFlows = allFlows.stream()
                    .filter(flow -> FUND_TYPE_WITHDRAW.equals(flow.getFundType()))
                    .collect(Collectors.toList());

            return Result.success(withdrawalFlows);
        } catch (Exception e) {
            // log.error("查询用户 {} 的提现记录失败", user, e);
            return Result.error("服务器内部错误，查询失败！");
        }
    }

    /**
     * 更新用户提现信息（审核通过或拒绝）
     * @param dto 包含用户名和新状态的请求体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result<?> updateUserWithdrawalInformation(@RequestBody UpdateWithdrawalStatusDTO dto) {
        // 1. 参数校验
        if (dto == null || !StringUtils.hasText(dto.getUserName()) || dto.getStatus() == null) {
            return Result.error("请求参数不完整");
        }

        // 建议对 status 的值进行合法性校验，例如只允许传入 2 或 3
        if (dto.getStatus() != 2 && dto.getStatus() != 3) {
            return Result.error("无效的状态值");
        }

        try {
            // 2. 调用Service层方法
            boolean success = userFundFlowService.modifyUserWithdrawalStatus(dto.getUserName(), dto.getStatus());

            // 3. 根据Service层返回结果，封装响应
            if (success) {
                return Result.success("提现状态更新成功");
            } else {
                return Result.error("更新失败，可能未找到该用户待处理的提现申请");
            }
        } catch (Exception e) {
            // 如果Service层抛出异常（如余额不足），这里可以捕获
            // log.error("更新用户 {} 的提现状态失败", dto.getUserName(), e);
            return Result.error(e.getMessage()); // 将业务异常信息返回给前端
        }
    }
    /**
     * 根据提现记录id，执行提现通过以及拒绝的接口
     * @param dto 包含提现记录ID (flowId) 和新状态 (status) 的请求体
     * @return 操作结果
     */
    @PostMapping("/updateById")
    public Result<?> updateWithdrawalStatusById(@Valid @RequestBody UpdateWithdrawalStatusByIdDTO dto) {
        // 1. 参数校验已通过 DTO 中的注解完成

        // 2. 状态值合法性校验 (在Service层也有校验，这里是Controller层的快速失败)
        // 根据Service层的常量，0=通过, 2=拒绝
        final int STATUS_APPROVED = 0;
        final int STATUS_REJECTED = 2;
        if (dto.getStatus() != STATUS_APPROVED && dto.getStatus() != STATUS_REJECTED) {
            return Result.error(400, "无效的状态值，必须是 " + STATUS_APPROVED + " (通过) 或 " + STATUS_REJECTED + " (拒绝)");
        }

        try {
            // 3. 调用Service层方法
            userFundFlowService.modifyWithdrawalStatusById(dto.getId(), dto.getStatus());

            // 4. 封装成功响应
            String action = dto.getStatus() == STATUS_APPROVED ? "通过" : "拒绝";
            return Result.success("提现申请审核成功，操作：" + action);

        } catch (BusinessException e) {
            // 捕获Service层主动抛出的业务异常，将信息返回给前端
            // log.error("审核提现[ID:{}]失败: {}", dto.getFlowId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他未知异常，防止敏感信息泄露
            // log.error("审核提现[ID:{}]时发生系统错误", dto.getFlowId(), e);
            return Result.error("服务器内部错误，操作失败！");
        }
    }
}