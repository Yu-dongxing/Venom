package com.wzz.venom.enums;

/**
 * 资金流水状态枚举
 */
public enum FundFlowStatusEnum {
    /**
     * 提现申请：用户已提交提现请求，等待审核
     */
    APPLYING,

    /**
     * 审核通过：提现请求已通过审核，等待打款
     */
    APPROVED,

    /**
     * 审核拒绝：提现请求被拒绝
     */
    REJECTED
}