package quizbot.core;

import quizbot.model.Session;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final long TIMEOUT_MILLIS = 20 * 60 * 1000; // 20 minutes

    private final Map<Long, SessionHolder> sessions = new ConcurrentHashMap<>();

    public Session getOrCreate(long userId, Session session) {
        sessions.putIfAbsent(userId, new SessionHolder(session));
        return sessions.get(userId).session;
    }

    public Session get(long userId) {
        SessionHolder holder = sessions.get(userId);
        if (holder == null) return null;
        holder.touch();
        return holder.session;
    }

    public void remove(long userId) {
        sessions.remove(userId);
    }

    public void purgeExpired() {
        long now = Instant.now().toEpochMilli();
        sessions.entrySet().removeIf(e -> now - e.getValue().lastTouch > TIMEOUT_MILLIS);
    }

    private static class SessionHolder {
        final Session session;
        volatile long lastTouch;
        SessionHolder(Session session) {
            this.session = session;
            touch();
        }
        void touch() {
            lastTouch = Instant.now().toEpochMilli();
        }
    }
}
