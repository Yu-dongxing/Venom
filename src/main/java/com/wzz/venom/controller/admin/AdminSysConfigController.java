package com.wzz.venom.controller.admin;

import cn.hutool.json.JSONObject;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.SysConfig;
import com.wzz.venom.service.config.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;


import java.util.Optional;

/**
 * 管理后台 - 系统配置接口
 * 模块：配置查询 / 更新
 */
@RestController
@RequestMapping("/api/admin/sysconfig")
public class AdminSysConfigController {

    @Autowired
    private SysConfigService sysConfigService;


    // 为了代码的可读性和可维护性，将字符串字面量定义为常量
    private static final String CONFIG_NAME_SYS = "sys_config";
    private static final String CONFIG_KEY_FINANCIAL = "financial_management";
    private static final String CONFIG_KEY_ANNOUNCEMENT = "platform_announcement";


    // ==================== 年化利率管理 ====================

    /**
     * 查看系统年化利率
     */
    @GetMapping("/financial")
    public Result<?> getFin(){
        Object rateValue = sysConfigService.getConfigValueByNameAndKey(CONFIG_NAME_SYS, CONFIG_KEY_FINANCIAL);
        if (rateValue == null){
            // 当系统中没有相关配置时，可以返回一个默认值或提示信息
            return Result.success("系统中没有配置，返回默认值", "0.2");
        }
        return Result.success("获取系统配置成功", rateValue);
    }

    /**
     * 更新系统年化利率
     * @param rate 新的年化利率值
     * @return 操作结果
     */
    @PostMapping("/financial/update")
    public Result<?> updateFinancialRate(@RequestParam String rate) {
        // 校验传入的rate是否合法，例如是否为数字等（此处省略，可根据业务需求添加）
        if (!StringUtils.hasText(rate)) {
            return Result.error("利率值不能为空");
        }

        boolean isSuccess = updateOrCreateConfig(CONFIG_KEY_FINANCIAL, rate);
        return isSuccess ? Result.success("更新成功") : Result.error("更新失败");
    }


    // ==================== 平台公告管理 ====================

    /**
     * 【新增】查询平台公告
     */
    @GetMapping("/announcement")
    public Result<?> getAnnouncement() {
        Object announcementValue = sysConfigService.getConfigValueByNameAndKey(CONFIG_NAME_SYS, CONFIG_KEY_ANNOUNCEMENT);
        if (announcementValue == null || !StringUtils.hasText(announcementValue.toString())) {
            return Result.success("系统中没有配置公告", "暂无公告");
        }
        return Result.success("获取平台公告成功", announcementValue);
    }

    /**
     * 【新增】更新平台公告
     * @param announcement 新的公告内容
     * @return 操作结果
     */
    @PostMapping("/announcement/update")
    public Result<?> updateAnnouncement(@RequestParam String announcement) {
        // 公告内容可以为空字符串，代表清空公告，所以不做空值判断
        boolean isSuccess = updateOrCreateConfig(CONFIG_KEY_ANNOUNCEMENT, announcement);
        return isSuccess ? Result.success("更新公告成功") : Result.error("更新公告失败");
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 提取出的通用更新逻辑：更新或创建 sys_config 中的某个键值对
     * @param key   配置项的键 (e.g., "financial_management", "platform_announcement")
     * @param value 配置项的值
     * @return 操作是否成功
     */
    private boolean updateOrCreateConfig(String key, Object value) {
        // 1. 尝试获取名为 "sys_config" 的配置实体
        SysConfig sysConfig = sysConfigService.getConfigByName(CONFIG_NAME_SYS);

        if (sysConfig == null) {
            // 2. 如果配置实体不存在，则创建新的配置
            SysConfig newConfig = new SysConfig();
            newConfig.setConfigName(CONFIG_NAME_SYS);

            JSONObject configValue = new JSONObject();
            configValue.set(key, value);
            newConfig.setConfigValue(configValue);

            return sysConfigService.addConfig(newConfig);
        } else {
            // 3. 如果配置实体已存在，则更新其内部的JSON值
            // 使用 Optional 包装，避免 configValue 为 null 导致的空指针异常
            JSONObject configValue = Optional.ofNullable(sysConfig.getConfigValue())
                    .orElse(new JSONObject());
            // 设置或更新指定字段
            configValue.set(key, value);
            sysConfig.setConfigValue(configValue);

            return sysConfigService.updateConfig(sysConfig);
        }
    }
}