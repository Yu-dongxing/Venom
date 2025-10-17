package com.wzz.venom.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 检查用户冻结状态的 AOP 切面
 */
@Aspect
@Component
public class CheckUserFrozenAspect {

    @Autowired
    private UserService userService;

    /**
     * 定义一个切点，匹配所有被 @CheckUserFrozen 注解标记的方法。
     */
    @Pointcut("@annotation(com.wzz.venom.annotation.CheckUserFrozen)")
    public void checkUserFrozenPointcut() {
    }

    /**
     * 在切点方法执行前执行此通知。
     */
    @Before("checkUserFrozenPointcut()")
    public void doBefore() {
        // 1. 确保用户已登录 (通常 Controller 层会调用 StpUtil.checkLogin()，这里可以作为双重保险)
        if (!StpUtil.isLogin()) {
            // Sa-Token 的全局异常处理器会处理未登录的情况，这里可以信赖它。
            // 如果想自定义异常，也可以在这里抛出。
            return;
        }

        // 2. 获取当前登录用户的ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 3. 查询用户信息
        User user = userService.queryUserByUserId(userId);
        if (Objects.isNull(user)) {
            // 正常情况下，已登录的用户都能查到信息。查不到可能是数据异常。
            throw new BusinessException(0, "无法获取用户信息，请重新登录");
        }

        // 4. 核心逻辑：检查冻结状态
        if (Boolean.TRUE.equals(user.getIsFrozen())) {
            // 如果用户被冻结，直接抛出业务异常，请求将被中断
            throw new BusinessException(0, "当前用户已被冻结");
        }
    }
}