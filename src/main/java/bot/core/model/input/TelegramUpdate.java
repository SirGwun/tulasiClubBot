package bot.core.model.input;

import bot.core.control.rout.classify.enums.InputSources;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

/**
 * Telegram Update adapter
 */
public class TelegramUpdate implements Input {
    Update update;

    public TelegramUpdate(Update update) {
        this.update = update;
    }

    @Override
    public InputSources source() {
        return InputSources.TELEGRAM;
    }

    public boolean hasMessage() {
        return update.hasMessage();
    }

    public Message getMessage() {
        return update.getMessage();
    }

    public boolean hasCallback() {
        return update.hasCallbackQuery();
    }

    public CallbackQuery getCallbackQuery() {
        return update.getCallbackQuery();
    }

    public boolean hasMyChatMember() {
        return update.hasMyChatMember();
    }

    public ChatMemberUpdated getMyChatMemberUpdated() {
        return update.getMyChatMember();
    }
}
