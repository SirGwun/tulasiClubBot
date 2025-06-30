package bot.core.util;

import bot.core.Main;
import bot.core.model.Session;
import bot.core.model.Group;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class that stores configuration and helper data for the bot.
 * All state is kept in instance fields instead of static ones.
 */
public final class DataUtils {
    private final static Logger log = LoggerFactory.getLogger(DataUtils.class);

    private final String base;
    private final String configPath;
    private final String groupListPath;
    private final String helpPath;
    private final String infoPath;
    private final String paymentFolderPath;
    private final String catalogPath;

    private String botName;
    private String botToken;
    private long adminChatID;
    private long favoriteGroupID;
    private String info;
    private String help;
    private final Properties config = new Properties();
    private List<Group> groupList = new ArrayList<>();

    /**
     * Create a new instance and load configuration and group list.
     * Paths are resolved depending on the environment.
     */
    public DataUtils() {
        boolean amvera = System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1");
        base = amvera ? "/data/" : "data/";
        this.configPath = base + "config.properties";
        this.groupListPath = base + "groupList.ser";
        this.helpPath = base + "help.txt";
        this.infoPath = base + "info.txt";
        this.paymentFolderPath = base + "Payment info/";
        this.catalogPath = base + "catalog.txt";

        if (amvera) {
            botToken = System.getenv("BOTTOCKEN");
            botName = System.getenv("BOTNAME");
            log.info("Запущено в AMVERA");
        } else {
            try (InputStream secretInput = DataUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }
                Properties secretProperties = new Properties();
                secretProperties.load(secretInput);
                botToken = secretProperties.getProperty("botToken");
                botName = secretProperties.getProperty("botName");
                log.info("Запущено в LOCAL");
            } catch (IOException ex) {
                Main.log.error("unable to read secret.properties: {}", ex.getMessage());
            }
        }

        loadConfig();
        loadGroupList();
    }

    public boolean updateConfig(String key, String value) {
        if (config.containsKey(key)) {
            config.setProperty(key, value);
            saveConfig();
            loadConfig();
            return true;
        }
        return false;
    }

    public boolean addNewGroup(String name, long id) {
        groupList.add(new Group(name, id));
        saveGroupList();
        loadGroupList();
        return groupList.stream().anyMatch(g -> g.getName().equals(name));
    }

    public void setDefaultGroup(long groupId) {
        updateConfig("groupID", String.valueOf(groupId));
    }

    private void saveConfig() {
        try (OutputStream configOutput = new FileOutputStream(configPath)) {
            config.store(configOutput, null);
        } catch (IOException ex) {
            Main.log.error("Can't save config {}", ex.getMessage());
        }
    }

    private void loadConfig() {
        try (InputStream configInput = new FileInputStream(configPath)) {
            config.load(configInput);
            adminChatID = Long.parseLong(config.getProperty("adminChatID"));
            favoriteGroupID = Long.parseLong(config.getProperty("favoriteGroupID"));
        } catch (FileNotFoundException ex) {
            log.error("Не удалось загрузить конфиг {}", ex.getMessage());
        } catch (IOException ex) {
            log.error("Unable to read конфиг file : {}", ex.getMessage());
        }
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    private void save(Object object, String name) {
        if (!(object instanceof Serializable))
            throw new IllegalArgumentException();

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(base + name + ".ser"))) {
            output.writeObject(object);
        } catch (IOException ex) {
            log.error("Can't save {} \n {}", name, ex.getMessage());
        }
    }

    private Object load(String name) {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(base + name + ".ser"))) {
            return input.readObject();
        } catch (FileNotFoundException ex) {
            log.error("Не удалось найти файл {}", groupListPath);
        } catch (IOException ex) {
            log.error("Unable to read {} : {}", name, ex.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Unable find class: {}", e.getMessage());;
        }
        return null;
    }

    public void saveSessions(Map<Long, Session> sessionByUser) {
        save(sessionByUser, "sessions");
    }

    public Map<Long, Session> loadSessions() {
        Object ses = load("sessions");
        if (ses instanceof ConcurrentHashMap<?, ?>)
            return (Map<Long, Session>) ses;
        else
            throw new RuntimeException("Не удалось загрузить сессии!");
    }

    private void loadGroupList() {
        List<Group> list = (List<Group>) load("groupList");
        if (list != null) {
            groupList = list;
        }
    }

    public void saveGroupList() {
        try (ObjectOutputStream groupListOutput = new ObjectOutputStream(new FileOutputStream(groupListPath))) {
            groupListOutput.writeObject(groupList);
        } catch (IOException ex) {
            log.error("Can't save groupList {}", ex.getMessage());
        }
    }

    public String getBotToken() {
        return botToken;
    }

    public void testMode() {
        setBotName("harmoniousNutritionBot");
        Properties secretProperties = new Properties();
        try (InputStream input = DataUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
            secretProperties.load(input);
            botToken = secretProperties.getProperty("testBotToken");
            botName = secretProperties.getProperty("testBotName");
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    public long getAdminId() {
        return adminChatID;
    }

    public long getFavoriteGroupId() {
        return favoriteGroupID;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
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

    public String getInfo() {
        if (info == null) {
            try (InputStream input = new FileInputStream(infoPath)) {
                info = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                log.error("Не удалось загрузить info.txt", e);
            } catch (IOException e) {
                log.error("Не удалось прочитать info.txt", e);
            }
        }
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
        try (OutputStream output = new FileOutputStream(infoPath)) {
            IOUtils.write(info, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Не удалось сохранить info.txt", e);
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
                Main.bot.execute(leaveChat);
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

    public Long getDefaultGroup() {
        return Long.parseLong(config.getProperty("groupID"));
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

    public File getPaymentPhoto() {
        return new File(paymentFolderPath + "paymentPhoto.jpg");
    }
}


