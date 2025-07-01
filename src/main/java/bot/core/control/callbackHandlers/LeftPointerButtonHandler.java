package bot.core.control.callbackHandlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public class LeftPointerButtonHandler implements CallbackHandler {
    @Override
    public Action getAction() {
        return Action.lefrPointer;
    }

    @Override
    public String getFormat() {
        return getAction() + "_tagId_<firstShownIndex>";
    }

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(getAction().toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        return data[0].equalsIgnoreCase(getAction().toString()) && data.length == 3;
    }

    @Override
    public void handle(Update update) {

    }
}
