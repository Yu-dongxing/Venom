package com.wzz.venom.controller.user;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserFinancialStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户理财流水接口控制器
 * 模块：查询理财流水记录
 */
@RestController
@RequestMapping("/api/user/financialFlow")
public class UserFinancialFlowController {
    @Autowired
    private UserFinancialStatementService userFinancialStatementService;
    /** 用户获取理财流水记录 */
    @GetMapping("/list")
    public Result<?> userFinancialFlow(@RequestParam String user) {
        try{
            List<UserFinancialStatement> ll = userFinancialStatementService.queryTheDesignatedUserSFinancialStatementList(user);
            return Result.success("查询成功",ll);
        }catch (BusinessException e){
            return Result.error(e.getMessage());
        }
    }
}