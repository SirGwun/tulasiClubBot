package bot.core.model;

public class User {
    private final long id;
    private final String userName;

    public User(long id, String username) {
        this.id = id;
        this.userName = username;
    }

    public long getId() {
        return id;
    }


    public String getName() {
        return userName;
    }
}
