package com.wzz.venom.domain.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.DefaultValue;
import com.wzz.venom.annotation.ForeignKey;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity; // 假设您有类似的基类
import com.wzz.venom.enums.ForeignKeyAction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal; // 导入 BigDecimal 类

/**
 * 用户资金流水实体类（账本）
 * 对应数据库表：user_fund_flow
 */
@Data
@EqualsAndHashCode(callSuper = true) // 如果继承了BaseEntity，建议加上这个注解
@TableName("user_fund_flow")
@TableComment("用户资金流水表")
@ForeignKey(
        name = "fk_account_user_id",               // 约束名称
        columns = {"user_id"},                     // 当前表的列
        referenceEntity = User.class,              // 引用 User 实体
        referencedColumns = {"id"},                // 引用 User 表的 id 列
        onDelete = ForeignKeyAction.CASCADE,       // 当 User 被删除时，该用户的账户也一并删除
        onUpdate = ForeignKeyAction.RESTRICT       // 不允许更新 User 的 ID
)
public class UserFundFlow extends BaseEntity { // 建议继承统一的BaseEntity
    /** 用户名 */
    @ColumnComment("用户名")
    @TableField("user_name")
    private String userName;

    /**
     * 用户id
     */
    @ColumnComment("用户id")
    @TableField("user_id")
    private Long userId;


    /** 业务关联ID（例如订单ID、理财产品ID等） */
    @ColumnComment("业务关联ID")
    @TableField("business_id")
    private String businessId; // 新增字段，用于关联具体业务单据

    /** 交易金额（正为收入，负为支出） */
    @ColumnComment("交易金额（正为收入，负为支出）")
    @TableField("amount")
    @DefaultValue("0")
    private BigDecimal amount;

    /** 资金类型（例如：RECHARGE-充值/WITHDRAW-提现/PURCHASE-购买/REWARD-奖励/WITHDRAW_REFUND-提现拒绝等） */
    @ColumnComment("资金类型")
    @TableField("fund_type")
    private String fundType; // 建议使用枚举或常量字符串，而非魔术字符串

    /** 操作后账户余额 */
    @ColumnComment("操作后账户余额")
    @TableField("balance")
    private BigDecimal balance; // 必须使用 BigDecimal

    /** 状态（0-成功 1-处理中 2-失败） */
    @ColumnComment("状态（0-成功 1-处理中 2-失败）")
    @TableField("status")
    private Integer status; // 优化状态定义，使其更通用

    /** 交易描述/备注 */
    @ColumnComment("交易描述/备注")
    @TableField("description") // 数据库字段名使用 description
    private String description; // 字段名从 describe 修改为 description，避免关键字冲突

    /**
     * [新增] 是否生效（0-未生效/待审核, 1-已生效,2 -拒绝）。充值功能需要审核。
     */
    @ColumnComment("是否生效（0-未生效/待审核, 1-已生效 2 -拒绝）")
    @TableField("is_effective")
    @DefaultValue("1")
    private Integer isEffective;
}