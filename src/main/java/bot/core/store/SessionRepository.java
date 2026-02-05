package bot.core.store;

import bot.core.model.Session;

import java.util.List;

public interface SessionRepository {
    List<Session> loadAll();
    void save(Session session);
    void saveAll(List<Session> sessions);
    void delete(long userId);
}
