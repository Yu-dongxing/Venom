package com.wzz.venom.service.product;

/**
 * 产品到期结算服务接口
 */
public interface ProductSettlementService {

    /**
     * 对指定ID的用户产品进行结算
     *
     * @param userProductId 用户持有的产品ID
     */
    void settleProduct(Long userProductId);
}