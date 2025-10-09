package com.wzz.venom.domain.dto;

import lombok.Data;

/**
 * 用户数据传输对象
 * 用于新增、更新、查询等操作
 */
@Data
public class UserDTO {

    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 提现密码
     */
    private String withdrawalPassword;
    /**
     * 银行卡号
     */
    private String bankCard;
    /**
     * 余额
     */
    private Double balance;
    /**
     *信用分数
     */
    private Integer creditScore;
    /** 账户状态（0-正常 1-冻结 2-注销） */
    private Integer accountStatus;
}