package bot.core.repos;

import bot.core.model.User;
import bot.core.util.config.DataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final DataConfig dataConfig = new DataConfig();
    private Connection connection;

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dataConfig.getDBURl());
        }
        return connection;
    }

    public void createAllUsersTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS Users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    chatId BIGINT UNIQUE NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error("Error creating Users table");
        }
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO Users (chatId, name) VALUES (?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, user.getId());
            ps.setString(2, user.getName());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.debug("{} user stored", rs.getString(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error storing user {}", user.getChatId());
        }
    }

    public Optional<User> findByChatId(long chatId) {
        String sql = "SELECT * FROM Users WHERE chatId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, chatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by chatId {}", chatId);
        }

        return Optional.empty();
    }

    /**
     * @return если у нескольких пользователей совпадут имена, вернет первого
     */
     @Deprecated
    public Optional<User> findByName(String name) {
        String sql = "SELECT * FROM Users WHERE name = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by name {}", name);
        }

        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id {}", id);
        }

        return Optional.empty();
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM Users";
        List<User> users = new ArrayList<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
        }

        return users;
    }

    public void updateName(long chatId, String newName) {
        String sql = "UPDATE Users SET name = ? WHERE chatId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setLong(2, chatId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating user name", e);
        }
    }

    public void deleteByChatId(long chatId) {
        String sql = "DELETE FROM Users WHERE chatId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, chatId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        long chatId = rs.getLong("chatId");
        String name = rs.getString("name");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new User(id, chatId, name, createdAt);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error closing connection", e);
        }
    }
}
