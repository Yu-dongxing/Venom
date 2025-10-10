package com.wzz.venom.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wzz.venom.annotation.DefaultValue;
import com.wzz.venom.annotation.TableComment;
import com.wzz.venom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 利率档位设置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("interest_rate_config")
@TableComment("利率档位设置")
public class InterestRateConfig extends BaseEntity {
    /**
     * 利率名
     */
    @TableField("interest_rate_name")
    private String interestRateName;

    /**
     * 利率值
     */
    @TableField("interest_rate_value")
    private String interestRateValue;

    /**
     * 是否开启
     */
    @TableField("is_open")
    @DefaultValue("1")
    private Boolean isOpen;
}
