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
 * 用户理财流水实体类
 * 对应数据库表：user_financial_statement
 */
@Data
@EqualsAndHashCode(callSuper = true) // 如果继承了BaseEntity，建议加上这个注解
@TableName("user_financial_statement")
@TableComment("用户理财流水表")
public class UserFinancialStatement extends BaseEntity { // 建议继承统一的BaseEntity

    /** 用户名 */
    @ColumnComment("用户名")
    @TableField("user_name")
    private String userName;

    /** 关联的理财产品持有ID */
    @ColumnComment("关联的理财产品持有ID")
    @TableField("financial_id")
    private Long financialId;

    /** 交易类型（1-买入 2-赎回 3-收益派发等） */
    @ColumnComment("交易类型（1-买入 2-赎回 3-收益派发等）")
    @TableField("transaction_type")
    private Integer transactionType;

    /** 交易金额（正为增加，负为减少） */
    @ColumnComment("交易金额（正为增加，负为减少）")
    @TableField("amount")
    @DefaultValue("0")
    private BigDecimal amount; //

}