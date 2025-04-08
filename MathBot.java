package com.mycompany.mathbot;

/**
 * Автор: Viktoriia Bohoslavska
 */

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

public class MathBot extends TelegramLongPollingBot {

    private static final String BOT_USERNAME = "he1pMath_bot";
    private static final String BOT_TOKEN = "766";
    private static final long GROUP_CHAT_ID = L;

    private final Map<Long, PendingRequest> activeRequests = new HashMap<>();

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        if (message.hasPhoto()) {
            List<PhotoSize> photos = message.getPhoto();
            String fileId = photos.get(photos.size() - 1).getFileId();
            activeRequests.put(chatId, new PendingRequest(chatId, fileId));
            sendText(chatId, "✅ Фото отримано! Вкажіть строк виконання завдання.");
        }
        else if (activeRequests.containsKey(chatId) && message.hasText()) {
            PendingRequest req = activeRequests.get(chatId);
            req.setDeadline(message.getText());
            sendToGroup(req);
            sendText(chatId, "Дякуємо! Ми скоро зв'яжемося з вами ✅");
        }
        else if (message.hasText()) {
            String text = message.getText();

            if (text.startsWith("/список")) {
                sendListToGroup();
            }
            else if (text.startsWith("/ціна")) {
                String[] parts = text.split(" ", 3);
                if (parts.length == 3) {
                    try {
                        Long targetId = Long.parseLong(parts[1]);
                        String price = parts[2];
                        sendText(targetId, "💰 Вартість вашого завдання: " + price);
                        sendText(chatId, "✅ Повідомлення надіслано клієнту.");
                    } catch (Exception e) {
                        sendText(chatId, "❌ Невірний chatId.");
                    }
                } else {
                    sendText(chatId, "Формат: /ціна [chatId] [сума]");
                }
            }
            else if (text.startsWith("/файл")) {
                String[] parts = text.split(" ");
                if (parts.length == 2 && message.hasDocument()) {
                    try {
                        Long targetId = Long.parseLong(parts[1]);
                        SendDocument doc = new SendDocument();
                        doc.setChatId(String.valueOf(targetId));
                        doc.setDocument(new InputFile(message.getDocument().getFileId()));
                        execute(doc);
                        sendText(chatId, "📤 Файл надіслано клієнту.");
                    } catch (Exception e) {
                        sendText(chatId, "❌ Помилка при надсиланні.");
                    }
                } else {
                    sendText(chatId, "Формат: /файл [chatId] (додай файл)");
                }
            }
        }
    }

    private void sendToGroup(PendingRequest req) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(GROUP_CHAT_ID));
        photo.setPhoto(new InputFile(req.getFileId()));
        photo.setCaption("📥 Нова заявка:\n" +
                         "👤 chatId: `" + req.getUserId() + "`\n" +
                         "🕓 Строк: *" + req.getDeadline() + "*\n" +
                         "✉️ [Перейти до чату](https://t.me/c/" + Math.abs(req.getUserId()) + ")");
        photo.setParseMode("Markdown");
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendListToGroup() {
        StringBuilder sb = new StringBuilder("📋 Активні заявки:\n");
        for (PendingRequest req : activeRequests.values()) {
            sb.append("👤 chatId: ").append(req.getUserId())
              .append(" | Строк: ").append(req.getDeadline() == null ? "Не вказано" : req.getDeadline()).append("\n");
        }
        sendText(GROUP_CHAT_ID, sb.toString());
    }

    private void sendText(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        msg.setParseMode("Markdown");
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new MathBot());
            System.out.println("🤖 Бот запущено. Очікую на заявки...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class PendingRequest {
        private final Long userId;
        private final String fileId;
        private String deadline;

        public PendingRequest(Long userId, String fileId) {
            this.userId = userId;
            this.fileId = fileId;
        }

        public Long getUserId() { return userId; }
        public String getFileId() { return fileId; }
        public String getDeadline() { return deadline; }
        public void setDeadline(String deadline) { this.deadline = deadline; }
    }
}



