package com.wzz.venom.service.webSocket;

/**
 * WebSocket 通知服务接口
 */
public interface WebSocketNotifyService {

    /**
     * 发送用户下单通知
     * @param user 用户名
     * @param productName 产品名称
     * @param amount 金额
     */
    void sendUserPurchaseNotification(String user, String productName, Double amount);

    /**
     * 发送用户提现通知
     * @param user 用户名
     * @param amount 金额
     */
    void sendUserWithdrawalNotification(String user, Double amount);

    /**
     * 发送用户充值通知
     * @param user 用户名
     * @param amount 金额
     */
    void sendUserRechargeNotification(String user, Double amount);
}
