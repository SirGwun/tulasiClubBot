package bot.core.control.callbackHandlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackHandler {
    Action getAction();
    String getFormat();
    boolean match(Update update);
    boolean isFormatCorrect(String callback);
    void handle(Update update);
}
