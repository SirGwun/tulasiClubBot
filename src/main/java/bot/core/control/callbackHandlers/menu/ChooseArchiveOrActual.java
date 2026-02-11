package bot.core.control.callbackHandlers.menu;

import bot.core.Main;
import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import bot.core.control.callbackHandlers.Action;
import bot.core.util.ChatUtils;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChooseArchiveOrActual extends AbstractCallbackHandler {

    public ChooseArchiveOrActual() {
        super(Action.chooseArchiveOrActual);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        int actualTag = Main.dataUtils.getTagId(Main.dataUtils.getGroupTag());

        List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();

        InlineKeyboardButton actualButton = new InlineKeyboardButton("Текущий курс");
        actualButton.setCallbackData(Action.chooseTag + "_"  + actualTag);

        InlineKeyboardButton archiveButton = new InlineKeyboardButton("Архив");
        archiveButton.setCallbackData(Action.archive + "_" + userId);

        buttonRows.add(Collections.singletonList(actualButton));
        buttonRows.add(Collections.singletonList(archiveButton));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(buttonRows);

        LoggerFactory.getLogger(ChooseArchiveOrActual.class)
                .debug("user {} in process chooseCourse button",
                        update.getCallbackQuery().getFrom().getUserName());
        ChatUtils.sendInlineKeyboard(userId,
                "Выберете актуальный курс или архивный",
                keyboardMarkup);
    }
}
