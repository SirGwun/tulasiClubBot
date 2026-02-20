package bot.core.model;

import java.time.LocalDateTime;

public class SpecialGroup implements BaseGroup {
    long id;
    String name;
    String tag;
    boolean isBotAdmin;
    LocalDateTime createdAt;

    public SpecialGroup(long id, String name, String tag, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.createdAt = createdAt;
    }

    public SpecialGroup(long id, String name, String tag) {
        this.id = id;
        this.name = name;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isBotAdmin() {
        return isBotAdmin;
    }

    public void setBotAdmin(boolean botAdmin) {
        isBotAdmin = botAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
