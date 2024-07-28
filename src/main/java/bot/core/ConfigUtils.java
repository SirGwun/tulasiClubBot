package bot.core;

import java.io.FileInputStream;
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
        Properties secretProperties = new Properties();

        if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
            botToken = System.getenv("BOTTOCKEN");
            try (InputStream configInput = new FileInputStream("/data/config.properties")) {
                config.load(configInput);
                adminChatId = Long.parseLong(config.getProperty("adminChatId"));
                groupID = Long.parseLong(config.getProperty("groupId"));
                System.out.println("Загружены данные из AMVERA");
            } catch (FileNotFoundException ex) {
                Main.log.error("Sorry, unable to find file: {}", ex.getMessage());
            } catch (IOException ex) {
                Main.log.error("Sorry, unable to read file: {}", ex.getMessage());
            }
        } else {
            try (
                    InputStream configInput = ConfigUtils.class.getClassLoader().getResourceAsStream("config.properties");
                    InputStream secretInput = ConfigUtils.class.getClassLoader().getResourceAsStream("secret.properties")
            ) {
                if (configInput == null) {
                    throw new FileNotFoundException("config.properties not found");
                }
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }

                config.load(configInput);
                adminChatId = Long.parseLong(config.getProperty("adminChatId"));
                groupID = Long.parseLong(config.getProperty("groupId"));

                secretProperties.load(secretInput);
                botToken = secretProperties.getProperty("botToken");
                System.out.println("Загружены данные из local.properties");
            } catch (FileNotFoundException ex) {
                Main.log.error("Sorry, unable to find file: {}", ex.getMessage());
            } catch (IOException ex) {
                Main.log.error("Sorry, unable to read file: {}", ex.getMessage());
            }

        }
    }

    public static String getBotToken() {
        return botToken;
    }

    public static void testMode() {
        Properties secretProperties = new Properties();
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
            secretProperties.load(input);
            botToken = secretProperties.getProperty("testBotToken");
        } catch (IOException ex) {
            Main.log.error(ex.getMessage());
        }
    }

    public static long getAdminChatId() {
        return adminChatId;
    }

    public static long getGroupID() {
        return groupID;
    }
}
