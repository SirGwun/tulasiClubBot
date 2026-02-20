package bot.core.control;

import bot.core.Main;
import bot.core.model.*;
import bot.core.repos.SessionRepository;
import bot.core.repos.UserRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionService {
    private static final SessionService INSTANCE = new SessionService();
    UserRepository userRepository = new UserRepository();
    SessionRepository sessionRepository = new SessionRepository();

    public static Map<Long, Session> getSessionMap() {
        return sessionMap;
    }

    private static Map<Long, Session> sessionMap;

    private SessionService() {
        Map<Long, Session> loaded = Main.dataUtils.loadSessions();
        sessionMap = new ConcurrentHashMap<>(loaded != null ? loaded : Map.of());
    }

    public static SessionService getInstance() {
        return INSTANCE;
    }

    public void setUserGroupId(Long userId, Long groupId) {
        Session session = getOrOpenSession(userId);
        synchronized (session) {
            sessionRepository.updateGroupId(userId, groupId);
            session.setGroupId(groupId);
        }
    }

    public EditingActions getAction(long userId) {
        return getOrOpenSession(userId).getAction();
    }

    public void setSessionAction(Long userId, EditingActions action) {
        Session session = getOrOpenSession(userId);
        synchronized (session) {
            sessionRepository.updateAction(userId, action);
            session.setAction(action);
        }
    }

    public Long getUserGroupId(long userId) {
        Session session = getOrOpenSession(userId);
        return session.getGroupId();
    }

    public Session getOrOpenSession(Long userId) {
        return sessionMap.computeIfAbsent(userId, id ->
                sessionRepository.findByUserId(id).orElseGet(() -> {
                    User user = getOrCreateUser(id);
                    Session session = new Session(id, user.getName());
                    sessionRepository.save(session);
                    return session;
                }));
    }

    private User getOrCreateUser(Long userId) {
        return userRepository.findByChatId(userId)
                .orElseGet(() -> {
                    User user = new User(userId);
                    userRepository.saveUser(user);
                    return user;
                });
    }
}
