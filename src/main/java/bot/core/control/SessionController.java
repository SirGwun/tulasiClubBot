package bot.core.control;

import bot.core.Main;
import bot.core.model.Session;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;


public class SessionController {
    private static final SessionController INSTANCE = new SessionController();
    private static Map<Long, Session> sessionMap;

    private SessionController() {
        sessionMap = Main.dataUtils.loadSessions();
    }

    public static SessionController getInstance() {
        return INSTANCE;
    }

    public void setUserGroupId(Long userId, Long groupId) {
        sessionMap.get(userId).setGroupId(groupId);
        Main.dataUtils.saveSessions(sessionMap);
    }

    public Session getUserSession(long targetUserId) {
        return sessionMap.get(targetUserId);
    }

    public Session openSessionIfNeeded(User user) {
        Long fromId = user.getId();
        return sessionMap.computeIfAbsent(fromId, k -> new Session(fromId, user.getUserName()));
    }

    /**
     * Find user id by username from stored sessions.
     *
     * @param username username without @
     * @return user id or null if not found
     */
    public Long getUserIdByUsername(String username) {
        for (Session session : sessionMap.values()) {
            if (username.equals(session.getUserName())) {
                return session.getUserId();
            }
        }
        return null;
    }
}
