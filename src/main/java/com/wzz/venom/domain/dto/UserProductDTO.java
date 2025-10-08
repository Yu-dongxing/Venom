package com.wzz.venom.domain.dto;

import lombok.Data;

/**
 * 用户产品数据传输对象
 */
@Data
public class UserProductDTO {

    private String user;
    private String productName;
    private Integer productType;
    private Double amount;
    private Double interestRate;
    private Integer status;
}
