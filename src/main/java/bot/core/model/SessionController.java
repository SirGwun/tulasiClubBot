package bot.core.model;

import bot.core.Main;
import bot.core.control.Session;

import java.util.Map;

public class SessionController {
    private static final SessionController INSTANCE = new SessionController();
    private final Map<Long, Session> sessionMap;

    private SessionController() {
        sessionMap = Main.dataUtils.loadSessions();
    }

    public static SessionController getInstance() {
        return INSTANCE;
    }

    public void setUserGroupId(Long userId, Long groupId) {
        //todo проерять что это реальня группа
        sessionMap.get(userId).setGroupId(groupId);
        Main.dataUtils.saveSessions(sessionMap);
    }

    public Session getUserSession(long targetUserId) {
        return sessionMap.get(targetUserId);
    }

    public Session openSessionIfNeeded(long fromId) {
        return sessionMap.computeIfAbsent(fromId, k -> new Session(fromId));
    }
}
