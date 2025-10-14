package com.wzz.venom.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户端 WebSocket Session 管理工具类
 * 使用线程安全的 ConcurrentHashMap 存储 UserId -> Session 的映射
 */
@Component
public class UserWebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(UserWebSocketSessionManager.class);

    // 使用线程安全的 Map 来存储 userId 和 session 的映射
    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 添加一个新的会话，如果该用户已有旧连接，则会替换掉
     * @param userId 用户ID
     * @param session WebSocketSession
     */
    public void addSession(Long userId, WebSocketSession session) {
        // 如果用户已存在一个 session，先关闭旧的，再添加新的，防止多端登录造成混乱
        if (userSessions.containsKey(userId)) {
            try {
                userSessions.get(userId).close();
            } catch (Exception e) {
                log.warn("关闭用户 [ID: {}] 的旧 WebSocket 连接时出错: {}", userId, e.getMessage());
            }
        }
        userSessions.put(userId, session);
        log.info("用户 WebSocket 连接池：新连接加入 [UserId: {}], [SessionId: {}], 当前在线用户数: {}",
                userId, session.getId(), userSessions.size());
    }

    /**
     * 根据 userId 移除一个会话
     * @param userId 用户ID
     */
    public void removeSession(Long userId) {
        if (userId != null && userSessions.containsKey(userId)) {
            WebSocketSession session = userSessions.remove(userId);
            if (session != null) {
                log.info("用户 WebSocket 连接池：连接断开 [UserId: {}], [SessionId: {}], 当前在线用户数: {}",
                        userId, session.getId(), userSessions.size());
            }
        }
    }

    /**
     * 根据 userId 获取会话
     * @param userId 用户ID
     * @return WebSocketSession or null
     */
    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }
}