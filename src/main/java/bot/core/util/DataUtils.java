package bot.core.util;

import bot.core.Legacy;
import bot.core.model.TimerController;
import bot.core.model.Session;
import bot.core.model.Group;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import jakarta.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * Utility class that stores configuration and helper data for the bot.
 * All state is kept in instance fields instead of static ones.
 */

@Singleton
@Component
public final class DataUtils {
    private final static Logger log = LoggerFactory.getLogger(DataUtils.class);

    private final boolean amvera = System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1");
    private final String base = amvera ? "/data/" : "data/";
    private final String configPath;
    private final String groupListPath;
    private final String helpPath;
    private final String paymentFolderPath;
    private final String catalogPath;
    private final String tagListPath;

    private long adminChatID;
    private long favoriteGroupID;
    private int timerMinutes;
    private String help;
    private final Properties config = new Properties();
    private List<Group> groupList = new ArrayList<>();

    private Connection connection;

    /**
     * Create a new instance and load configuration and group list.
     * Paths are resolved depending on the environment.
     */
    public DataUtils() {
        this.configPath = base + "config.properties";
        this.groupListPath = base + "groupList.ser";
        this.helpPath = base + "help.txt";
        this.paymentFolderPath = base + "Payment info/";
        this.catalogPath = base + "catalog.txt";
        this.tagListPath = base + "tagList.txt";

        if (amvera) loadProdLogger();
        loadConfig();
        loadGroupList();
    }

    public void checkAdminRights() {
        boolean needToStore = false;
        for (Group group : groupList) {
            boolean realAdminRight = ChatUtils.isBotAdminInGroup(group.getId());
            if (realAdminRight != group.isBotAdmin()) {
                group.setIsBotAdmin(realAdminRight);
                needToStore = true;
            }
        }
        if (needToStore) {
            saveGroupList();
        }
    }

