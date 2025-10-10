package com.wzz.venom.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.DefaultValue;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity; // 假设您有类似的基类
import com.wzz.venom.enums.ProductIncomeStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户持有产品实体类
 * 对应数据库表：user_products
 */
@Data
@EqualsAndHashCode(callSuper = true) // 如果继承了BaseEntity，建议加上这个注解
@TableName("user_products")
@TableComment("用户持有产品表")
public class UserProduct extends BaseEntity { // 建议继承统一的BaseEntity，便于统一处理公共字段如ID、创建时间等

    /** 用户名 */
    @ColumnComment("用户名")
    @TableField("user_name") // 数据库字段名建议使用下划线命名法
    private String userName; // 字段名从 user 修改为 userName，更清晰

    /** 产品名称 */
    @ColumnComment("产品名称")
    @TableField("product_name")
    private String productName;

    /** 产品类型（1-理财、2-基金、3-存款等） */
    @ColumnComment("产品类型（1-理财、2-基金、3-存款等）")
    @TableField("product_type")
    private Integer productType;

    /** 到期时间 */
    @ColumnComment("到期时间")
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 方便前端展示
    private LocalDateTime endTime;

    /** 投入金额 */
    @ColumnComment("投入金额")
    @TableField("amount")
    private Double amount; // 金额类型在实际项目中更推荐使用 BigDecimal，避免精度问题

    /** 利率 */
    @ColumnComment("利率")
    @TableField("interest_rate")
    private Double interestRate; // 利率同样建议使用 BigDecimal

    /** 预期收益 */
    @ColumnComment("预期收益")
    @TableField("income")
    private Double income; // 收益同样建议使用 BigDecimal


    /** 状态（0-持有中，1-已结束） */
    @ColumnComment("状态（0-持有中，1-已结束）")
    @TableField("status")
    private Integer status;


    /**
     * 周期类型（秒）cycle_type
     */
    @TableField("cycle_type")
    @ColumnComment("周期类型（秒）s")
    @DefaultValue("'s'")
    private String cycleType;

    /**
     * 产品周期（秒）数值 cycle_value
     */
    @TableField("cycle_value")
    @ColumnComment("产品周期（秒）数值")
    private String cycleValue;
    /**
     * 收益状态 （盈利 PROFIT 和 亏损 LOSS）
     */
    @TableField("income_status")
    @ColumnComment("状态 盈利 PROFIT 和 亏损 LOSS")
    @DefaultValue("'LOSS'")
    private ProductIncomeStatusEnum incomeStatus;


    /** 状态哈希（数据防篡改校验） */
    @ColumnComment("状态哈希（数据防篡改校验）")
    @TableField("state_hash")
    private String stateHash;
}