package bot.core.control.callbackHandlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class AbstractCallbackHandler implements CallbackHandler {
    /** Константа действия, специфичная для подкласса */
    protected final Action action;
    protected final int argumentCount;

    protected AbstractCallbackHandler(Action action, int argumentCount) {
        this.action = action;
        this.argumentCount = argumentCount;
    }

    @Override
    public String getFormat() {
        return action + "_<chatId>";
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public boolean match(Update update) {
        if (!update.hasCallbackQuery()) return false;
        return update.getCallbackQuery().getData().startsWith(action.toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] parts = callback.split("_");
        return parts.length == 2 && action.toString().equalsIgnoreCase(parts[0]);
    }


    /** Подкласс заполняет только логику реакции */
    @Override
    public abstract void handle(Update update);
}

