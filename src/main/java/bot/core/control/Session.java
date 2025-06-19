package bot.core.control;

import bot.core.Main;

import java.io.Serial;
import java.io.Serializable;

public class Session implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    long userId;
    String userName;
    Long groupId;
    SessionState state;

    public Session(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        state = new SessionState();
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void unSetGroupId() {
        groupId = null;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
