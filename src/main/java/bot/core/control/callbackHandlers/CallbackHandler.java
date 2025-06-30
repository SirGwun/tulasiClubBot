package bot.core.control.callbackHandlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackHandler {
    public String getFormat();
    public boolean match(Update update);
    public boolean isFormatCorrect(String callback);
    public void handle(Update update);
}
