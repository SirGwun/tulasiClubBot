package bot.core.repos;

import bot.core.model.Group;
import bot.core.model.SpecialGroup;
import bot.core.model.Tag;
import bot.core.util.config.DataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
                    id INT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    tag VARCHAR(100) NOT NULL,
                    is_bot_admin BOOLEAN NOT NULL DEFAULT 0,
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
                    id INT PRIMARY KEY,
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
                    id INT PRIMARY KEY,
                    name VARCHAR(100) UNIQUE NOT NULL,
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

    // ===================== GROUP METHODS =====================

    public void saveGroup(Group group) {
        String sql = "INSERT INTO Groups (id, name, tag, is_bot_admin) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, group.getId());
            ps.setString(2, group.getName());
            ps.setString(3, group.getTag());
            ps.setBoolean(4, group.isBotAdmin());

            ps.executeUpdate();
            logger.debug("Group saved with id: {}", group.getId());

        } catch (SQLException e) {
            logger.error("Error saving group", e);
        }
    }

    public void updateGroupAdminRights(long id, boolean isBotAdmin) {

        String sql = "UPDATE Groups SET is_bot_admin = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, isBotAdmin);
            ps.setLong(2, id);

            ps.executeUpdate();
            logger.debug("Group with id {} updated", id);

        } catch (SQLException e) {
            logger.error("Error updating group {}", id, e);
        }
    }

    public void updateGroupName(Long id, String name) {

        String sql = "UPDATE Groups SET name = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, id);

            ps.executeUpdate();
            logger.debug("Group with id {} updated", id);

        } catch (SQLException e) {
            logger.error("Error updating group {}", id, e);
        }
    }

    public void deleteGroup(Long groupId) {
        String sql = "DELETE FROM Groups WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, groupId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting group", e);
        }
    }

    public Optional<Group> findGroupById(long id) {
        String sql = "SELECT * FROM Groups WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToGroup(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding group by id {}", id, e);
        }

        return Optional.empty();
    }

    public Optional<Group> findGroupByName(String name) {
        String sql = "SELECT * FROM Groups WHERE name = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToGroup(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding group by name {}", name, e);
        }

        return Optional.empty();
    }

    public List<Group> findAllGroups() {
        String sql = "SELECT * FROM Groups";
        List<Group> groups = new ArrayList<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                groups.add(mapRowToGroup(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding all groups", e);
        }

        return groups;
    }

    public List<Group> getAllGroupForTag(Tag tag) {
        String sql = "SELECT * FROM Groups WHERE tag = ?";
        List<Group> groups = new ArrayList<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, tag.getName());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(mapRowToGroup(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding groups for tag {}", tag.getName(), e);
        }

        return groups;
    }

    // SpecialGroups methods
    public boolean saveSpecialGroup(SpecialGroup group) {
        String sql = "INSERT INTO SpecialGroups (id, name, tag) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, group.getId());
            ps.setString(2, group.getName());
            ps.setString(3, group.getTag());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.debug("Special group saved with id: {}", rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error("Error saving special group", e);
            return false;
        }
    }

    public Optional<SpecialGroup> findSpecialGroupById(int id) {
        String sql = "SELECT * FROM SpecialGroups WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSpecialGroup(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding special group by id {}", id, e);
        }

        return Optional.empty();
    }

    public Optional<SpecialGroup> findSpecialGroupByTag(String tag) {
        String sql = "SELECT * FROM SpecialGroups WHERE tag = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, tag);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSpecialGroup(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding special group by tag {}", tag, e);
        }

        return Optional.empty();
    }

    public Optional<SpecialGroup> findSpecialGroupForGroup(Long groupId) {
        Group group = findGroupById(groupId.intValue()).orElse(null);

        if (group == null) return Optional.empty();;

        String sql = "SELECT * FROM SpecialGroups WHERE tag = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, group.getTag());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSpecialGroup(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding special group for group {}", groupId, e);
        }

        return Optional.empty();
    }


    // Tags methods

    public void saveTag(Tag tag) {
        String sql = "INSERT INTO Tags (id, name) VALUES (?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, tag.getId());
            ps.setString(2, tag.getName());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    logger.debug("Tag saved with id: {}", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving tag {}", e.getMessage());
        }
    }

    public Optional<Tag> findTagById(int id) {
        String sql = "SELECT * FROM Tags WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTag(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tag by id {}", id, e);
        }

        return Optional.empty();
    }

    public Optional<Tag> findTagByName(String name) {
        String sql = "SELECT * FROM Tags WHERE name = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTag(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tag by name {}", name, e);
        }

        return Optional.empty();
    }

    public List<Tag> findAllTag() {
        String sql = "SELECT * FROM Tags";
        List<Tag> tags = new ArrayList<>();

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tags.add(mapRowToTag(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all tags", e);
        }

        return tags;
    }

    // Mapping methods

    // ===================== MAPPING =====================

    private Group mapRowToGroup(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String tag = rs.getString("tag");
        boolean isBotAdmin = rs.getBoolean("is_bot_admin");

        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new Group(id, name, tag, isBotAdmin, createdAt);
    }

    private SpecialGroup mapRowToSpecialGroup(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String tag = rs.getString("tag");

        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        return new SpecialGroup(id, name, tag, createdAt);
    }

    private Tag mapRowToTag(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
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