package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ChooseCourseHandler implements CallbackHandler {
    Logger log = LoggerFactory.getLogger(ChooseCourseHandler.class);
    @Override
    public boolean match(Update update) {
        if (!update.hasCallbackQuery()) return false;
        String[] data = update.getCallbackQuery().getData().split("_");
        String action = data[0];
        return action.equalsIgnoreCase("chooseCourse");
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        String action = data[0];
        return data.length == 2 && action.equalsIgnoreCase("chooseCourse");
    }

    @Override
    public void handle(Update update) {
        Map<Integer, String> tagMap = Main.dataUtils.getTagMap();
        List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : tagMap.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData("chooseTag_"  + entry.getKey());
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(buttonRows);

        log.debug("user {} in process chooseCourse button", update.getCallbackQuery().getFrom().getUserName());
        ChatUtils.sendInlineKeyboard(update.getCallbackQuery().getFrom().getId(),
                "Выберете интересующий вас курс",
                keyboardMarkup);
    }

    @Override
    public String getFormat() {
        return "chooseCourse_<chatId>";
    }
}
