package bot.core.control;

import bot.core.util.DataUtils;

public class Session {
    long userId;
    String groupId;
    SessionState state;

    public Session(long userId) {
        this.userId = userId;
        state = new SessionState();
    }

    public String getGroupId() {
        if (groupId == null)
            return String.valueOf(DataUtils.getMainGroupId());
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
