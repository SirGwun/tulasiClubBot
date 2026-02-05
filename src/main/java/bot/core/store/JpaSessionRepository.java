package bot.core.store;

import bot.core.model.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSessionRepository extends JpaRepository<SessionEntity, Long> {
}
