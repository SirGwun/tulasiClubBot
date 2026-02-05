package bot.core.store;

import bot.core.model.Session;
import bot.core.model.SessionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Primary
@Repository
public class DatabaseSessionRepository implements SessionRepository {
    private final Logger log = LoggerFactory.getLogger(DatabaseSessionRepository.class);
    private final JpaSessionRepository jpa;

    public DatabaseSessionRepository(JpaSessionRepository repository) {
        this.jpa = repository;
    }

    @Override
    public List<Session> loadAll() {
        return jpa.findAll()
                .stream()
                .map(SessionEntity::toDomain)
                .toList();
    }

    @Override
    public void save(Session session) {
        jpa.save(new SessionEntity(session));
    }

    @Override
    public void saveAll(List<Session> sessions) {
        jpa.saveAll(sessions.stream().map(SessionEntity::new).toList());
    }

    @Override
    public void delete(long userId) {
        jpa.deleteById(userId);
    }
}
