package com.wzz.venom.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket Session 管理与广播工具类
 * 采用线程安全的 CopyOnWriteArraySet 存储 Session
 */
@Component
public class WebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    // 使用线程安全的 Set 来存储所有连接的 session
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    // 用于 JSON 序列化
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加一个新的会话
     * @param session WebSocketSession
     */
    public void addSession(WebSocketSession session) {
        sessions.add(session);
        log.info("新连接加入: [ID: {}], 当前在线管理员数量: {}", session.getId(), sessions.size());
    }

    /**
     * 移除一个已关闭的会话
     * @param session WebSocketSession
     */
    public void removeSession(WebSocketSession session) {
        if (sessions.remove(session)) {
            log.info("连接断开: [ID: {}], 当前在线管理员数量: {}", session.getId(), sessions.size());
        }
    }

    /**
     * 广播 TextMessage 消息给所有在线的会话
     * @param message TextMessage
     */
    public void broadcast(TextMessage message) {
        if (sessions.isEmpty()) {
            log.warn("没有在线的管理员客户端，消息未能推送: {}", message.getPayload());
            return;
        }

        int successCount = 0;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                    successCount++;
                } catch (IOException e) {
                    log.error("向 session [ID: {}] 发送消息失败: {}", session.getId(), e.getMessage(), e);
                }
            } else {
                // 如果 session 已经关闭，则从集合中移除
                removeSession(session);
            }
        }
        log.info("成功向 {} 个管理员客户端广播消息: {}", successCount, message.getPayload());
    }

    /**
     * 广播一个对象（会自动序列化为 JSON 字符串）
     * @param payload 消息内容，通常是一个 Map 或 DTO 对象
     */
    public void broadcast(Object payload) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            broadcast(new TextMessage(jsonMessage));
        } catch (JsonProcessingException e) {
            log.error("消息对象序列化为 JSON 失败: {}", payload, e);
        }
    }
}