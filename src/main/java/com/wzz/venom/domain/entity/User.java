package com.wzz.venom.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.ColumnType;
import com.wzz.venom.annotation.DefaultValue;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity;
import com.wzz.venom.enums.UserStatusEnum;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("b_user")
@TableComment("用户表")
public class User extends BaseEntity {
    /** 用户名 */
    @ColumnComment("用户名")
    @TableField("user_name")
    private String userName;

    /** 登录密码 */
    @ColumnComment("登录密码")
    @TableField("password")
    @JsonIgnore
    private String password;

    /** 提现密码 */
    @ColumnComment("提现密码")
    @TableField("withdrawal_password")
    @JsonIgnore
    private String withdrawalPassword;

    /** 银行卡号 */
    @ColumnComment("银行卡号")
    @TableField("bank_card")
    @DefaultValue("0")
    private String bankCard;

    /** 账户余额 */
    @ColumnComment("账户余额")
    @TableField("balance")
    @DefaultValue("0")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")//json返回时保留两位
    private BigDecimal balance;

    /** 信用分数 */
    @ColumnComment("信用分数")
    @TableField("credit_score")
    @DefaultValue("0")
    private Integer creditScore;

    /**
     * 是否有理财转出的权限
     */
    @TableField("if_out")
    @ColumnComment("是否有理财转出的权限")
    @DefaultValue("0")
    private Boolean ifOut;


    /** 账户状态（0-正常 1-冻结 2-注销） */
    @ColumnComment("账户状态（0-正常 1-冻结 2-注销）")
    @TableField("account_status")
    @DefaultValue("0")
    private Integer accountStatus;


}