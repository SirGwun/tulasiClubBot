package bot.core.control.callbackHandlers;

import bot.core.control.rout.classify.enums.Callbacks;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackHandler {
    Callbacks getAction();
    String getFormat();
    boolean match(Update update);
    boolean isFormatCorrect(String callback);
    void handle(Update update);
}
