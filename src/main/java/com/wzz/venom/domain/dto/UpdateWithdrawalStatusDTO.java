package com.wzz.venom.domain.dto;
import lombok.Data;

/**
 * 更新用户提现状态的请求数据传输对象
 */
@Data
public class UpdateWithdrawalStatusDTO {
    /**
     * 需要操作的用户名
     */
    private String userName;

    /**
     * 要更新的状态
     * 业务中定义：2-提现申请通过（审核通过），3-提现申请拒绝
     */
    private Integer status;
}