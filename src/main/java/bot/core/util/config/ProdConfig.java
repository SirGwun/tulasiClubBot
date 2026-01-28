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
    public String getBotToken() {
        return environment.getRequiredProperty("BOTTOCKEN");
    }

    @Override
    public String getBotName() {
        return environment.getRequiredProperty("BOTNAME");
    }
}
