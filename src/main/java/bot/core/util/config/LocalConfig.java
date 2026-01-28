package bot.core.util.config;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalConfig implements Config {
    private final Environment environment;

    public LocalConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getBotToken() {
        return environment.getProperty("bot.token");
    }

    @Override
    public String getBotName() {
        return environment.getProperty("bot.name");
    }
}
