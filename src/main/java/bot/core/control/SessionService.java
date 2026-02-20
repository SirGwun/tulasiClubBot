package bot.core.control;

import bot.core.Main;
import bot.core.model.*;
import bot.core.repos.SessionRepository;
import bot.core.repos.UserRepository;

import java.util.Map;


public class SessionService {
    private static final SessionService INSTANCE = new SessionService();
    UserRepository userRepository = new UserRepository();
    SessionRepository sessionRepository = new SessionRepository();

    public static Map<Long, Session> getSessionMap() {
        return sessionMap;
    }

    private static Map<Long, Session> sessionMap;

    private SessionService() {
        sessionMap = Main.dataUtils.loadSessions();
    }

    public static SessionService getInstance() {
        return INSTANCE;
    }

    public void setUserGroupId(Long userId, Long groupId) {
        getOrOpenSession(userId).setGroupId(groupId);
        Main.dataUtils.saveSession(sessionMap.get(userId));
    }

    public EditingActions getAction(long userId) {
        return getOrOpenSession(userId).getAction();
    }

    public void setSessionAction(Long userId, EditingActions action) {
        sessionRepository.updateAction(userId, action);
    }

    public Long getUserGroupId(long userId) {
        Session session = getOrOpenSession(userId);
        return session.getGroupId();
    }

    public Session getOrOpenSession(Long userId) {
        return sessionMap.computeIfAbsent(userId, id -> {
            User user = getOrCreateUser(id);
            Session session = new Session(id, user.getName());
            sessionRepository.save(session);
            return session;
        });
    }

    private User getOrCreateUser(Long userId) {
        return userRepository.findByChatId(userId)
                .orElseGet(() -> {
                    User user = new User(userId);
                    userRepository.saveUser(user);
                    return user;
                });
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
