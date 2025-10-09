package com.wzz.venom.controller.cs;

import com.wzz.venom.service.webSocket.WebSocketNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * webSocket测试接口（禁止使用）
 */
@RestController
@RequestMapping("/api/cs/websocket")
public class webSocketCsController {
    @Autowired
    private WebSocketNotifyService webSocketNotifyService;

    @RequestMapping("/test")
    public String test(){
        webSocketNotifyService.sendUserPurchaseNotification("username", "productName", 20D);
        return "test";
    }
}
