package bot.core.util.config;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("test")
public class TestConfig implements Config {
    Environment environment;

    public TestConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getBotToken() {
        return environment.getProperty("test.bot.token");
    }

    @Override
    public String getBotName() {
        return environment.getProperty("test.bot.name");
    }
}
