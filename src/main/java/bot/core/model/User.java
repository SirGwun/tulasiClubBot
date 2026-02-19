package bot.core.model;

import java.time.LocalDateTime;

public class User {
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
