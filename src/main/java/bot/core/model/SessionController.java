package bot.core.model;

import bot.core.Main;
import bot.core.control.Session;
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
        //todo проерять что это реальня группа
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
}
