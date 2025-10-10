package com.wzz.venom.service.impl.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzz.venom.domain.entity.SysConfig;
import com.wzz.venom.mapper.SysConfigMapper;
import com.wzz.venom.service.config.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper,SysConfig> implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    /**
     * 根据配置名获取配置详情
     * Mybatis-Plus会把驼峰命名的configName自动映射到数据库的config_name字段
     */
    @Override
    public SysConfig getConfigByName(String configName) {
        Assert.hasText(configName, "配置名不能为空");
        QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_name", configName);
        return sysConfigMapper.selectOne(queryWrapper);
    }


    /**
     * 【新增】根据配置名和配置项的键获取具体的值
     * @param configName 配置名, 例如 "aliyun_oss"
     * @param key JSON配置中的字段名, 例如 "accessKeyId"
     * @return 对应字段的值，可以是任意类型 (String, Integer, Boolean, JSONObject, etc.)，如果未找到则返回 null
     */
    @Override
    public Object getConfigValueByNameAndKey(String configName, String key) {
        Assert.hasText(configName, "配置名不能为空");
        Assert.hasText(key, "配置字段Key不能为空");

        SysConfig sysConfig = this.getConfigByName(configName);

        // 使用 Optional 避免多层 if-null 判断，代码更优雅
        return Optional.ofNullable(sysConfig)
                .map(SysConfig::getConfigValue)
                .map(configValue -> configValue.get(key))
                .orElse(null);
    }

    /**
     * 新增配置
     */
    @Override
    public boolean addConfig(SysConfig newSysConfig) {
        // 简单校验
        Assert.notNull(newSysConfig, "配置实体不能为空");
        Assert.hasText(newSysConfig.getConfigName(), "配置名不能为空");

        // 检查配置名是否已存在
        SysConfig existingConfig = this.getConfigByName(newSysConfig.getConfigName());
        if (existingConfig != null) {
            throw new IllegalArgumentException("配置名 '" + newSysConfig.getConfigName() + "' 已存在");
        }

        return sysConfigMapper.insert(newSysConfig) > 0;
    }

    /**
     * 根据ID删除配置
     */
    @Override
    public boolean deleteConfigById(Long id) {
        Assert.notNull(id, "配置ID不能为空");
        return sysConfigMapper.deleteById(id) > 0;
    }

    /**
     * 更新配置
     */
    @Override
    public boolean updateConfig(SysConfig newSysConfig) {
        Assert.notNull(newSysConfig, "配置实体不能为空");
        Assert.notNull(newSysConfig.getId(), "更新时配置ID不能为空");
        return sysConfigMapper.updateById(newSysConfig) > 0;
    }

    /**
     * 根据ID查询配置
     */
    @Override
    public SysConfig getConfigById(Long id) {
        Assert.notNull(id, "配置ID不能为空");
        return sysConfigMapper.selectById(id);
    }

    /**
     * 获取所有配置列表
     */
    @Override
    public List<SysConfig> listAllConfigs() {
        return sysConfigMapper.selectList(null);
    }
}
