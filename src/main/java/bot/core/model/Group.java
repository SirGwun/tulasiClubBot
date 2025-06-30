package bot.core.model;

import java.io.Serializable;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    long id;
    String name;

    public Group( String name, long id) {
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
}
