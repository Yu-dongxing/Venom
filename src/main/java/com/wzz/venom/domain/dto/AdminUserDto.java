package com.wzz.venom.domain.dto;

import lombok.Data;

@Data
public class AdminUserDto {
    /**
     * 管理员用户名
     */
        private String adminUserName;
    /**
     * 管理员用户密码
     */
    private String adminUserPassword;
}
