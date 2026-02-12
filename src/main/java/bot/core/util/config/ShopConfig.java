package bot.core.util.config;

import bot.core.util.config.infra.ConfigProvider;
import bot.core.util.config.infra.ConfigProviderBuilder;

public class ShopConfig {
    ConfigProvider provider;

    public ShopConfig() {
        this.provider = ConfigProviderBuilder.getConfigProvider();
    }

    public String getShopId() {
        return provider.get("shop.id");
    }

    public String getSecretKey() {
        return provider.get("shop.key");
    }

    public String getShopUrl() {
        return provider.get("shop.url");
    }
}
