package com.wzz.venom.utils;

import com.wzz.venom.enums.ProductIncomeStatusEnum;
import com.wzz.venom.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金融收益计算工具类
 * <p>
 * 提供基于收益状态（盈利/亏损）的最终金额计算。
 * 这是一个无状态的工具类，所有方法都是静态的。
 * </p>
 */
public final class FinancialCalculatorUtils {

    // 将构造函数私有化，防止外部实例化工具类
    private FinancialCalculatorUtils() {
    }

    /**
     * 根据收益状态计算最终金额。
     * <p>
     * 计算公式:
     * <ul>
     *   <li><b>盈利 (PROFIT):</b> 本金 + (本金 * 利率)</li>
     *   <li><b>亏损 (LOSS):</b> 本金 - (本金 * 利率)</li>
     * </ul>
     *
     * @param status    收益状态枚举 (PROFIT 或 LOSS)，不能为 null
     * @param principal 本金，不能为 null 且必须大于等于 0
     * @param rate      利率 (例如: 0.05 代表 5%)，不能为 null 且必须大于等于 0
     * @return 计算后的最终金额，类型为 {@link BigDecimal}
     * @throws BusinessException 如果传入的参数不合法
     */
    public static BigDecimal calculateFinalAmount(ProductIncomeStatusEnum status, BigDecimal principal, BigDecimal rate) {
        // 1. 参数健壮性校验
        if(status==null){
            throw new BusinessException(0,"收益状态 (status) 不能为空");
        }
        if(principal==null){
            throw new BusinessException(0,"本金 (principal) 不能为空");
        }if(rate==null){
            throw new BusinessException(0,"利率 (rate) 不能为空");
        }

        if (principal.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(0,"本金 (principal) 不能为负数");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(0,"利率 (rate) 不能为负数");
        }

        // 2. 计算收益或亏损的绝对值
        // principal.multiply(rate) -> 本金 * 利率
        BigDecimal incomeOrLossAmount = principal.multiply(rate);

        // 3. 使用 Java 17 的增强型 switch 表达式进行计算
        // 这种写法非常简洁且不会漏掉 case
        return switch (status) {
            case PROFIT -> principal.add(incomeOrLossAmount);
            case LOSS -> principal.subtract(incomeOrLossAmount);
        };
    }

    /**
     * 重载方法，方便直接传入 double 类型参数。
     * 内部会统一转换为 BigDecimal 进行精确计算。
     *
     * @param status    收益状态枚举
     * @param principal 本金
     * @param rate      利率
     * @return 计算后的最终金额
     */
    public static BigDecimal calculateFinalAmount(ProductIncomeStatusEnum status, double principal, double rate) {
        // 使用 BigDecimal.valueOf() 可以更好地处理 double 类型转换
        return calculateFinalAmount(status, BigDecimal.valueOf(principal), BigDecimal.valueOf(rate));
    }
}