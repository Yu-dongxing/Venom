package com.wzz.venom.controller.admin;

import org.springframework.web.bind.annotation.*;

/**
 * 管理后台 - 提现与资金管理接口
 * 模块：提现查询 / 更新提现信息
 */
@RestController
@RequestMapping("/api/admin/withdrawal")
public class AdminWithdrawalController {

    /** 查询所有提现列表 */
    @GetMapping("/list")
    public void findAllWithdrawalLists() { }

    /** 查询指定用户的提现记录 */
    @GetMapping("/user")
    public void searchForTheWithdrawalListOfTheSpecifiedUser(@RequestParam String user) { }

    /** 更新用户提现信息 */
    @PostMapping("/update")
    public void updateUserWithdrawalInformation(@RequestBody Object fundFlowPojo) { }
}

