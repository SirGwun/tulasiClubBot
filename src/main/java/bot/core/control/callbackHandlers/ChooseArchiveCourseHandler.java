package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Возвращает клавиатуру со всеми курсами
 */
public class ChooseArchiveCourseHandler extends AbstractCallbackHandler {
    Logger log = LoggerFactory.getLogger(ChooseArchiveCourseHandler.class);

    public ChooseArchiveCourseHandler() {
        super(Action.archive);
    }

    @Override
    public void handle(Update update) {
        Map<Integer, String> tagMap = Main.dataUtils.getTagMap();
        //удаляем из мапы актуальный курс (тег)
        tagMap.remove(Main.dataUtils.getTagId(Main.dataUtils.getGroupTag()));


        List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : tagMap.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData(Action.chooseTag + "_"  + entry.getKey());
            buttonRows.add(Collections.singletonList(button));
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(buttonRows);

        log.debug("user {} in process chooseCourse button", update.getCallbackQuery().getFrom().getUserName());
        ChatUtils.sendInlineKeyboard(update.getCallbackQuery().getFrom().getId(),
                "Выберете интересующий вас курс",
                keyboardMarkup);
    }
}
