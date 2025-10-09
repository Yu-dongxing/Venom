package com.wzz.venom.service.impl.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wzz.venom.domain.dto.AdminUserDto;
import com.wzz.venom.domain.entity.AdminUser;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.mapper.AdminUserMapper;
import com.wzz.venom.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private AdminUserMapper adminUserMapper;


    @Override
    public String adminLogin(AdminUserDto adminUserDto) {
        AdminUser adminUser = selectByAdminUser(adminUserDto.getAdminUserName());
        if (adminUser == null){
            throw new BusinessException(0,"该用户不存在");
        }
        if (!adminUser.getAdminUserPassword().equals(adminUserDto.getAdminUserPassword())){
            throw new BusinessException(0,"用户账户密码错误！");
        }

        StpUtil.login(adminUser.getId());
        return StpUtil.getTokenValue();
    }

    @Override
    public Boolean adminRegist(AdminUserDto adminUserDto) {
        AdminUser adminUser = new AdminUser();
        adminUser.setAdminUserName(adminUserDto.getAdminUserName());
        adminUser.setAdminUserPassword(adminUserDto.getAdminUserPassword());
        return adminUserMapper.insert(adminUser)>0;
    }

    /**
     * 根据用户名查询管理员用户
     */
    public AdminUser selectByAdminUser(String user){
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getAdminUserName,user);
        return adminUserMapper.selectOne(queryWrapper);
    }
}
