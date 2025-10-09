package com.wzz.venom.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.DefaultValue;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity; // 假设您有类似的基类
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal; // 导入 BigDecimal 类
import java.time.LocalDateTime;

/**
 * 用户理财信息实体类
 * 对应数据库表：user_financial
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_financial")
@TableComment("用户理财信息表")
public class UserFinancial extends BaseEntity {

    /** 用户名 */
    @ColumnComment("用户名")
    @TableField("user_name")
    private String userName;

    /** 理财利率 */
    @ColumnComment("理财利率")
    @TableField("interest_rate")
    @DefaultValue("0")
    private BigDecimal interestRate;

    /** 理财金额 */
    @ColumnComment("理财金额")
    @TableField("amount")
    @DefaultValue("0")
    private BigDecimal amount; // 强烈建议使用 BigDecimal 处理金额，避免精度问题

    /** 状态（0-持有中，1-已赎回） */
    @ColumnComment("状态（0-持有中，1-已赎回）")
    @TableField("status")
    @DefaultValue("0")
    private Integer status;

    /** 状态哈希（数据完整性校验） */
    @ColumnComment("状态哈希（数据完整性校验）")
    @TableField("state_hash")
    private String stateHash;
}
