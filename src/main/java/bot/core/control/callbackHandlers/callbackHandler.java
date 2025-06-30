package bot.core.control.callbackHandlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface callbackHandler {
    public boolean match(Update update);
    public boolean isFormatCorrect(String callback);
    public boolean handle(Update update);
    public String getFormat();
}
