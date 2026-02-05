package bot.core.util.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringConfigProvider implements ConfigProvider {

    private final Environment environment;

    public SpringConfigProvider(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String get(String key) {
        return environment.getRequiredProperty(key);
    }
}

