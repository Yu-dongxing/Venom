package com.wzz.venom.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.ForeignKey;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity; // 假设您有类似的基类
import com.wzz.venom.enums.ForeignKeyAction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邀请码实体类
 * 对应数据库表：invitation_code
 */
@ForeignKey(
        name = "fi_account_user_id",               // 约束名称
        columns = {"used_by_user_id"},                     // 当前表的列
        referenceEntity = User.class,              // 引用 User 实体
        referencedColumns = {"id"},                // 引用 User 表的 id 列
        onDelete = ForeignKeyAction.CASCADE,       // 当 User 被删除时，该用户的邀请码也一并删除
        onUpdate = ForeignKeyAction.RESTRICT       // 不允许更新 User 的 ID
)
@Data
@EqualsAndHashCode(callSuper = true) // 如果继承了BaseEntity，建议加上这个注解
@TableName("invitation_code")
@TableComment("邀请码表")
public class InvitationCode extends BaseEntity { // 建议继承统一的BaseEntity

    /**
     * 邀请码
     */
    @ColumnComment("邀请码，具有唯一性")
    @TableField("code")
    private String code;

    /**
     * 生成该邀请码的用户ID
     */
    @ColumnComment("生成该邀请码的用户ID")
    @TableField("generator_user_id")
    private Long generatorUserId;

    /**
     * 使用该邀请码的用户ID
     */
    @ColumnComment("使用该邀请码的用户ID")
    @TableField("used_by_user_id")
    private Long usedByUserId;

    /**
     * 邀请码状态（0-未使用 1-已使用 2-已过期）
     */
    @ColumnComment("邀请码状态（0-未使用 1-已使用 2-已过期）")
    @TableField("status")
    private Integer status;

    /**
     * 过期时间，null表示永不过期
     */
    @ColumnComment("过期时间，null表示永不过期")
    @TableField("expiry_time")
    private LocalDateTime expiryTime;

    /**
     * 使用时间
     */
    @ColumnComment("使用时间")
    @TableField("used_time")
    private LocalDateTime usedTime;

}