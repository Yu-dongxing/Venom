package com.wzz.venom.controller.user;

import org.springframework.web.bind.annotation.*;

/**
 * 用户理财流水接口控制器
 * 模块：查询理财流水记录
 */
@RestController
@RequestMapping("/api/user/financialFlow")
public class UserFinancialFlowController {

    /** 用户获取理财流水记录 */
    @GetMapping("/list")
    public void userFinancialFlow(@RequestParam String user) { }
}