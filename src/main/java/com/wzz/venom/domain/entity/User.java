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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String password;

    /** 提现密码 */
    @ColumnComment("提现密码")
    @TableField("withdrawal_password")
    private String withdrawalPassword;

    /** 银行卡号 */
    @ColumnComment("银行卡号")
    @TableField("bank_card")
    private String bankCard;

    /** 账户余额 */
    @ColumnComment("账户余额")
    @TableField("balance")
    private Double balance;

    /** 信用分数 */
    @ColumnComment("信用分数")
    @TableField("credit_score")
    private Integer creditScore;


    /** 账户状态（0-正常 1-冻结 2-注销） */
    @ColumnComment("账户状态（0-正常 1-冻结 2-注销）")
    @TableField("account_status")
    private Integer accountStatus;

    /**
     * 用户状态 (NORMAL-正常/FROZEN-冻结/CANCELLED-注销)
     */
    @ColumnComment("用户状态 (NORMAL-正常/FROZEN-冻结/CANCELLED-注销)")
    @TableField("status")
    @DefaultValue("'NORMAL'") // 2. 添加默认值 'NORMAL' (SQL中为字符串)
    private UserStatusEnum status; // 3. 添加 status 字段，类型为 UserStatusEnum

}