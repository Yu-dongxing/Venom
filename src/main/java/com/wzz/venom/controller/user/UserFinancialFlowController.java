package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserFinancialStatement;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserFinancialStatementService;
import com.wzz.venom.service.user.UserService;
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

    @Autowired
    private UserService userService;
    /** 用户获取理财流水记录 */
    @GetMapping("/list")
    public Result<?> userFinancialFlow() {
        try{
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User u =  userService.queryUserByUserId(userId);
            if (u==null){
                return Result.error("无法查询该用户！");
            }
            List<UserFinancialStatement> ll = userFinancialStatementService.queryTheDesignatedUserSFinancialStatementList(u.getUserName());
            return Result.success("查询成功",ll);
        }catch (BusinessException e){
            return Result.error(e.getMessage());
        }
    }
}