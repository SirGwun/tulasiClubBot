package bot.core.control.rout.classify;

import bot.core.control.rout.AbstractClassifier;
import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.TgMessageTypes;
import bot.core.model.input.TelegramUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class TgMessageClassifier extends AbstractClassifier<TelegramUpdate, TgMessageTypes> {

    public TgMessageClassifier(List<RoutedHandler<TgMessageTypes, TelegramUpdate>> children) {
        super(TgMessageTypes.class, children);
    }

    @Override
    public TgMessageTypes classify(TelegramUpdate input) {
        Message message = input.getMessage();
        if (message.hasText() && message.getText().startsWith("/")) return TgMessageTypes.COMMAND;
        if (message.hasPhoto()) return TgMessageTypes.IMAGE;
        if (message.hasDocument()) return TgMessageTypes.DOCUMENT;
        if (message.hasText()) return TgMessageTypes.PLAIN_TEXT;
        throw new IllegalArgumentException("Unknown message type " + message);
    }
}
