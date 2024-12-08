package com.nexgen.bankingsystem.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccountBalanceWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AccountBalanceWebSocketHandler.class);

    // Maintain active WebSocket sessions
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("New WebSocket connection established: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Echo received message (for testing purposes)
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: " + session.getId());
    }

    // Send balance updates to all connected WebSocket clients
    public void sendBalanceUpdate(String balance) throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("{\"balance\": \"" + balance + "\"}"));
                } catch (IOException e) {
                    logger.error("Error sending message to session " + session.getId(), e);
                }
            }
        }
    }
}
