package com.wzz.venom.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.ColumnComment;
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
@EqualsAndHashCode(callSuper = true) // 如果继承了BaseEntity，建议加上这个注解
@TableName("user_financial")
@TableComment("用户理财信息表")
public class UserFinancial extends BaseEntity { // 建议继承统一的BaseEntity

    /**
     * 主键ID
     * 在你的BaseEntity中可能已经定义
     */

    /** 用户名 */
    @ColumnComment("用户名")
    @TableField("user_name") // 数据库字段名使用下划线
    private String userName; // 字段名从 user 修改为 userName

    /** 理财利率 */
    @ColumnComment("理财利率")
    @TableField("interest_rate")
    private BigDecimal interestRate; // 强烈建议使用 BigDecimal 处理利率，避免精度问题

    /** 理财金额 */
    @ColumnComment("理财金额")
    @TableField("amount")
    private BigDecimal amount; // 强烈建议使用 BigDecimal 处理金额，避免精度问题

    /**
     * 创建时间
     * 此字段已从 BaseEntity 继承，此处无需重复定义。
     * @ColumnComment("创建时间")
     * @TableField(value = "create_time", fill = FieldFill.INSERT)
     * @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     * private LocalDateTime createTime;
     */

    /** 状态（0-持有中，1-已赎回） */
    @ColumnComment("状态（0-持有中，1-已赎回）")
    @TableField("status")
    private Integer status;

    /** 状态哈希（数据完整性校验） */
    @ColumnComment("状态哈希（数据完整性校验）")
    @TableField("state_hash")
    private String stateHash;
}
