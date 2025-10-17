package com.wzz.venom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于检查当前登录用户是否被冻结。
 * <p>
 * 当此注解被应用到一个方法上时，会通过AOP切面在方法执行前
 * 检查当前登录用户的'isFrozen'状态。如果用户被冻结，
 * 将会抛出 BusinessException 异常。
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME) // 确保注解在运行时可用
@Target(ElementType.METHOD)         // 注解只能用于方法上
public @interface CheckUserFrozen {
}