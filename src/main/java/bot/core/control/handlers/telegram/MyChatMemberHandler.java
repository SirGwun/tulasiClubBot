package bot.core.control.handlers.telegram;

import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.TgUpdateTypes;
import bot.core.model.input.TelegramUpdate;

public class MyChatMemberHandler implements RoutedHandler<TgUpdateTypes, TelegramUpdate> {
    @Override
    public TgUpdateTypes routeKey() {
        return TgUpdateTypes.MY_CHAT_MEMBER;
    }

    @Override
    public void handle(TelegramUpdate input) {

    }
}
