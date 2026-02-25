package bot.core.model;

import java.time.LocalDateTime;

public class SpecialGroup implements BaseGroup {
    long id;
    String name;
    String tag;
    boolean isBotAdmin;
    LocalDateTime createdAt;
    double price;
    String description;


    public SpecialGroup(long id, String name, String tag) {
        this.id = id;
        this.name = name;
        this.tag = tag;
    }

    public SpecialGroup(long id, String name, String tag, double price, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.createdAt = createdAt;
        this.description = description;
        this.price = price;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
