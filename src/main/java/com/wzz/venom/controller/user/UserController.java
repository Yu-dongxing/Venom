package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.dto.UserDTO;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.config.SysConfigService;
import com.wzz.venom.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 用户核心接口控制器
 * 模块：用户账号 / 注册 / 登录 / 密码修改 / 用户详情
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {


    private static final String CONFIG_NAME_SYS = "sys_config";
    private static final String CONFIG_KEY_FINANCIAL = "financial_management";
    private static final String CONFIG_KEY_ANNOUNCEMENT = "platform_announcement";

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param userDTO 包含用户名和密码的数据传输对象
     * @return 包含token的通用响应结果
     */
    @PostMapping("/login")
    public Result<?> userLogin(@RequestBody UserDTO userDTO) {
        try {
            // 1. 校验用户名和密码
            boolean isValid = userService.verifyUserPassword(userDTO.getUserName(), userDTO.getPassword());
            if (!isValid) {
                return Result.error("用户名或密码错误");
            }
            /**
             * 根据用户名查询用户
             */
            User u = userService.selectByUserName(userDTO);
            if (u==null){
                return Result.error("没有查询到该用户消息");
            }

            // 2. 登录成功，使用 Sa-Token 生成 Token
            // 我们使用 userName 作为 loginId，因为 service 层的方法多是基于 userName 操作的
            StpUtil.login(u.getId());
            String tokenValue = StpUtil.getTokenValue();

            // 3. 封装 Token 信息并返回
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("tokenName", StpUtil.getTokenName());
            tokenMap.put("tokenValue", tokenValue);

            return Result.success("登录成功", tokenMap);
        } catch (BusinessException e) {
            log.error("登录业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("登录时发生未知错误", e);
            return Result.error(500, "登录时发生错误");
        }
    }

    /**
     * 用户注册
     * @param userDTO 包含注册所需信息的数据传输对象
     * @return 通用响应结果
     */
    @PostMapping("/register")
    public Result<?> userRegistration(@RequestBody UserDTO userDTO) {
        if(userDTO.getInvitationCode()==null){
            return Result.error("邀请码不能为空！");
        }
        try {
            User user = new User();
            user.setUserName(userDTO.getUserName());
            user.setPassword(userDTO.getPassword());
            // 移除 setBankCard 逻辑，注册时不再需要银行卡号
            // user.setBankCard(userDTO.getBankCard());
            user.setWithdrawalPassword(userDTO.getWithdrawalPassword());
            user.setCreditScore(userDTO.getCreditScore());
            boolean success = userService.addUserByCode(user,userDTO.getInvitationCode());

            return success ? Result.success("注册成功") : Result.error("注册失败，请重试");
        } catch (BusinessException e) {
            log.warn("注册业务异常: {}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("注册时发生未知错误", e);
            return Result.error(500, "系统繁忙，请稍后再试");
        }
    }

    /**
     * 用户修改登录密码
     * @param userDTO 包含新密码的数据传输对象
     * @return 通用响应结果
     */
    @PostMapping("/changePassword")
    public Result<?> userChangesLoginPassword(@RequestBody UserDTO userDTO) {
        try {
            // 1. 确认用户已登录，否则 Sa-Token 会抛出异常
            StpUtil.checkLogin();

            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.queryUserByUserId(userId);
            if (Objects.isNull(user)) {
                return Result.error(404, "无法获取用户信息，请重新登录");
            }

            if (!Objects.isNull(userDTO.getPassword())) {
                boolean success = userService.changeUserPassword(user.getUserName(), userDTO.getPassword());
                return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
            } else if (!Objects.isNull(userDTO.getWithdrawalPassword())) {
                boolean success = userService.changeUserWithdrawalPassword(user.getUserName(), userDTO.getWithdrawalPassword());
                return success ? Result.success("提现密码修改成功") : Result.error("提现密码修改失败");
            }else {
                return Result.error("输入参数不正确");
            }

        } catch (BusinessException e) {
            log.warn("修改密码业务异常 for user {}: {}", StpUtil.getLoginId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码时发生未知错误 for user " + StpUtil.getLoginId(), e);
            return Result.error("修改密码时发生未知错误");
        }
    }

    /**
     * 获取当前登录用户的详情
     * @return 包含用户信息的通用响应结果
     */
    @GetMapping("/detail")
    public Result<User> userObtainsUserDetails() {
        try {
            // 1. 确认用户已登录
            StpUtil.checkLogin();
            Long userId  = StpUtil.getLoginIdAsLong();
            User user = userService.queryUserByUserId(userId);
            if (Objects.isNull(user)) {
                return Result.error(404, "无法获取用户信息，请重新登录");
            }
            return Result.success("获取成功", user);
        } catch (BusinessException e) {
            log.warn("获取用户详情业务异常 for user {}: {}", StpUtil.getLoginId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取用户详情时发生未知错误 for user " + StpUtil.getLoginId(), e);
            return Result.error("获取用户详情时发生未知错误");
        }
    }
    @Autowired
    private SysConfigService sysConfigService;
    /**
     * 获取理财的利率
     */
    @GetMapping("/financial")
    public Result<?> getFin(){
        Object rateValue = sysConfigService.getConfigValueByNameAndKey("sys_config", "financial_management");
        if (rateValue==null){
            return Result.success("系统中没有配置","0.2");
        }
        return Result.success("获取系统配置", rateValue);
    }

    /**
     *获取公告
     */
    /**
     * 【新增】查询平台公告
     */
    @GetMapping("/announcement")
    public Result<?> getAnnouncement() {
        Object announcementValue = sysConfigService.getConfigValueByNameAndKey(CONFIG_NAME_SYS, CONFIG_KEY_ANNOUNCEMENT);
        if (announcementValue == null || !StringUtils.hasText(announcementValue.toString())) {
            return Result.success("系统中没有配置公告", "暂无公告");
        }
        return Result.success("获取平台公告成功", announcementValue);
    }
    /**
     * 【新增】用户首次绑定银行卡
     * @param userDTO 包含 bankCard 的数据传输对象
     * @return 通用响应结果
     */
    @PostMapping("/addBankCard")
    public Result<?> addBankCard(@RequestBody UserDTO userDTO) {
        try {
            // 1. 确认用户已登录
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();

            // 2. 调用业务层方法
            boolean success = userService.addBankCardForUser(userId, userDTO.getBankCard());
            return success ? Result.success("银行卡绑定成功") : Result.error("银行卡绑定失败");

        } catch (BusinessException e) {
            log.warn("绑定银行卡业务异常 for user {}: {}", StpUtil.getLoginId(), e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("绑定银行卡时发生未知错误 for user " + StpUtil.getLoginId(), e);
            return Result.error("系统繁忙，请稍后再试");
        }
    }
}