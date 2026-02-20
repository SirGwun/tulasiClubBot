package bot.core.model;

import bot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;


public class User {
    Logger log = LoggerFactory.getLogger(User.class);

    public static String DEFAULT_NAME = "unknown";

    private int id;
    private final long chatId;
    private String name;
    private LocalDateTime createdAt;

    public User(int id, long chatId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public User(long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public User(long userId) {
        this.chatId = userId;
        name = askTelegramForUserName();
    }

    private String askTelegramForUserName() {
        GetChat getChat = new GetChat(String.valueOf(chatId));
        String userName = DEFAULT_NAME;
        try {
            Chat chat = Main.paymentBot.execute(getChat);
            userName = chat.getUserName();
            String firstName = chat.getFirstName();
            String lastName = chat.getLastName();
            if (userName == null) {
                userName = (firstName == null ? "не_удалось_получить_имя" : firstName) +
                        (lastName == null ? "" : " " + lastName);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при получении имени пользователя {}", e.getMessage());
        }
        return userName;
    }

    public int getId() {
        return id;
    }

    public long getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }
}
