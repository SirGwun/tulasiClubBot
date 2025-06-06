package bot.core.control;

public class Session {
    long userId;
    String groupId;
    SessionState state;

    public Session(long userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public long getUserId() {
        return userId;
    }
}
