package bot.core.util.config;

import bot.core.util.config.infra.ConfigProvider;
import bot.core.util.config.infra.ConfigProviderBuilder;

public class DataConfig {
    ConfigProvider provider;

    public DataConfig() {
        this.provider = ConfigProviderBuilder.getConfigProvider();
    }

    public String getDBURl() {
        return provider.get("db.url");
    }
}
