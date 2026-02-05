package bot.core.control.rout.classify;

import bot.core.control.rout.AbstractClassifier;
import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.Callbacks;
import bot.core.model.input.Input;
import bot.core.model.input.TelegramUpdate;

import java.util.List;

public class CallbackClassifier extends AbstractClassifier<TelegramUpdate, Callbacks> {

    public CallbackClassifier(List<RoutedHandler<Callbacks, TelegramUpdate>> children) {
        super(Callbacks.class, children);
    }

    @Override
    public Callbacks classify(TelegramUpdate input) {
        String callbackData = input.getCallbackQuery().getData();
        String key = callbackData.split("_")[0];
        try {
            return Callbacks.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return Callbacks.none;
        }
    }
}
