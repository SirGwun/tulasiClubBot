package bot.core.model;

import java.io.Serializable;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    long id;
    String name;
    String tag;

    public Group(String name, long id, String tag) {
        this.id = id;
        this.name = name;
        this.tag = tag;
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
