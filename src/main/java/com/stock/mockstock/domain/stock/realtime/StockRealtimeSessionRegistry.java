// 브라우저 WebSocket session을 종목코드별로 관리하고 실시간 메시지를 전달하는 저장소
package com.stock.mockstock.domain.stock.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StockRealtimeSessionRegistry {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessionsBySymbol = new ConcurrentHashMap<>();

    // 특정 종목을 구독하는 브라우저 session을 등록한다.
    public void subscribe(String symbol, WebSocketSession session) {
        sessionsBySymbol
                .computeIfAbsent(symbol, key -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    // 연결이 끊긴 브라우저 session을 전체 구독 목록에서 제거한다.
    public void remove(WebSocketSession session) {
        sessionsBySymbol.values().forEach(sessions -> sessions.remove(session));
    }

    // 특정 종목을 구독 중인 브라우저들에게 메시지를 전송한다.
    public void broadcast(String symbol, String message) {
        Set<WebSocketSession> sessions = sessionsBySymbol.get(symbol);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        sessions.removeIf(session -> !session.isOpen());

        sessions.forEach(session -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.warn("Realtime message send failed. symbol={}, sessionId={}", symbol, session.getId(), e);
            }
        });
    }
}