package bot.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    private static String botToken;
    private static long adminChatId;
    private static long groupID;

    static {
        Properties config = new Properties();

        // Загрузка общих настроек
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("config.properties")) {
            config.load(input);
            adminChatId = Long.parseLong(config.getProperty("adminChatId"));
            groupID = Long.parseLong(config.getProperty("groupId"));
        } catch (FileNotFoundException ex) {
            Main.log.error("Sorry, unable to find config.properties {}", ex.getMessage());
        }
        catch (IOException ex) {
            Main.log.error("Sorry, unable to read config.properties {}", ex.getMessage());
        }

        if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
            botToken = System.getenv("BOTTOCKEN");
        } else {
            Properties secretProperties = new Properties();
            try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                secretProperties.load(input);
                botToken = secretProperties.getProperty("botToken");
            } catch (FileNotFoundException ex) {
                Main.log.error("Sorry, unable to find secret.properties {}", ex.getMessage());
            } catch (IOException ex) {
                Main.log.error("Sorry, unable to read secret.properties {}", ex.getMessage());
            }
        }
    }

    public static String getBotToken() {
        return botToken;
    }

    public static long getAdminChatId() {
        return adminChatId;
    }

    public static long getGroupID() {
        return groupID;
    }
}
