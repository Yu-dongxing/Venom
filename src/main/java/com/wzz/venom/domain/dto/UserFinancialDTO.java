package com.wzz.venom.domain.dto;

import lombok.Data;

/**
 * 用户理财信息数据传输对象
 * 用于新增、修改、查询操作
 */
@Data
public class UserFinancialDTO {

    private String user;
    private Double interestRate;
    private Double amount;
    private Integer status;
}