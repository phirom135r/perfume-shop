package com.perfumeshop.service;

import com.perfumeshop.config.TelegramProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TelegramService {

    private final TelegramProperties props;
    private final RestClient restClient;

    public TelegramService(TelegramProperties props) {
        this.props = props;
        this.restClient = RestClient.builder().build();
    }

    public void sendMessage(String text) {
        String token = props.getBot().getToken() == null ? "" : props.getBot().getToken().trim();
        String msg = text == null ? "" : text.trim();

        if (!props.getBot().isEnabled()) return;
        if (token.isBlank() || msg.isBlank()) return;

        sendToChat(props.getChatId(), msg, token);
        sendToChat(props.getChannelId(), msg, token);
    }

    private void sendToChat(String chatId, String msg, String token) {
        String targetChatId = chatId == null ? "" : chatId.trim();
        if (targetChatId.isBlank()) return;

        String url = "https://api.telegram.org/bot" + token + "/sendMessage";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", targetChatId);
        body.put("text", msg);
        body.put("parse_mode", "HTML");

        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("Telegram sent to: " + targetChatId);
        } catch (Exception e) {
            System.out.println("Telegram send failed for [" + targetChatId + "]: " + e.getMessage());
        }
    }
}