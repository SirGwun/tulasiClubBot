package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import bot.core.control.callbackHandlers.Action;
import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChooseCourseHandler extends AbstractCallbackHandler {
    Logger log = LoggerFactory.getLogger(ChooseCourseHandler.class);

    public ChooseCourseHandler() {
        super(Action.chooseCourse, 2);
    }

    @Override
    public void handle(Update update) {
        Map<Integer, String> tagMap = Main.dataUtils.getTagMap();
        List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : tagMap.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData(Action.chooseTag.toString() + "_"  + entry.getKey());
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(buttonRows);

        log.debug("user {} in process chooseCourse button", update.getCallbackQuery().getFrom().getUserName());
        ChatUtils.sendInlineKeyboard(update.getCallbackQuery().getFrom().getId(),
                "Выберете интересующий вас курс",
                keyboardMarkup);
    }

}
