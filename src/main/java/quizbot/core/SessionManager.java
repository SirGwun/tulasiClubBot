package quizbot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quizbot.model.Session;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final long TIMEOUT_MILLIS = 20 * 60 * 1000; // 20 minutes
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final Map<Long, SessionHolder> sessions = new ConcurrentHashMap<>();

    public SessionManager() {
        load(Path.of(QuizBot.data, "quizSessions.ser"));
    }

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

    public void save() {
        Path path = Path.of(QuizBot.data, "quizSessions.ser");

        Thread thread = new Thread(() -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
                oos.writeObject(sessions);
            } catch (IOException e) {
                log.warn("Failed to save sessions");
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @SuppressWarnings("unchecked")
    public void load(Path path) {
        if (!Files.exists(path)) return;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            Map<Long, SessionHolder> loaded = (Map<Long, SessionHolder>) ois.readObject();
            sessions.putAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            log.warn("Failed to load sessions");
        }
    }

    private static class SessionHolder implements Serializable {
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
