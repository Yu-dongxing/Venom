package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.dto.UserDTO;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

            // 2. 登录成功，使用 Sa-Token 生成 Token
            // 我们使用 userName 作为 loginId，因为 service 层的方法多是基于 userName 操作的
            StpUtil.login(userDTO.getUserName());
            String tokenValue = StpUtil.getTokenValue();

            // 3. 封装 Token 信息并返回
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("tokenName", StpUtil.getTokenName());
            tokenMap.put("tokenValue", tokenValue);

            return Result.success("登录成功", tokenMap);
        } catch (BusinessException e) {
            log.error("登录业务异常: {}", e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("登录时发生未知错误", e);
            return Result.error(500, "系统繁忙，请稍后再试");
        }
    }

    /**
     * 用户注册
     * @param userDTO 包含注册所需信息的数据传输对象
     * @return 通用响应结果
     */
    @PostMapping("/register")
    public Result<?> userRegistration(@RequestBody UserDTO userDTO) {
        try {
            // 1. 将 DTO 转换为 Entity
            User user = new User();
            user.setUserName(userDTO.getUserName());
            user.setPassword(userDTO.getPassword()); // 注意：实际生产中密码应加密存储
            user.setBankCard(userDTO.getBankCard());
            // 其他字段（如余额、信用分、状态）在 service 的 addUser 方法中设置了默认值

            // 2. 调用服务层进行注册
            boolean success = userService.addUser(user);

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

            // 2. 获取当前登录用户的用户名
            String userName = StpUtil.getLoginIdAsString();

            // 3. 调用服务层修改密码
            boolean success = userService.changeUserPassword(userName, userDTO.getPassword());

            return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
        } catch (BusinessException e) {
            log.warn("修改密码业务异常 for user {}: {}", StpUtil.getLoginId(), e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改密码时发生未知错误 for user " + StpUtil.getLoginId(), e);
            return Result.error(500, "系统繁忙，请稍后再试");
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

            // 2. 获取当前登录用户的用户名
            String userName = StpUtil.getLoginIdAsString();

            // 3. 查询用户详情
            User user = userService.queryUser(userName);

            if (Objects.isNull(user)) {
                return Result.error(404, "无法获取用户信息，请重新登录");
            }

            // 4. 数据脱敏：隐藏密码等敏感信息，防止泄露
            // 在实际项目中，更推荐返回一个 UserVO (View Object) 来替代直接返回实体类
            user.setPassword(null);
            user.setWithdrawalPassword(null);

            return Result.success("获取成功", user);
        } catch (BusinessException e) {
            log.warn("获取用户详情业务异常 for user {}: {}", StpUtil.getLoginId(), e.getMessage());
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取用户详情时发生未知错误 for user " + StpUtil.getLoginId(), e);
            return Result.error(500, "系统繁忙，请稍后再试");
        }
    }
}