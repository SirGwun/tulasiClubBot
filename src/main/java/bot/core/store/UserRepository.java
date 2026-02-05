package bot.core.store;

import bot.core.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    void delete(Long userId);
    List<User> loadAll();
    void saveAll(List<User> users);
    Optional<User> getUserById(long userId);
}
