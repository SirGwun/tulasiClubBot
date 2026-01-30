package bot.core.control.handlers.telegram;

import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.Commands;
import bot.core.model.input.TelegramUpdate;

public class StartCommandHandler implements RoutedHandler<Commands, TelegramUpdate> {
    @Override
    public Commands routeKey() {
        return Commands.start;
    }

    @Override
    public void handle(TelegramUpdate input) {

    }
}
