package bot.core.control.rout.classify;

import bot.core.control.rout.AbstractClassifier;
import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.InputSources;
import bot.core.control.rout.classify.enums.TgUpdateTypes;
import bot.core.model.input.TelegramUpdate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TgUpdateClassifier extends AbstractClassifier<TelegramUpdate, TgUpdateTypes>
        implements RoutedHandler<InputSources, TelegramUpdate> {

    public TgUpdateClassifier(List<RoutedHandler<TgUpdateTypes, TelegramUpdate>> handlers) {
        super(TgUpdateTypes.class, handlers);
    }

    @Override
    public InputSources routeKey() {
        return InputSources.TELEGRAM;
    }

    @Override
    public TgUpdateTypes classify(TelegramUpdate input) {
        if (input.hasMessage()) return TgUpdateTypes.MESSAGE;
        if (input.hasCallback()) return TgUpdateTypes.CALLBACK;
        if (input.hasMyChatMember()) return TgUpdateTypes.MY_CHAT_MEMBER;
        throw new IllegalArgumentException("There is no classification rule for " + input);
    }
}
