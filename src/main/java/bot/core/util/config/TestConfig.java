package bot.core.util.config;

import bot.core.Legacy;
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
        Legacy.log.info("Test config lounched");
    }

    @Override
    public String getMainBotToken() {
        return environment.getProperty("test.bot.token");
    }

    @Override
    public String getMainBotName() {
        return environment.getProperty("test.bot.name");
    }

    @Override
    public String getQuizBotToken() {
        return environment.getProperty("test.quizBot.token");
    }

    @Override
    public String getQuizBotName() {
        return environment.getProperty("test.quizBot.name");
    }
}
