package com.wzz.venom.domain.dto;

import lombok.Data;

/**
 * 用户资金流水数据传输对象
 * 用于新增、修改、查询操作
 */
@Data
public class UserFundFlowDTO {

    private String user;
    private Double amount;
    private String fundType;
    private Double balance;
    private Integer status;
    private String describe;
}
