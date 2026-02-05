package bot.core.control.handlers.telegram.commands;

import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.Commands;
import bot.core.model.input.TelegramUpdate;
import org.springframework.stereotype.Service;

@Service
public class MenuCommandHandler implements RoutedHandler<Commands, TelegramUpdate> {
    @Override
    public Commands routeKey() {
        return Commands.menu;
    }

    @Override
    public void handle(TelegramUpdate input) {

    }
}
