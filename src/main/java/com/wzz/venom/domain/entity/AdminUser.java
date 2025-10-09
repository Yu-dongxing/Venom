package com.wzz.venom.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableComment("管理员用户表")
@TableName("admin_name")
public class AdminUser extends BaseEntity {
    @ColumnComment("管理员用户姓名")
    @TableField("admin_user_name")
    private String adminUserName;

    @ColumnComment("管理员用户密码")
    @TableField("admin_user_password")
    private String adminUserPassword;
}