    private void loadProdLogger() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset(); // сброс текущей конфигурации
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        try (InputStream configStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("logback-prod.xml")) {
            if (configStream == null) {
                throw new IllegalArgumentException("Logback config not found: " + "logback-prod.xml");
            }
            configurator.doConfigure(configStream); // загружаем как stream
        } catch (Exception e) {
            log.error("Failed to configure logging context", e);
        }
    }

    public void addNewGroup(String groupName, long groupId) {
        Group newGroup = new Group(
                groupName,
                groupId,
                getGroupTag(),
                ChatUtils.isBotAdminInGroup(groupId));
        groupList.add(newGroup);
        saveGroupList();
    }

    private void saveConfig() {
        try (OutputStream configOutput = new FileOutputStream(configPath)) {
            config.store(configOutput, null);
        } catch (IOException ex) {
            Legacy.log.error("Can't save config {}", ex.getMessage());
        }
    }

    private void loadConfig() {
        try (InputStream configInput = new FileInputStream(configPath)) {
            config.load(configInput);
            adminChatID = Long.parseLong(config.getProperty("adminChatID"));
            favoriteGroupID = Long.parseLong(config.getProperty("favoriteGroupID"));
            timerMinutes = Integer.parseInt(config.getProperty("timeMinutes"));
        } catch (FileNotFoundException ex) {
            log.error("Не удалось загрузить конфиг {}", ex.getMessage());
        } catch (IOException ex) {
            log.error("Unable to read конфиг file : {}", ex.getMessage());
        }
    }

    public synchronized List<Group> getGroupList() {
        return groupList;
    }

    private Object load(String name) {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(base + name + ".ser"))) {
            return input.readObject();
        } catch (FileNotFoundException ex) {
            log.error("Не удалось найти файл {}", groupListPath);
        } catch (IOException ex) {
            log.error("Unable to read {} : {}", name, ex.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Unable find class: {}", e.getMessage());
        }
        return null;
    }

    public void saveSessions(Map<Long, Session> sessionByUser) {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(base + "sessions" + ".ser"))) {
            output.writeObject(sessionByUser);
        } catch (IOException ex) {
            log.error("Can't save {} \n {}", "sessions", ex.getMessage());
        }
    }

    public Map<Long, Session> loadSessions() {
        Object ses = load("sessions");
        if (!(ses instanceof Map<?, ?> rawMap)) {
            log.warn("Loaded object is not a Map");
            return Collections.emptyMap();
        }

        Map<Long, Session> result = new HashMap<>();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof Long)) {
                log.warn("Invalid key type: {}", entry.getKey());
                continue;
            }
            if (!(entry.getValue() instanceof Session)) {
                log.warn("Invalid value type for key {}", entry.getKey());
                continue;
            }
            result.put((Long) entry.getKey(), (Session) entry.getValue());
        }

        return result;
    }


    private void loadGroupList() {
        Object list =  load("groupList");
        if (!(list instanceof List<?> lodedList)) {
            log.warn("Loaded object is not a List");
            return;
        }
        List<Group> groupList = new ArrayList<>();
        for (Object object : lodedList) {
            if (!(object instanceof Group group)) {
                log.warn("Loaded object is not a Group");
                return;
            }
            groupList.add(group);
        }
        log.info("Loaded {} groups", groupList.size());
        this.groupList = groupList;
    }

    public void saveGroupList() {
        try (ObjectOutputStream groupListOutput = new ObjectOutputStream(new FileOutputStream(groupListPath))) {
            groupListOutput.writeObject(groupList);
        } catch (IOException ex) {
            log.error("Can't save groupList {}", ex.getMessage());
        }
    }


    public long getAdminId() {
        return adminChatID;
    }

    public long getFavoriteGroupId() {
        return favoriteGroupID;
    }

    public String getHelp() {
        if (help == null) {
            try (InputStream input = new FileInputStream(helpPath)) {
                help = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                log.error("Не удалось загрузить help.txt", e);
            } catch (IOException e) {
                log.error("Не удалось прочитать help.txt", e);
            }
        }
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
        try (OutputStream output = new FileOutputStream(helpPath)) {
            IOUtils.write(help, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Не удалось сохранить help.txt", e);
        }
    }


    public String getHistoryId() {
        return (String) config.get("history");
    }

    public void removeGroup(Long groupId) {
        Group removeGroup = null;

        for (Group group : groupList) {
            if (Objects.equals(group.getId(), groupId)) {
                removeGroup = group;
                break;
            }
        }
        if (removeGroup != null) {
            try {
                LeaveChat leaveChat = new LeaveChat();
                leaveChat.setChatId(groupId);
                Legacy.paymentBot.execute(leaveChat);
            } catch (TelegramApiException e) {
                log.warn("Не удалось выйти из группы {}", removeGroup.getName());
            }

            groupList.remove(removeGroup);
            log.info("Группа {} удалена из списка", removeGroup.getName());
            saveGroupList();
        } else {
            log.info("Группа {} не найдена в списке", groupId);
        }
    }

    public String getGroupName(long groupId) {
        for (Group group : groupList) {
            if (Objects.equals(group.getId(), groupId)) {
                return group.getName();
            }
        }
        return null;
    }

    public Group getGroupByName(String name) {
        for (Group group : groupList) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public Group getGroupById(Long id) {
        for (Group group : groupList) {
            if (group.getId() == (id)) {
                return group;
            }
        }
        return null;
    }

    public boolean containsGroupId(long id) {
        return groupList.stream().anyMatch(g -> g.getId() == id);
    }

    public String getCatalog() {
        try (InputStream catalogInput = new FileInputStream(catalogPath)) {
            return IOUtils.toString(catalogInput, StandardCharsets.UTF_8);
        } catch (FileNotFoundException ex) {
            log.warn("Не удалось загрузить каталог {}", ex.getMessage());
        } catch (IOException ex) {
            log.warn("Unable to read каталог file : {}", ex.getMessage());
        }
        return null;
    }

    public void setPaymentInfo(String text) {
        try (OutputStream out = new FileOutputStream(paymentFolderPath + "paymentText.txt")) {
            IOUtils.write(text, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка при сохранении payment info", e);
        }
    }

    public String getPaymentInfo() {
        try (InputStream input = new FileInputStream(paymentFolderPath + "paymentText.txt")) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Ошибка при чтении paymentInfo", e);
        }
        return "";
    }

    public String getGroupTag() {
        return getTagMap().get(Integer.parseInt(config.getProperty("groupTag")));
    }

    public void setGroupTag(String groupTag) {
        for (Map.Entry<?, String> entry : getTagMap().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(groupTag)) {
                config.setProperty("groupTag", String.valueOf(entry.getKey()));
                saveConfig();
                break;
            }
        }
    }

    public Map<Integer, String> getTagMap() {
        Map<Integer, String> tags = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tagListPath))) {
            while (reader.ready()) {
                String input = reader.readLine();
                tags.put(Integer.parseInt(input.substring(0, 1)),
                        input.substring(2));
            }
        } catch (IOException e) {
            log.error("Ошибка чтения tagList {}", e.getMessage());
        }
        return tags;
    }

    public void addNewTag(String tagName) {
        Map<Integer, String> tags = getTagMap();
        int newTagId = tags.size() + 1;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tagListPath, true))) {
            writer.write("\n" + newTagId + " " + tagName);
        } catch (IOException e) {
            log.error("Ошибка при добавлении нового тега {}", e.getMessage());
        }
    }

    public Integer getTagId(String tag) {
        Map<Integer, String> tags = getTagMap();
        if (!tags.containsValue(tag)) {
            log.warn("Попытка прочитать не существующий тег {}", tag);
            return -1;
        }

        for (Map.Entry<Integer, String> entry : tags.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(tag))
                return entry.getKey();
        }
        log.warn("Попытка прочитать не существующий тег {}", tag);
        return -1;
    }

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
                    log.info("user {} added in group {} by timer", userId, Legacy.dataUtils.getGroupName(groupId));
                    ChatUtils.addInGroup(userId, groupId, "Добавлен по таймеру");
                    unstoreTimer(userId, groupId);
                }
            }
            resultSet.getLong(1);
        } catch (SQLException ex) {
            log.warn("Failed to load timers");
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + base + "DataBase.db");
        }
        return connection;
    }

}


