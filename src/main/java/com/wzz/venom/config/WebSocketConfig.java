package com.wzz.venom.config;

import com.wzz.venom.controller.webSocket.AdminWebSocketController;
import com.wzz.venom.controller.webSocket.UserWebSocketController;
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
    private final UserWebSocketController userWebSocketController; // 新增
    private final CustomHandshakeInterceptor customHandshakeInterceptor; // 新增

    @Autowired
    public WebSocketConfig(AdminWebSocketController adminWebSocketController,
                           UserWebSocketController userWebSocketController, // 新增
                           CustomHandshakeInterceptor customHandshakeInterceptor) { // 新增
        this.adminWebSocketController = adminWebSocketController;
        this.userWebSocketController = userWebSocketController; // 新增
        this.customHandshakeInterceptor = customHandshakeInterceptor; // 新增
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 1. 管理后台的 WebSocket 配置 (保持不变)
        registry.addHandler(adminWebSocketController, "/ws/admin/notify")
                .setAllowedOrigins("*");

        // 2. 新增：用户端的 WebSocket 配置
        registry.addHandler(userWebSocketController, "/ws/user/notify")
                .addInterceptors(customHandshakeInterceptor) // 添加握手拦截器，用于身份认证
                .setAllowedOrigins("*"); // 生产环境建议配置为具体的域名
    }
}