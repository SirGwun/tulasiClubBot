package bot.core.control.callbackHandlers.groupNavigation.paiment;

import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import bot.core.control.callbackHandlers.Action;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public class PayWithForeignCardButton extends AbstractCallbackHandler {
    public PayWithForeignCardButton() {
        super(Action.payWithForeignCard);
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        long chatId = cq.getMessage().getChatId();

        ChatUtils.sendMessage(chatId, "Пришлите чек об оплате - лучше всего в формате pdf, " +
                "его мы сможем проверить автоматически. " +
                "Можете прислать скриншот или фото, но проверка займет какое-то время");
    }
}
