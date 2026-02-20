package bot.core.repos;

import bot.core.model.Session;
import bot.core.model.EditingActions;
import bot.core.util.config.DataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SessionRepository {

    private final Logger logger = LoggerFactory.getLogger(SessionRepository.class);
    private final DataConfig dataConfig = new DataConfig();
    private Connection connection;

    public SessionRepository() {
        createSessionsTable();
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dataConfig.getDBURl());
        }
        return connection;
    }

    public void createSessionsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS Sessions (
                    userId BIGINT PRIMARY KEY,
                    userName VARCHAR(255),
                    groupId BIGINT NULL,
                    action VARCHAR(50) NOT NULL
                )
                """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error("Error creating Sessions table", e);
        }
    }

    public void save(Session session) {
        String sql = """
        INSERT INTO Sessions (userId, userName, groupId, action)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(userId) DO UPDATE SET
            userName = excluded.userName,
            groupId = excluded.groupId,
            action = excluded.action
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, session.getUserId());
            ps.setString(2, session.getUserName());
            ps.setObject(3, session.getGroupId());
            ps.setString(4, session.getAction().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving session {}", session.getUserId(), e);
        }
    }


    public void saveAll(Collection<Session> values) {
        if (values == null || values.isEmpty()) return;

        String sql = """
        INSERT INTO Sessions (userId, userName, groupId, action)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(userId) DO UPDATE SET
            userName = excluded.userName,
            groupId = excluded.groupId,
            action = excluded.action
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (Session session : values) {
                ps.setLong(1, session.getUserId());
                ps.setString(2, session.getUserName());
                ps.setObject(3, session.getGroupId());
                ps.setString(4, session.getAction().name());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            logger.error("Error saving sessions batch. Count: {}", values.size(), e);
        }
    }


    public Optional<Session> findByUserId(long userId) {
        String sql = "SELECT * FROM Sessions WHERE userId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSession(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding session {}", userId, e);
        }

        return Optional.empty();
    }

    public List<Session> findAll() {
        String sql = "SELECT * FROM Sessions";
        List<Session> sessions = new ArrayList<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sessions.add(mapRowToSession(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all Session {}", e.getMessage(), e);
        }

        return sessions;
    }

    public void updateGroupId(long userId, Long groupId) {
        String sql = "UPDATE Sessions SET groupId = ? WHERE userId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, groupId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating groupId for {}", userId, e);
        }
    }

    public void updateAction(long userId, EditingActions action) {
        String sql = "UPDATE Sessions SET action = ? WHERE userId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, action.name());
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating action for {}", userId, e);
        }
    }

    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM Sessions WHERE userId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting session {}", userId, e);
        }
    }

    private Session mapRowToSession(ResultSet rs) throws SQLException {
        long userId = rs.getLong("userId");
        String userName = rs.getString("userName");
        long groupId = rs.getLong("groupId");
        String actionStr = rs.getString("action");

        Session session = new Session(userId, userName);
        session.setGroupId(groupId);

        EditingActions action = EditingActions.valueOf(actionStr);
        session.setAction(action);

        return session;
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
