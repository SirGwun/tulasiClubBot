package bot.core.control.handlers.telegram;

import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.TgMessageTypes;
import bot.core.model.input.TelegramUpdate;

public class DocumentHandler implements RoutedHandler<TgMessageTypes, TelegramUpdate> {
    @Override
    public TgMessageTypes routeKey() {
        return TgMessageTypes.DOCUMENT;
    }

    @Override
    public void handle(TelegramUpdate input) {

    }
}
