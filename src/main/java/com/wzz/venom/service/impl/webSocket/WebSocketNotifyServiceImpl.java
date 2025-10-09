package com.wzz.venom.service.impl.webSocket;

import com.wzz.venom.controller.webSocket.AdminWebSocketController;
import com.wzz.venom.service.webSocket.WebSocketNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * WebSocket 通知服务实现
 */
@Service
public class WebSocketNotifyServiceImpl implements WebSocketNotifyService {

    private final AdminWebSocketController adminWebSocketController;

    @Autowired
    public WebSocketNotifyServiceImpl(AdminWebSocketController adminWebSocketController) {
        this.adminWebSocketController = adminWebSocketController;
    }

    @Override
    public void sendUserPurchaseNotification(String user, String productName, Double amount) {
        adminWebSocketController.notifyAdminUserPurchase(user, productName, amount);
    }

    @Override
    public void sendUserWithdrawalNotification(String user, Double amount) {
        adminWebSocketController.notifyAdminUserWithdrawal(user, amount);
    }

    @Override
    public void sendUserRechargeNotification(String user, Double amount) {
        adminWebSocketController.notifyAdminUserRecharge(user, amount);
    }
}