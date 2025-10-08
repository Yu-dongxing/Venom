package com.wzz.venom.controller.admin;

import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 用户管理接口
 * 模块：用户查询 / 用户更新 / 用户删除 / 用户资金操作
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    /** 查询所有用户 */
    @GetMapping("/list")
    public void queryAllUsers() { }

    /** 查询指定用户信息 */
    @GetMapping("/info")
    public void querySpecifiedUserInformation(@RequestParam String user) { }

    /** 更新用户信息 */
    @PostMapping("/update")
    public void updateUserInformation(@RequestBody Object userInfoDto) { }

    /** 删除用户信息 */
    @PostMapping("/deleteInfo")
    public void deleteUserInformation(@RequestParam String user) { }

    /** 为用户充值 */
    @PostMapping("/recharge")
    public void rechargeForUsers(@RequestParam String user, @RequestParam Double amount) { }

    /** 扣减用户余额 */
    @PostMapping("/reduceBalance")
    public void reduceUserBalance(@RequestParam String user, @RequestParam Double amount) { }

    /** 删除用户 */
    @PostMapping("/delete")
    public void deleteUser(@RequestParam String user) { }
}
