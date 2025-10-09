package com.wzz.venom.config;

import com.wzz.venom.controller.webSocket.AdminWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AdminWebSocketController adminWebSocketController;

    @Autowired
    public WebSocketConfig(AdminWebSocketController adminWebSocketController) {
        this.adminWebSocketController = adminWebSocketController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adminWebSocketController, "/ws/admin/notify")
                // 允许所有域的连接，在生产环境中建议配置为具体的域名
                .setAllowedOrigins("*");
    }
}
