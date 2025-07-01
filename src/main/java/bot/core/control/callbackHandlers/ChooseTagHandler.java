package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;

/**
* Срабатывает когда нажимают на кнопку того или иного курса
 */
public class ChooseTagHandler extends AbstractCallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ChooseTagHandler.class);

    public ChooseTagHandler() {
        super(Action.chooseTag, 2);
    }

    @Override
    public String getFormat() {
        return "chooseTag_<tag>";
    }

    @Override
    public void handle(Update update) {
        String[] data = update.getCallbackQuery().getData().split("_");
        String tagId = data[1];
        User user = update.getCallbackQuery().getFrom();
        Map<Integer, String> tagMap = Main.dataUtils.getTagMap();
        String tag = tagMap.get(Integer.parseInt(tagId));

        log.debug("User {} press chooseTag button", user.getUserName());

        InlineKeyboardMarkup keyboard = ChatUtils.getTaggedGroupKeyboard(Action.chooseGroup, user.getId(), tag);
        if (keyboard.getKeyboard().isEmpty()) {
            log.warn("Вызов getTaggedGroupKeyboard без доступных групп");
            ChatUtils.sendMessage(user.getId(), "На данный момент нет доступных групп");
        } else {
            ChatUtils.sendInlineKeyboard(
                    user.getId(),
                    "Выберете интересующую вас группу",
                    keyboard);
        }

    }
}
