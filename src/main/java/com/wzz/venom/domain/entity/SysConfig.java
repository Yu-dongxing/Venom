package com.wzz.venom.domain.entity;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.common.BaseEntity;
import com.wzz.venom.handler.HutoolJsonObjectTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "sys_config", autoResultMap = true)
public class SysConfig extends BaseEntity {
    /**
     * 配置名
     */
    @TableField("config_name")
    @ColumnComment("配置名")
    private String configName;
    /*
     * 配置详情（json格式）
     */
    @ColumnComment("配置详情-json格式")
    @TableField(value = "config_value", typeHandler = HutoolJsonObjectTypeHandler.class)
    private JSONObject configValue;

}