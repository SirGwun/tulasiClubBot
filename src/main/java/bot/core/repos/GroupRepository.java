package bot.core.repos;

import bot.core.model.Group;
import bot.core.model.SpecialGroup;
import bot.core.model.Tag;
import bot.core.util.config.DataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class GroupRepository {
    private final Logger logger = LoggerFactory.getLogger(GroupRepository.class);
    private final DataConfig dataConfig = new DataConfig();
    private Connection connection;

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dataConfig.getDBURl());
        }
        return connection;
    }

    public void createCommonGroupTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS Groups (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    tag VARCHAR(100) NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
            logger.debug("Groups table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating Groups table", e);
        }
    }

    public void createSpecialGroupsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS SpecialGroups (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    tag VARCHAR(100) NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
            logger.debug("SpecialGroups table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating SpecialGroups table", e);
        }
    }

    public void createTagsTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS Tags (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
            logger.debug("Tags table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating Tags table", e);
        }
    }

    // Groups methods
    public void saveGroup(Group group) {
        String sql = "INSERT INTO Groups (name, tag) VALUES ('" +
                group.getName() + "', '" +
                group.getTag() + "')";

        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.debug("Group saved with id: {}", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving group", e);
        }
    }

    public Optional<Group> findGroupById(int id) {
        String sql = "SELECT * FROM Groups WHERE id = " + id;

        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapRowToGroup(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding group by id {}", id, e);
        }

        return Optional.empty();
    }

    // SpecialGroups methods
    public void saveSpecialGroup(SpecialGroup group) {
        String sql = "INSERT INTO SpecialGroups (name, tag) VALUES ('" +
                group.getName() + "', '" +
                group.getTag() + "')";

        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.debug("Special group saved with id: {}", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving special group", e);
        }
    }

    public Optional<SpecialGroup> findSpecialGroupById(int id) {
        String sql = "SELECT * FROM SpecialGroups WHERE id = " + id;

        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapRowToSpecialGroup(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding special group by id {}", id, e);
        }

        return Optional.empty();
    }

    // Tags methods
    public void saveTag(Tag tag) {
        String sql = "INSERT INTO Tags (name) VALUES ('" + tag.getName() + "')";

        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.debug("Tag saved with id: {}", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving tag", e);
        }
    }

    public Optional<Tag> findTagById(int id) {
        String sql = "SELECT * FROM Tags WHERE id = " + id;

        try (Statement statement = getConnection().createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                return Optional.of(mapRowToTag(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding tag by id {}", id, e);
        }

        return Optional.empty();
    }

    // Mapping methods
    private Group mapRowToGroup(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String tag = rs.getString("tag");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new Group(id, name, tag, createdAt);
    }

    private SpecialGroup mapRowToSpecialGroup(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String tag = rs.getString("tag");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new SpecialGroup(id, name, tag, createdAt);
    }

    private Tag mapRowToTag(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new Tag(id, name, createdAt);
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