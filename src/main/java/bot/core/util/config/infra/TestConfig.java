package bot.core.util.config.infra;

import bot.core.util.DataUtils;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig implements ConfigProvider {
    private static final ConfigProvider INSTANCE = new TestConfig();

    public static ConfigProvider getInstance() {
        return INSTANCE;
    }

    private TestConfig() {

    }

    @Override
    public String get(String key) {
        try (InputStream secretInput = DataUtils.class.getClassLoader().getResourceAsStream("secretTest.properties")) {
            if (secretInput == null) {
                throw new FileNotFoundException("secretTest.properties not found");
            }

            Properties secretProperties = new Properties();
            secretProperties.load(secretInput);

            return secretProperties.getProperty(key);
        } catch (IOException e) {
            LoggerFactory.getLogger(TestConfig.class)
                    .error("Не удалось прочитать токен и имя PaymentBot бота в тестовом режиме");
            throw new IllegalArgumentException();
        }
    }
}
