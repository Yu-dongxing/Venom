package com.wzz.venom.enums;

/**
 * 用户账户状态枚举
 */
public enum UserStatusEnum {
    /**
     * 正常：账户可以正常登录和使用所有功能
     */
    NORMAL,

    /**
     * 冻结：账户被临时禁用，无法登录或进行操作，通常可由管理员解冻
     */
    FROZEN,

    /**
     * 注销：账户已被永久删除，所有数据清空，无法恢复
     */
    CANCELLED
}