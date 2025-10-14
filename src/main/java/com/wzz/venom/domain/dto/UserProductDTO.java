package com.wzz.venom.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wzz.venom.annotation.ColumnComment;
import com.wzz.venom.enums.ProductIncomeStatusEnum;
import lombok.Data;

/**
 * 用户产品数据传输对象
 */
@Data
public class UserProductDTO {

    private String user;

    /** 产品名称 */
    private String productName;
    /**
     * 产品类型
     */
    private Integer productType;
    /**
     * 产品金额
     */
    private Double amount;
    /**
     * 收益率
     */
    private Double interestRate;
    /**
     * 状态（0-持有中，1-已结束）
     */
    private Integer status;
    /**
     * 周期类型（秒，分钟）cycle_type
     */
    private String cycleType;

    /**
     * 产品周期（秒，分钟）数值 cycle_value
     */
    private String cycleValue;
    /**
     * 收益状态 （盈利 PROFIT 和 亏损 LOSS
     */
    private ProductIncomeStatusEnum incomeStatus;

    /**
     * 用户数据检验值
     */
    private boolean value;
}
