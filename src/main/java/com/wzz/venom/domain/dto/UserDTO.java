package com.wzz.venom.domain.dto;

import lombok.Data;

/**
 * 用户数据传输对象
 * 用于新增、更新、查询等操作
 */
@Data
public class UserDTO {

    private String userName;
    private String password;
    private String withdrawalPassword;
    private String bankCard;
    private Double balance;
    private Integer creditScore;
    private Integer accountStatus;
}