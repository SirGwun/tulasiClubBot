package bot.core.model;

import java.time.LocalDateTime;

public class Tag {
    long id;
    String name;
    LocalDateTime createdAt;

    public Tag(long id, String name, LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.id = id;
        this.name = name;
    }

    public Tag(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
