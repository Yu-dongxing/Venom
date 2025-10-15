package com.wzz.venom.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.DefaultValue;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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


    /**
     * 真实姓名。
     * 该字段用于存储用户的实际姓名，通常在需要进行身份验证或法律文件签署等正式场合使用。
     *
     * @ColumnComment("真实姓名")
     * @TableField("real_name")
     */
    @ColumnComment("真实姓名")
    @TableField("real_name")
    private String realName;

    /**
     * 银行名称。
     */
    @ColumnComment("银行名称")
    @TableField("bank_name")
    private String bankName;

    /**
     * 开户行信息。
     * 该字段用于存储用户的银行开户行名称，通常在处理与银行相关的事务时需要使用此信息。
     *
     * @ColumnComment("开户行")
     * @TableField("bank_branch")
     */
    @ColumnComment("开户行")
    @TableField("bank_branch")
    private String bankBranch;
    // --- [新增结束] ---

    /** 银行卡号 */
    @ColumnComment("银行卡号")
    @TableField("bank_card")
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

    /** [新增] 是否冻结 (0-否, 1-是) */
    @ColumnComment("是否冻结 (0-否, 1-是)")
    @TableField("is_frozen")
    @DefaultValue("0")
    private Boolean isFrozen;
}