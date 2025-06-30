package bot.core.control.callbackHandlers;

import bot.core.util.ChatUtils;
import bot.core.control.callbackHandlers.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
* Срабатывает когда нажимают на кнопку "Выбрать курс"
 */
public class ChooseTagHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ChooseTagHandler.class);

    @Override
    public boolean match(Update update) {
        if (!update.hasCallbackQuery()) return false;
        String[] data = update.getCallbackQuery().getData().split("_");
        String action = data[0];
        return action.equalsIgnoreCase(Action.chooseTag.toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        return data.length == 2 && data[0].equalsIgnoreCase(Action.chooseTag.toString());
    }

    @Override
    public void handle(Update update) {
        String[] data = update.getCallbackQuery().getData().split("_");
        String action = data[0];
        String tag = data[1];
        User user = update.getCallbackQuery().getFrom();

        log.debug("User {} press chooseTag button", user.getUserName());
        ChatUtils.sendInlineKeyboard(
                user.getId(),
                "Выберете интересующую вас группу",
                ChatUtils.getTaggedGroupKeyboard(Action.setGroup.toString(), user.getId(), tag));
    }

    @Override
    public String getFormat() {
        return "chooseTag_<tag>";
    }
}
