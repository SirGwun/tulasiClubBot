package bot.core.util.config;

import org.springframework.stereotype.Service;

@Service
public class StorageConfig {
    ConfigProvider provider;

    public StorageConfig(ConfigProvider provider) {
        this.provider = provider;
    }

    public String getDataDirPatch() {
        return provider.get("storage.data.dir");
    }
}
