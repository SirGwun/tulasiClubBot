package bot.core.control.handlers.telegram;

import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.TgMessageTypes;
import bot.core.model.input.TelegramUpdate;

public class ImageHandler implements RoutedHandler<TgMessageTypes, TelegramUpdate> {
    @Override
    public TgMessageTypes routeKey() {
        return TgMessageTypes.IMAGE;
    }

    @Override
    public void handle(TelegramUpdate input) {

    }
}
