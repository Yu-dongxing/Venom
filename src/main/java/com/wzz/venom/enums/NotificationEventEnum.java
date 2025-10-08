package com.wzz.venom.enums;

/**
 * WebSocket通知事件类型枚举
 */
public enum NotificationEventEnum {
    /**
     * 系统公告：由系统发出的全局通知
     */
    SYSTEM_ANNOUNCEMENT,

    /**
     * 提现结果：通知用户提现申请的处理结果（通过或拒绝）
     */
    WITHDRAWAL_RESULT,

    /**
     * 产品到期：通知用户其购买的产品已到期
     */
    PRODUCT_EXPIRATION,

    /**
     * 账户异动：通知用户账户发生登录异常、密码修改等安全事件
     */
    ACCOUNT_ACTIVITY
}