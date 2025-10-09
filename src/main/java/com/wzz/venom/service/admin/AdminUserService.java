package com.wzz.venom.service.admin;

import com.wzz.venom.domain.dto.AdminUserDto;

public interface AdminUserService {
    String adminLogin(AdminUserDto adminUserDto);

    Boolean adminRegist(AdminUserDto adminUserDto);
}
