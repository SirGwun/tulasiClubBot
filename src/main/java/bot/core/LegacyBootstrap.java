package bot.core;

import bot.core.util.DataUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class LegacyBootstrap {
    public LegacyBootstrap(DataUtils dataUtils, PaymentBot paymentBot) {
        Legacy.dataUtils = dataUtils;
        Legacy.paymentBot = paymentBot;
    }

    @PostConstruct
    public void init() {
        Legacy.log.info("Legacy инициализирован через Spring");
        Legacy.dataUtils.checkAdminRights();
        Legacy.dataUtils.loadTimers();
    }
}
