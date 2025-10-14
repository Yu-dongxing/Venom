// src/main/java/com/wzz/venom/domain/dto/UserIncomeStatsDto.java
package com.wzz.venom.domain.dto;

import com.wzz.venom.domain.entity.UserFinancialStatement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户收益统计数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIncomeStatsDto {

    /** 总收益 */
    private BigDecimal totalIncome;

    /** 昨日收益 */
    private BigDecimal yesterdayIncome;

    /** 收益记录列表 */
    private List<UserFinancialStatement> incomeRecords;
}