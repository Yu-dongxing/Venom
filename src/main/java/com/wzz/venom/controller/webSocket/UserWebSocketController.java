package com.wzz.venom.controller.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzz.venom.utils.UserWebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 用户端 WebSocket 通知控制器
 * 模块：用户实时事件上报（理财操作、客服请求等）
 * 连接地址: ws://<your-server>/ws/user/notify
 */
@Component
public class UserWebSocketController extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(UserWebSocketController.class);
    private final UserWebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 新增：注入 AdminWebSocketController
    private final AdminWebSocketController adminWebSocketController;

    @Autowired
    // 2. 修改：更新构造函数以接收注入
    public UserWebSocketController(UserWebSocketSessionManager sessionManager, AdminWebSocketController adminWebSocketController) {
        this.sessionManager = sessionManager;
        this.adminWebSocketController = adminWebSocketController;
    }

    /**
     * 当用户客户端成功建立 WebSocket 连接时触发
     * userId 是通过 HandshakeInterceptor 放入 session attributes 的
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        String userName = session.getAttributes().get("user").toString();
        if (userId == null) {
            log.error("WebSocket 连接失败：无法从 session attributes 中获取 userId");
            try {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing userId"));
            } catch (Exception e) {
                // ignore
            }
            return;
        }
        sessionManager.addSession(userId, session);
        // 你可以在这里通知后台，某个用户上线了
        log.info("后台通知：用户 [ID: {}] 已连接 WebSocket 服务。", userId);
        // 3. 新增：通知所有管理员，有新用户连接
        adminWebSocketController.notifyAdminGenericUserEvent(userId, "USER_CONNECTED", null);
    }

    /**
     * 当客户端关闭连接时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId);
            // 你可以在这里通知后台，某个用户下线了
            log.info("后台通知：用户 [ID: {}] 已断开 WebSocket 连接。状态: {}", userId, status);
            // 4. 新增：通知所有管理员，有用户断开连接
            Map<String, String> data = Map.of("reason", status.toString());
            adminWebSocketController.notifyAdminGenericUserEvent(userId, "USER_DISCONNECTED", data);
        }
    }

    /**
     * 接收到用户端发送的消息
     * 前端发送的JSON格式: {"event": "EVENT_TYPE", "data": {...}}
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        try {
            String payload = message.getPayload();
            Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
            String event = (String) msgMap.get("event");

            if (event == null) {
                log.warn("收到来自用户 [ID: {}] 的无效消息（缺少 event 字段）: {}", userId, payload);
                return;
            }
            // 5. 新增：在处理用户消息后，立即通知管理员
            // 我们将整个消息体作为数据转发给管理员
            Object data = msgMap.get("data");



            // 根据事件类型进行分发处理
            switch (event) {
                case "FINANCIAL_TRANSFER_IN":
                    // 用户理财转入
                    log.info("后台通知：用户 [ID: {}] 发起理财转入操作，数据: {}", userId, data);
                    adminWebSocketController.notifyAdminGenericUserEvent(userId, event, data);

                    // 在这里可以调用你的业务 Service 进行处理
                    // e.g., financeService.handleTransfer(userId, data);
                    break;
                case "FINANCIAL_TRANSFER_OUT":
                    // 用户理财转出
                    log.info("后台通知：用户 [ID: {}] 发起理财转出操作，数据: {}", userId, data);
                    adminWebSocketController.notifyAdminGenericUserEvent(userId, event, data);

                    // 在这里可以调用你的业务 Service 进行处理
                    // e.g., financeService.handleTransfer(userId, data);
                    break;

                case "CONTACT_SUPPORT":
                    // 用户点击客服通知
                    log.info("后台通知：用户 [ID: {}] 请求人工客服支持。", userId);
                    adminWebSocketController.notifyAdminGenericUserEvent(userId, event, data);
                    // 在这里可以调用你的业务 Service，例如创建一个客服工单
                    // e.g., supportService.createTicket(userId);
                    break;

                case "ping":
                    // 心跳检测
                    session.sendMessage(new TextMessage("pong"));
                    break;

                default:
                    log.warn("后台通知：收到来自用户 [ID: {}] 的未知事件类型 '{}'", userId, event);
                    break;
            }

        } catch (Exception e) {
            log.error("处理用户 [ID: {}] 的 WebSocket 消息时出错: {}", userId, message.getPayload(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId);
            log.error("后台通知：用户 [ID: {}] 的 WebSocket 连接发生传输错误，已移除。", userId, exception);
        }
    }
}