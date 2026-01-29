package bot.core.util.config;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdConfig implements Config {
    Environment environment;

    public ProdConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getMainBotToken() {
        return environment.getRequiredProperty("BOTTOCKEN");
    }

    @Override
    public String getMainBotName() {
        return environment.getRequiredProperty("BOTNAME");
    }

    @Override
    public String getQuizBotToken() {
        return environment.getRequiredProperty("TESTBOTTOCKEN");
    }

    @Override
    public String getQuizBotName() {
        return environment.getRequiredProperty("TESTBOTNAME");
    }
}
