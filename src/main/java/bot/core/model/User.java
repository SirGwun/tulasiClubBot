package bot.core.model;

public class User {
    private final long id;
    private final String userName;
    private final String firstName;
    private final String lastName;

    public User(long id, String username, String firstName, String lastName) {
        this.id = id;
        this.userName = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }
}
