package bot.core.control;

import bot.core.Main;

public class Session {
    long userId;
    Long groupId;
    SessionState state;

    public Session(long userId) {
        this.userId = userId;
        state = new SessionState();
    }

    public Long getGroupId() {
        if (groupId == null)
            return Main.dataUtils.getMainGroupId();
        return groupId;
    }

    public void setGroupId(Long groupId) {
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
