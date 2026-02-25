package bot.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Group implements BaseGroup {
    long id;
    String name;
    String tag;
    boolean isBotAdmin;
    LocalDateTime createdAt;
    double price;
    String description;

    public Group(long id, String name, String tag, boolean isBotAdmin, double price, String description, LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.description = description;
        this.id = id;
        this.isBotAdmin = isBotAdmin;
        this.name = name;
        this.price = price;
        this.tag = tag;
    }

    public Group(long id, String name, String tag, boolean isBotAdmin, LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.id = id;
        this.isBotAdmin = isBotAdmin;
        this.name = name;
        this.tag = tag;
    }

    public Group(long id, String name, String tag, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.createdAt = createdAt;
    }

    public Group(String name, long id, String tag, boolean isBotAdmin) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.isBotAdmin = isBotAdmin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isBotAdmin() {
        return isBotAdmin;
    }

    public void setIsBotAdmin(boolean isBotAdmin) {
        this.isBotAdmin = isBotAdmin;
    }

    public String getTag() {
        return tag;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        if (name == null) return "не удалось найти имя";
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object group) {
        if (group == null) return false;
        if (!(group instanceof Group)) return false;
        if (group == this) return true;
        return ((Group) group).getId() == this.getId()
                && ((Group) group).getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "id: " + getId() + " name: " + getName() + " tag: " + getTag();
    }
}
