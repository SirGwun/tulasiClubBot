package bot.core.control.rout.classify;

import bot.core.control.rout.AbstractClassifier;
import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.Commands;
import bot.core.model.input.TelegramUpdate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandClassifier extends AbstractClassifier<TelegramUpdate, Commands> {

    public CommandClassifier(List<RoutedHandler<Commands, TelegramUpdate>> children) {
        super(Commands.class, children);
    }

    @Override
    public Commands classify(TelegramUpdate input) {
        String callbackData = input.getMessage().getText();
        String key = callbackData.split(" ")[0];
        try {
            return Commands.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return Commands.uncnown;
        }
    }
}
