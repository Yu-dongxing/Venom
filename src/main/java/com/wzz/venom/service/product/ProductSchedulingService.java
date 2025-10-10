package com.wzz.venom.service.product;


import com.wzz.venom.domain.entity.UserProduct;

/**
 * 产品结算任务调度服务接口
 */
public interface ProductSchedulingService {

    /**
     * 安排一个产品到期结算任务
     * @param product 用户购买的产品实体，必须包含ID和endTime
     */
    void scheduleProductSettlement(UserProduct product);

    /**
     * 取消一个产品的结算任务（如果需要）
     * @param userProductId 产品ID
     */
    // void cancelScheduledTask(Long userProductId); // 可选扩展
}