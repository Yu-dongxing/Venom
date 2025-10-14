package com.wzz.venom.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.dto.AdminUserDto;
import com.wzz.venom.domain.dto.UserAmountDto;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.admin.AdminUserService;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.lang.Double.parseDouble;

/**
 * 管理后台 - 用户管理接口
 * 模块：用户查询 / 用户更新 / 用户删除 / 用户资金操作/管理员用户登录/管理员用户注册
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserFundFlowService userFundFlowService;

    /**
     * 查询所有用户
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<?> queryAllUsers() {
        List<User> userList = userService.queryUserList();
        return Result.success(userList);
    }

    /**
     * 查询指定用户信息
     * @param userName 用户名
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<?> querySpecifiedUserInformation(@RequestParam("user") String userName) {
        User user = userService.queryUser(userName);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     * 注意：根据现有的 Service 实现，此接口仅能更新用户的银行卡号。
     * @param user 包含用户ID(id)和要更新的银行卡号(bankCard)等信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result<?> updateUserInformation(@RequestBody User user) {
        try {
            boolean success = userService.updateUserInformation(user);
            return success ? Result.success() : Result.error("用户信息更新失败");
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     * @param userName 用户名
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<?> deleteUser(@RequestParam("user") String userName) {
        try {
            boolean success = userService.deleteUser(userName);
            return success ? Result.success() : Result.error("用户删除失败");
        } catch (BusinessException e) {
            log.error("删除用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为用户充值 (逻辑已优化, 接口已修改为接收 JSON)
     * @param dto 包含用户名和金额的数据传输对象
     * @return 操作结果
     */
    @PostMapping("/recharge")
    // 【修改点】: 使用 @RequestBody 和 DTO
    public Result<?> rechargeForUsers(@RequestBody UserAmountDto dto) {
        log.info("管理员调用充值接口：{}，{}", dto.getUser(), dto.getAmount());

        // 参数校验
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            return Result.error("充值金额必须为正数");
        }

        try {
            boolean success =  userFundFlowService.increaseUserTransactionAmount(dto.getUser(), dto.getAmount(), "管理员后台充值");
            return success ? Result.success() : Result.error("充值失败");
        } catch (BusinessException e) {
            log.error("为用户 {} 充值 {} 失败", dto.getUser(), dto.getAmount(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("为用户 {} 充值 {} 失败", dto.getUser(), dto.getAmount(), e);
            return Result.error("充值失败，发生未知错误");
        }
    }

    /**
     * 扣减用户余额 (逻辑已优化, 接口已修改为接收 JSON)
     * @param dto 包含用户名和金额的数据传输对象
     * @return 操作结果
     */
    @PostMapping("/reduceBalance")
    public Result<?> reduceUserBalance(@RequestBody UserAmountDto dto) {
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            return Result.error("扣减金额必须为正数");
        }
        try {
            boolean success = userFundFlowService.reduceUserTransactionAmount(dto.getUser(), dto.getAmount(), "管理员后台扣款");
            return success ? Result.success() : Result.error("扣减余额失败");
        } catch (BusinessException e) {
            log.error("扣减用户 {} 余额 {} 失败", dto.getUser(), dto.getAmount(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("扣减用户 {} 余额 {} 失败", dto.getUser(), dto.getAmount(), e);
            return Result.error("扣减余额失败，发生未知错误");
        }
    }

    /*
     * 备注:
     * 1. 原始 Controller 模板中的 /deleteInfo 接口与 /delete 接口功能高度重合，
     *    在实际开发中通常会合并为一个，此处予以忽略，仅实现 /delete 作为逻辑删除。
     * 2. 当前的 updateUserInformation 服务层方法功能较弱（只能修改银行卡），
     *    在实际项目中，可能需要一个更通用的更新方法，或者提供更多特定字段的修改接口（如修改信用分、冻结/解冻账户等）。
     */

    @Autowired
    private AdminUserService adminUserService;
    /**
     * 管理员用户登录
     */
    @PostMapping("/login")
    public Result<?> adminLogin(@RequestBody AdminUserDto adminUserDto){
        try {
            String is = adminUserService.adminLogin(adminUserDto);
            return Result.success("登录成功，返回token",is);

        }catch (BusinessException e){
            return Result.error(e.getMessage());
        }
    }
    /**
     * 管理员用户注册
     */
    @PostMapping("/regist")
    public Result<?> adminRegist(@RequestBody AdminUserDto adminUserDto){
        Boolean is =  adminUserService.adminRegist(adminUserDto);
        if (is){
            return Result.success("管理员用户注册成功！");
        }
        return Result.error("用户注册失败");
    }
}