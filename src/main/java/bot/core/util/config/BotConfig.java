package bot.core.util.config;

import bot.core.util.config.infra.ConfigProvider;
import bot.core.util.config.infra.ConfigProviderBuilder;

public class BotConfig {
    ConfigProvider provider;

    public BotConfig() {
        this.provider = ConfigProviderBuilder.getConfigProvider();
    }

    public String getBotToken() {
        return provider.get("bot.token");
    }

    public String getBotName() {
        return provider.get("bot.name");
    }

    public String getQuizToken() {
        return provider.get("quiz.token");
    }

    public String getQuizName() {
        return provider.get("quiz.name");
    }
}