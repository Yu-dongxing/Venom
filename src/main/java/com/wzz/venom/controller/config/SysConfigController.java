package com.wzz.venom.controller.config;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.SysConfig;
import com.wzz.venom.service.config.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统 - 系统配置控制器
 */
@RestController
@RequestMapping("/api/sys/config")
public class SysConfigController {
    @Autowired
    private SysConfigService sysConfigService;

    /**
     * 新增配置
     * POST /api/sys/config
     */
    @PostMapping("/add")
    public Result<?> addConfig(@RequestBody SysConfig newSysConfig) {
        try {
            if (sysConfigService.addConfig(newSysConfig)) {
                return Result.success("新增配置成功");
            }
            return Result.error("新增配置失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID删除配置
     * DELETE /api/sys/config/{id}
     */
    @PostMapping("/{id}")
    public Result<?> deleteConfig(@PathVariable Long id) {
        if (sysConfigService.deleteConfigById(id)) {
            return Result.success("删除配置成功");
        }
        return Result.error("配置不存在或删除失败");
    }

    /**
     * 更新配置
     * PUT /api/sys/config/update
     */
    @PostMapping("/update")
    public Result<?> updateConfig(@RequestBody SysConfig newSysConfig) {
        if (newSysConfig.getId() == null) {
            return Result.error("更新时ID不能为空");
        }
        if (sysConfigService.updateConfig(newSysConfig)) {
            return Result.success("更新成功，返回更新后的数据",newSysConfig);
        }
        return Result.error("配置不存在或更新失败");
    }

    /**
     * 根据配置名查询
     * GET /api/sys/config/by-name?name=xxx
     */
    @PostMapping("/by-name")
    public Result<?> getConfigByName(@RequestParam("name") String configName) {
        SysConfig config = sysConfigService.getConfigByName(configName);
        if (config != null) {
            return Result.success(config);
        }
        return Result.error("没有数据？");
    }

    /**
     * 根据配置名查询(用户端)
     * GET /api/sys/config/user/by-name?name=xxx
     */
    @PostMapping("/user/by-name")
    public Result<?> UsergetConfigByName(@RequestParam("name") String configName) {
        SysConfig config = sysConfigService.getConfigByName(configName);
        if (config != null) {
            return Result.success(config);
        }
        return Result.error("没有数据");
    }

    /**
     * 根据ID查询
     * GET /api/sys/config/{id}
     */
    @PostMapping("/select/{id}")
    public Result<?> getConfigById(@PathVariable Long id) {
        SysConfig config = sysConfigService.getConfigById(id);
        if (config != null) {
            return Result.success(config);
        }
        return Result.error("错误？");
    }

    /**
     * 查询所有配置
     * GET /api/sys/config/list
     */
    @PostMapping("/list")
    public Result<?> listAll() {
        List<SysConfig> configs = sysConfigService.listAllConfigs();
        return Result.success(configs);
    }
}
