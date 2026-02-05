package bot.core.control;

import bot.core.model.EditingActionStates;
import bot.core.model.Session;
import bot.core.store.DatabaseSessionRepository;
import bot.core.store.SessionRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Singleton
public class SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final SessionRepository sessions;

    private volatile ScheduledFuture<?> flushTask;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final Map<Long, Session> sessionMap;

    private final Set<Long> dirtySessions = ConcurrentHashMap.newKeySet();

    private SessionService(DatabaseSessionRepository sessions) {
        this.sessions = sessions;
        sessionMap = sessions.loadAll().stream().collect(Collectors.toMap(
                Session::getUserId,
                Function.identity()
        ));
    }

    public void openSession(Long userId) {
        sessionMap.put(userId, new Session(userId));
        markDirty(userId);
    }

    public void setGroupId(Long userId, Long groupId) {
        Session s = sessionMap.get(userId);
        s.setGroupId(groupId);
        markDirty(s.getUserId());
    }

    public Long getGroupId(Long userId) {
        return sessionMap.get(userId).getGroupId();
    }

    public void setEditingAction(Long userId, EditingActionStates action) {
        Session s = sessionMap.get(userId);
        s.setAction(action);
        markDirty(s.getUserId());
    }

    public EditingActionStates getEditingActionState(Long userId) {
        return sessionMap.get(userId).getAction();
    }

    public void deleteSession(long userId) {
        sessionMap.remove(userId);
        sessions.delete(userId); //in ideal make it by async batch too
    }

    private void markDirty(long userId) {
        dirtySessions.add(userId);

        if (flushTask == null || flushTask.isDone()) {
            flushTask = scheduler.schedule(this::flashDirtySessionsAsync, 1, TimeUnit.SECONDS);
        }
    }

    private void flashDirtySessionsAsync() {
        List<Session> sessionsToSave;

        synchronized (this) {
            sessionsToSave = dirtySessions.stream()
                    .map(sessionMap::get)
                    .filter(Objects::nonNull)
                    .toList();
        }

        if (!sessionsToSave.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    sessions.saveAll(sessionsToSave);
                } catch (Exception e) {
                    LoggerFactory.getLogger(SessionService.class).error("Filled to flash sessions", e);
                }
            });
        }
    }
}
