package bot.core.util.config.infra;

import bot.core.Main;

public class ConfigProviderBuilder {
    public static ConfigProvider getConfigProvider() {
        boolean amvera = System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1");

        if (amvera) return ProductionConfig.getInstance();
        if (Main.test) return TestConfig.getInstance();
        return LocalConfig.getInstance();
    }
}
