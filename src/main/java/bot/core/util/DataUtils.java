package bot.core.util;

import bot.core.Main;
import bot.core.control.Session;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
    private final String catalogPath;

    private String botName = "tulasiClubBot";
    private String botToken;
    private long adminChatID;
    private long mainGroupID;
    private String info;
    private String help;
    private final Properties config = new Properties();
    private Map<String, Long> groupList = new HashMap<>();

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
        this.catalogPath = base + "catalog.txt";

        if (amvera) {
            botToken = System.getenv("BOTTOCKEN");
            log.info("Запущено в AMVERA");
        } else {
            try (InputStream secretInput = DataUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }
                Properties secretProperties = new Properties();
                secretProperties.load(secretInput);
                botToken = secretProperties.getProperty("botToken");
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
        groupList.put(name, id);
        saveGroupList();
        loadGroupList();
        return groupList.containsKey(name);
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
            mainGroupID = Long.parseLong(config.getProperty("groupID"));
        } catch (FileNotFoundException ex) {
            Main.log.error("Не удалось загрузить конфиг {}", ex.getMessage());
        } catch (IOException ex) {
            Main.log.error("Unable to read конфиг file : {}", ex.getMessage());
        }
    }

    public Map<String, Long> getGroupList() {
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
        groupList = (Map<String, Long>) load("groupList");
    }

    private void saveGroupList() {
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
        } catch (IOException ex) {
            Main.log.error(ex.getMessage());
        }
    }

    public long getAdminId() {
        return adminChatID;
    }

    public long getMainGroupId() {
        return mainGroupID;
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
                Main.log.error("Не удалось загрузить help.txt", e);
            } catch (IOException e) {
                Main.log.error("Не удалось прочитать help.txt", e);
            }
        }
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
        try (OutputStream output = new FileOutputStream(helpPath)) {
            IOUtils.write(help, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Main.log.error("Не удалось сохранить help.txt", e);
        }
    }

    public String getInfo() {
        if (info == null) {
            try (InputStream input = new FileInputStream(infoPath)) {
                info = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                Main.log.error("Не удалось загрузить info.txt", e);
            } catch (IOException e) {
                Main.log.error("Не удалось прочитать info.txt", e);
            }
        }
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
        try (OutputStream output = new FileOutputStream(infoPath)) {
            IOUtils.write(info, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Main.log.error("Не удалось сохранить info.txt", e);
        }
    }

    public String getHistroyId() {
        return (String) config.get("history");
    }

    public void removeGroup(Long groupId) {
        for (Map.Entry<String, Long> entry : groupList.entrySet()) {
            String name = entry.getKey();
            Long id = entry.getValue();

            if (Objects.equals(id, groupId)) {
                groupList.remove(name, id);
                log.info("Группа {} удалена из списка", name);
                saveGroupList();
                return;
            }
        }
        Main.log.info("Группа {} не найдена в списке", groupId);
    }

    public String getGroupName(long groupId) {
        for (Map.Entry<String, Long> entry : groupList.entrySet()) {
            String name = entry.getKey();
            Long id = entry.getValue();

            if (Objects.equals(groupId, id)) {
                return name;
            }
        }
        return null;
    }

    public String getCatalog() {
        try (InputStream catalogInput = new FileInputStream(catalogPath)) {
            return IOUtils.toString(catalogInput, StandardCharsets.UTF_8);
        } catch (FileNotFoundException ex) {
            Main.log.error("Не удалось загрузить каталог {}", ex.getMessage());
        } catch (IOException ex) {
            Main.log.error("Unable to read каталог file : {}", ex.getMessage());
        }
        return null;
    }

    public Long getDefaulfGroup() {
        return Long.parseLong(config.getProperty("groupID"));
    }
}

