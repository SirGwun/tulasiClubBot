package bot.core.util;

import bot.core.Main;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class ConfigUtils {
    private static String botName = "tulasiClubBot";
    private static String botToken;
    private static long adminChatID;
    private static long groupID;
    private static String info;
    private static String help;
    private static final Properties config = new Properties();
    private static final Properties groupList = new Properties();

    static {
        if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
            botToken = System.getenv("BOTTOCKEN");
            System.out.println("Загружен токен");
            System.out.println("AMVERA");
        } else {
            try (InputStream secretInput = ConfigUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }
                Properties secretProperties = new Properties();
                secretProperties.load(secretInput);
                botToken = secretProperties.getProperty("botToken");
                System.out.println("Загружен токен");
                System.out.println("local");
            } catch (FileNotFoundException ex) {
                Main.log.error("unable to find secret.properties: {}", ex.getMessage());
            } catch (IOException ex) {
                Main.log.error("unable to read secret.properties: {}", ex.getMessage());
            }
        }
        loadConfig();
        loadGroupList();
    }

    public static boolean updateConfig(String kay, String value) {
        if (config.containsKey(kay)) {
            config.setProperty(kay, value);
            saveConfig();
            loadConfig();
            return true;
        }
        return false;
    }

    public static boolean addNewGroup(String name, long id) {
        groupList.put(name, String.valueOf(id));
        saveGroupList();
        loadGroupList();
        return groupList.containsKey(name);
    }

    private static void saveConfig() {
        OutputStream configOutput = null;
        try {
            if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
                configOutput = new FileOutputStream("/data/config.properties");
            } else {
                configOutput = new FileOutputStream("data/config.properties");
            }
            config.store(configOutput, null);
        } catch (IOException ex) {
            Main.log.error("Can't save config {}", ex.getMessage());
        } finally {
            if (configOutput != null) {
                try {
                    configOutput.close();
                } catch (IOException ex) {
                    Main.log.error("Unable to close конфиг file : {}", ex.getMessage());
                }
            }
        }
    }
    private static void loadConfig() {
        InputStream configInput = null;
        try {
            if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
                configInput = new FileInputStream("/data/config.properties");
            } else {
                configInput = new FileInputStream("data/config.properties");
            }
            config.load(configInput);
            adminChatID = Long.parseLong(config.getProperty("adminChatID"));
            groupID = Long.parseLong(config.getProperty("groupID"));
        } catch (FileNotFoundException ex) {
            Main.log.error("Не удалось загрузить конфиг {}", ex.getMessage());
        } catch (IOException ex) {
            Main.log.error("Unable to read конфиг file : {}", ex.getMessage());
        } finally {
            if (configInput != null) {
                try {
                    configInput.close();
                } catch (IOException ex) {
                    Main.log.error("Unable to close конфиг file : {}", ex.getMessage());
                }
            }
        }
    }

    public static Properties getGroupList() {
        return groupList;
    }

    private static void loadGroupList() {
        InputStream groupListInput = null;
        try {
            if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
                groupListInput = new FileInputStream("/data/groupList.properties");
            } else {
                groupListInput = new FileInputStream("data/groupList.properties");
            }
            groupList.load(groupListInput);
        } catch (FileNotFoundException ex) {
            Main.log.error("Не удалось загрузить groupList {}", ex.getMessage());
        } catch (IOException ex) {
            Main.log.error("Unable to read groupList file : {}", ex.getMessage());
        } finally {
            if (groupListInput != null) {
                try {
                    groupListInput.close();
                } catch (IOException ex) {
                    Main.log.error("Unable to close groupList file : {}", ex.getMessage());
                }
            }
        }
    }

    private static void saveGroupList() {
        OutputStream groupListOutput = null;
        try {
            if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
                groupListOutput = new FileOutputStream("/data/groupList.properties");
            } else {
                groupListOutput = new FileOutputStream("data/groupList.properties");
            }
            groupList.store(groupListOutput, null);
        } catch (IOException ex) {
            Main.log.error("Can't save groupList {}", ex.getMessage());
        } finally {
            if (groupListOutput != null) {
                try {
                    groupListOutput.close();
                } catch (IOException ex) {
                    Main.log.error("Unable to close groupList file : {}", ex.getMessage());
                }
            }
        }
    }


    public static String getBotToken() {
        return botToken;
    }

    public static void testMode() {
        setBotName("harmoniousNutritionBot");
        Properties secretProperties = new Properties();
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
            secretProperties.load(input);
            botToken = secretProperties.getProperty("testBotToken");
        } catch (IOException ex) {
            Main.log.error(ex.getMessage());
        }
    }

    public static long getAdminChatID() {
        return adminChatID;
    }

    public static long getGroupID() {
        return groupID;
    }

    public static String getBotName() {
        return botName;
    }

    public static void setBotName(String botName) {
        ConfigUtils.botName = botName;
    }

    public static String getHelp() {
        if (help == null) {
            String filePath = (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1"))
                    ? "/data/help.txt"
                    : "data/help.txt";

            try (InputStream input = new FileInputStream(filePath)) {
                help = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                Main.log.error("Не удалось загрузить help.txt", e);
            } catch (IOException e) {
                Main.log.error("Не удалось прочитать help.txt", e);
            }
        }
        return help;
    }

    public static void setHelp(String help) {
        ConfigUtils.help = help;

        String filePath = (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1"))
                ? "/data/help.txt"
                : "data/help.txt";
        try (OutputStream output = new FileOutputStream(filePath)) {
            IOUtils.write(help, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Main.log.error("Не удалось сохранить help.txt", e);
        }
    }

    public static String getInfo() {
        if (info == null) {
            String filePath = (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1"))
                    ? "/data/info.txt"
                    : "data/info.txt";

            try (InputStream input = new FileInputStream(filePath)) {
                info = IOUtils.toString(input, StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                Main.log.error("Не удалось загрузить info.txt", e);
            } catch (IOException e) {
                Main.log.error("Не удалось прочитать info.txt", e);
            }
        }
        return info;
    }

    public static void setInfo(String info) {
        ConfigUtils.info = info;

        String filePath = (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1"))
                ? "/data/info.txt"
                : "data/info.txt";
        try (OutputStream output = new FileOutputStream(filePath)) {
            IOUtils.write(info, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Main.log.error("Не удалось сохранить info.txt", e);
        }
    }
}
