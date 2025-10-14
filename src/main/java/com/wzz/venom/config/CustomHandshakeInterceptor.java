package com.wzz.venom.config;

import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 自定义 WebSocket 握手拦截器
 * 用于在 WebSocket 连接建立前进行用户身份认证
 */
@Component
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CustomHandshakeInterceptor.class);

    /**
     * 在握手之前执行，验证用户是否登录
     * @return true: 允许握手, false: 拒绝握手
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // 核心：使用 sa-token 检查当前 HTTP 请求的登录状态
            StpUtil.checkLogin();

            // 如果检查通过（即用户已登录），获取用户ID
            Long userId = StpUtil.getLoginIdAsLong();

            // 将 userId 放入 WebSocketSession 的 attributes 中，以便后续处理器使用
            attributes.put("userId", userId);

            log.info("WebSocket 握手成功, 用户ID: {}", userId);
            return true;
        } catch (Exception e) {
            // 如果 StpUtil.checkLogin() 抛出异常，说明用户未登录
            log.warn("WebSocket 握手失败: 用户未登录或 Token 无效。异常: {}", e.getMessage());
            return false; // 拒绝连接
        }
    }

    /**
     * 握手之后执行
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}