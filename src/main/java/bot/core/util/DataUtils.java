package bot.core.util;

import bot.core.Main;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class that stores configuration and helper data for the bot.
 * All state is kept in instance fields instead of static ones.
 */
public class DataUtils {
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
    private final Properties groupList = new Properties();

    /**
     * Create a new instance and load configuration and group list.
     * Paths are resolved depending on the environment.
     */
    public DataUtils() {
        boolean amvera = System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1");
        String base = amvera ? "/data/" : "data/";
        this.configPath = base + "config.properties";
        this.groupListPath = base + "groupList.properties";
        this.helpPath = base + "help.txt";
        this.infoPath = base + "info.txt";
        this.catalogPath = base + "catalog.txt";

        if (amvera) {
            botToken = System.getenv("BOTTOCKEN");
            System.out.println("Загружен токен");
            System.out.println("AMVERA");
        } else {
            try (InputStream secretInput = DataUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }
                Properties secretProperties = new Properties();
                secretProperties.load(secretInput);
                botToken = secretProperties.getProperty("botToken");
                System.out.println("Загружен токен");
                System.out.println("local");
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
        groupList.put(name, String.valueOf(id));
        saveGroupList();
        loadGroupList();
        return groupList.containsKey(name);
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

    public Properties getGroupList() {
        return groupList;
    }

    private void loadGroupList() {
        try (InputStream groupListInput = new FileInputStream(groupListPath)) {
            groupList.load(groupListInput);
        } catch (FileNotFoundException ex) {
            Main.log.error("Не удалось загрузить groupList {}", ex.getMessage());
        } catch (IOException ex) {
            Main.log.error("Unable to read groupList file : {}", ex.getMessage());
        }
    }

    private void saveGroupList() {
        try (OutputStream groupListOutput = new FileOutputStream(groupListPath)) {
            groupList.store(groupListOutput, null);
        } catch (IOException ex) {
            Main.log.error("Can't save groupList {}", ex.getMessage());
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

    public long getAdminID() {
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

    public String getHistroyID() {
        return (String) config.get("history");
    }

    public void removeGroup(String groupId) {
        boolean removed = false;
        Iterator<Map.Entry<Object, Object>> iterator = groupList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> group = iterator.next();
            if (group.getValue().equals(groupId)) {
                iterator.remove();
                removed = true;
                break;
            }
        }
        if (removed) {
            Main.log.info("Группа {} удалена из списка", groupId);
            saveGroupList();
            loadGroupList();
        } else {
            Main.log.info("Группа {} не найдена в списке", groupId);
        }
    }

    public String getGroupName(long groupID) {
        for (Map.Entry<Object, Object> group : groupList.entrySet()) {
            if (group.getValue().equals(String.valueOf(groupID))) {
                return group.getKey().toString().replace("-", " ");
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
}

