package bot.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Group implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    long id;
    String name;
    String tag;
    boolean isBotAdmin;

    public Group(String name, long id, String tag, boolean isBotAdmin) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.isBotAdmin = isBotAdmin;
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

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        if (name == null) return "не удалось найти имя";
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
