package bot.core.util.config;

import org.springframework.stereotype.Service;

@Service
public class BotConfig {
    ConfigProvider provider;

    public BotConfig(ConfigProvider provider) {
        this.provider = provider;
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