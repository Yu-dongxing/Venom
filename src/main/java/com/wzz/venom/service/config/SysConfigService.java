package com.wzz.venom.service.config;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzz.venom.domain.entity.SysConfig;

import java.util.List;

public interface SysConfigService extends IService<SysConfig> {
    SysConfig getConfigByName(String configName);

    Object getConfigValueByNameAndKey(String configName, String key);

    boolean addConfig(SysConfig newSysConfig);

    boolean deleteConfigById(Long id);

    boolean updateConfig(SysConfig newSysConfig);

    SysConfig getConfigById(Long id);

    List<SysConfig> listAllConfigs();
}
