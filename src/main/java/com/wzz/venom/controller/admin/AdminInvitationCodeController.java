package com.wzz.venom.controller.admin;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.InvitationCode;
import com.wzz.venom.service.InvitationCode.InvitationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 邀请码管理接口
 * 模块：邀请码
 */
@RestController
@RequestMapping("/api/admin/invitationcode")
public class AdminInvitationCodeController {
    @Autowired
    private InvitationCodeService invitationCodeService;

    /**
     * 生成邀请码
     * @return
     */
    @GetMapping("/get")
    public Result<?> get(){
        InvitationCode s = invitationCodeService.addPermanentCode(0L);
        if (s!=null){
            return Result.success("生成成功，返回邀请码",s.getCode());
        }
        return Result.error("生成失败");
    }

    /**
     * 查询使用邀请码列表
     */
    @GetMapping("/find/all")
    public Result<?> findAll(){
        List<InvitationCode> s = invitationCodeService.list();
        if (!s.isEmpty()){
            return Result.success("查询成功",s);
        }
        return Result.error("列表为空");
    }
    /**
     * 根据id修改邀请码
     */
    @PostMapping("/updata")
    public Result<?> update(@RequestBody InvitationCode s){
        boolean sa  =  invitationCodeService.updateById(s);
        if (sa){
            return Result.success("更新成功！");
        }
        return Result.error("更新失败");
    }
 }
