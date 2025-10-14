package com.wzz.venom.controller.webSocket;

import com.wzz.venom.utils.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 WebSocket 通知控制器
 * 模块：实时事件推送（下单、提现、充值）
 *
 * 建议前端管理后台建立 WebSocket 连接地址：
 *    ws://<server-host>/ws/admin/notify
 */
@Component
public class AdminWebSocketController extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public AdminWebSocketController(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 当管理后台客户端成功建立 WebSocket 连接时触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 将 session 保存至连接池，以便后续推送消息
        sessionManager.addSession(session);
    }

    /**
     * 当客户端关闭连接时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 从连接池移除断开的 session
        sessionManager.removeSession(session);
    }

    /**
     * 管理后台 WebSocket 消息接收事件（一般用于测试或心跳）
     * 前端可以定时发送 "ping" 消息，后端收到后回复 "pong"，以维持连接活性。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 可解析 message.getPayload() 做业务处理
        String payload = message.getPayload();
        // 例如，简单的心跳检测
        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 发生传输错误时，也认为连接已断开
        sessionManager.removeSession(session);
    }


    // ============================================================
    // 以下为系统事件触发时推送通知的方法（由业务逻辑调用）
    // ============================================================

    /** 通知管理后台：有用户下单理财产品 */
    public void notifyAdminUserPurchase(String user, String productName, Double amount) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "USER_PURCHASE");
        message.put("user", user);
        message.put("productName", productName);
        message.put("amount", amount);
        sessionManager.broadcast(message);
    }

    /** 通知管理后台：有用户发起提现 */
    public void notifyAdminUserWithdrawal(String user, Double amount) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "USER_WITHDRAWAL");
        message.put("user", user);
        message.put("amount", amount);
        sessionManager.broadcast(message);
    }

    /** 通知管理后台：有用户发起充值 */
    public void notifyAdminUserRecharge(String user, Double amount) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "USER_RECHARGE");
        message.put("user", user);
        message.put("amount", amount);
        sessionManager.broadcast(message);
    }

    /**
     * 通用方法：通知管理后台，有来自用户的实时事件
     * @param userId 报告事件的用户ID
     * @param eventType 事件类型 (例如: "USER_CONNECTED", "CONTACT_SUPPORT")
     * @param data 事件相关数据 (可以为 null)
     */
    public void notifyAdminGenericUserEvent(Long userId, String eventType, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", "USER_EVENT_REPORT"); // 给管理端定义一个新的事件类型，用于接收所有用户上报
        message.put("reportedByUserId", userId);
        message.put("reportedEvent", eventType);
        message.put("reportedData", data);
        sessionManager.broadcast(message);
    }
}
