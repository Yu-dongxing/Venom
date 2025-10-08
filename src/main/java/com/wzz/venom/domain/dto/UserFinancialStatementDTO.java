package com.wzz.venom.domain.dto;

import lombok.Data;

/**
 * 用户理财流水数据传输对象
 * 用于新增、修改、查询操作
 */
@Data
public class UserFinancialStatementDTO {

    private String user;
    private Double amount;
}
