package com.wzz.venom.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 根据ID更新提现状态的请求DTO
 */
@Data
public class UpdateWithdrawalStatusByIdDTO {

    @NotNull(message = "提现记录ID不能为空")
    private Long flowId;

    @NotNull(message = "目标状态码不能为空")
    private Integer status;
}