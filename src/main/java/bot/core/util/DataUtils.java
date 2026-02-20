package bot.core.util;

import bot.core.Main;
import bot.core.model.*;
import bot.core.repos.GroupRepository;
import bot.core.repos.SessionRepository;
import bot.core.repos.UserRepository;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public final class DataUtils {

    private static final Logger log = LoggerFactory.getLogger(DataUtils.class);

    private final boolean amvera =
            System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1");

    private final String base = amvera ? "/data/" : "data/";
    private final String configPath = base + "config.properties";
    private final String helpPath = base + "help.txt";
    private final String paymentFolderPath = base + "Payment info/";
    private final String catalogPath = base + "catalog.txt";

    private final Properties config = new Properties();

    private long adminChatID;
    private int timerMinutes;
    private String help;

    private final GroupRepository groupRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public DataUtils(GroupRepository groupRepository,
                     SessionRepository sessionRepository,
                     UserRepository userRepository) {

        this.groupRepository = groupRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;

        loadConfig();
    }

    /* ================= GROUPS ================= */

    public void addNewGroup(String groupName, long groupId) {
        Group group = new Group(
                groupName,
                groupId,
                getActualGroupTag(),
                ChatUtils.isBotAdminInGroup(groupId)
        );

        groupRepository.saveGroup(group);
    }

    public synchronized List<Group> getGroupList() {
        return groupRepository.findAllGroups();
    }

    public void removeGroup(Long groupId) {

        Group group = groupRepository.findGroupById(groupId).orElse(null);
        if (group == null) {
            log.info("Группа {} не найдена", groupId);
            return;
        }

        try {
            LeaveChat leaveChat = new LeaveChat();
            leaveChat.setChatId(groupId);
            Main.paymentBot.execute(leaveChat);
        } catch (TelegramApiException e) {
            log.warn("Не удалось выйти из группы {}", group.getName());
        }

        groupRepository.deleteGroup(groupId);
        log.info("Группа {} удалена", group.getName());
    }

    public void updateGroupName(String groupName, Long groupId) {
        groupRepository.updateGroupName(groupId, groupName);
    }

    public String getGroupName(Long groupId) {
        Group group = groupRepository.findGroupById(groupId).orElse(null);
        return group != null ? group.getName() : null;
    }

    public Group getGroupByName(String name) {
        return groupRepository.findGroupByName(name).orElse(null);
    }

    public Group getGroupById(Long id) {
        return groupRepository.findGroupById(id).orElse(null);
    }

    public boolean containsGroupId(long id) {
        return groupRepository.findGroupById(id).isPresent();
    }

    public void checkAndFixAdminRights() {
        List<Group> groups = groupRepository.findAllGroups();

        for (Group group : groups) {
            boolean realAdmin = ChatUtils.isBotAdminInGroup(group.getId());
            if (realAdmin != group.isBotAdmin()) {
                group.setIsBotAdmin(realAdmin);
                groupRepository.updateGroupAdminRights(group.getId(), realAdmin);
            }
        }
    }

    /* ================= SESSIONS ================= */

    public void saveSession(Session sessionByUser) {
        sessionRepository.save(sessionByUser);
    }

    public Map<Long, Session> loadSessions() {
        List<Session> sessions = sessionRepository.findAll();
        Map<Long, Session> result = new HashMap<>();
        for (Session session : sessions) {
            result.put(session.getUserId(), session);
        }
        return result;
    }

    /* ================= TIMERS ================= */

    public int getTimerMinutes() {
        return timerMinutes;
    }

    public void setTimerMinutes(int timerMinutes) {
        this.timerMinutes = timerMinutes;
        config.setProperty("timeMinutes", String.valueOf(timerMinutes));
        saveConfig();
    }

    public void storeTimer(TimerController.Timer timer) {
        String sql = "INSERT INTO timers(userId, groupId, time, startTime) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setLong(1, timer.getUserId());
            stmt.setLong(2, timer.getGroupId());
            stmt.setLong(3, timer.getTime_sec());
            stmt.setLong(4, timer.getStartTime());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            log.warn("Not success insertion for userId={}, groupId={}: \n{}",
                    timer.getUserId(), timer.getGroupId(), ex.getMessage());
        }
    }
    public void unstoreTimer(Long userId, Long groupId) {
        String sql = "DELETE FROM timers WHERE userId=? AND groupId=?;";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setLong(1, userId);
            stmt.setLong(2, groupId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                log.debug("No records deleted for userId={}, groupId={}",
                        userId, groupId);
            }
        } catch (SQLException ex) {
            log.warn("Failed to delete timer for userId={}, groupId={}: {}",
                    userId, groupId, ex.getMessage());
        }
    }

    public void loadTimers() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM timers");
            while (resultSet.next()) {
                long userId = resultSet.getLong(1);
                long groupId = resultSet.getLong(2);
                long time = resultSet.getLong(3);
                long startTime = resultSet.getLong(4);

                long elapsedTime = Instant.now().getEpochSecond() - startTime;
                if (time > elapsedTime) {
                    TimerController.restoreTimer(userId, groupId, time - elapsedTime);
                } else {
                    log.info("user {} added in group {} by timer", userId, Main.dataUtils.getGroupName(groupId));
                    ChatUtils.addInGroup(userId, groupId, "Добавлен по таймеру");
                    unstoreTimer(userId, groupId);
                }
            }
            resultSet.getLong(1);
        } catch (SQLException ex) {
            log.warn("Failed to load timers {}", ex.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + base + "DataBase.db");
    }

    /* ================= TAGS ================= */

    public String getActualGroupTag() {
        int tagId = Integer.parseInt(config.getProperty("groupTag"));
        return groupRepository.findTagById(tagId).orElse(null).getName();
    }

    public void setGroupTag(String groupTag) {
        var tag = groupRepository.findTagByName(groupTag).orElse(null);
        if (tag != null) {
            config.setProperty("groupTag", String.valueOf(tag.getId()));
            saveConfig();
        }
    }

    public Map<Long, String> getTagMap() {
        Map<Long, String> result = new HashMap<>();
        groupRepository.findAllTag()
                .forEach(tag -> result.put(tag.getId(), tag.getName()));
        return result;
    }

    public void addNewTag(String tagName) {
        groupRepository.saveTag(new Tag(groupRepository.findAllTag().size() + 1, tagName));
    }

    public Long getTagId(String tagName) {
        Tag tag = groupRepository.findTagByName(tagName).orElse(null);
        return tag != null ? tag.getId() : -1;
    }

    /* ================= CONFIG ================= */

    private void loadConfig() {
        try (InputStream input = new FileInputStream(configPath)) {
            config.load(input);
            adminChatID = Long.parseLong(config.getProperty("adminChatID"));
            timerMinutes = Integer.parseInt(config.getProperty("timeMinutes"));
        } catch (IOException e) {
            log.error("Ошибка загрузки config", e);
        }
    }

    private void saveConfig() {
        try (OutputStream output = new FileOutputStream(configPath)) {
            config.store(output, null);
        } catch (IOException ex) {
            log.error("Can't save config {}", ex.getMessage());
        }
    }

    public String getHistoryId() {
        return (String) config.get("history");
    }

    public long getAdminId() {
        return adminChatID;
    }


    /* ================= FILE CONTENT ================= */

    public String getHelp() {
        if (help == null) {
            try (InputStream input = new FileInputStream(helpPath)) {
                help = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Ошибка чтения help.txt", e);
            }
        }
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
        try (OutputStream output = new FileOutputStream(helpPath)) {
            IOUtils.write(help, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка сохранения help.txt", e);
        }
    }

    public String getCatalog() {
        try (InputStream input = new FileInputStream(catalogPath)) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка чтения catalog.txt", e);
        }
        return null;
    }

    public void setPaymentInfo(String text) {
        try (OutputStream out =
                     new FileOutputStream(paymentFolderPath + "paymentText.txt")) {
            IOUtils.write(text, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка сохранения payment info", e);
        }
    }

    public String getPaymentInfo() {
        try (InputStream input =
                     new FileInputStream(paymentFolderPath + "paymentText.txt")) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка чтения payment info", e);
        }
        return "";
    }

    public List<Long> getUsrList() {
        return userRepository.findAll()
                .stream()
                .map(User::getChatId)
                .toList();
    }
}
