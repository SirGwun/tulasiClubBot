package bot.core.control.callbackHandlers.groupNavigation;

import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import bot.core.control.callbackHandlers.Action;
import bot.core.util.ChatUtils;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class GetCoueseList extends AbstractCallbackHandler {

    public GetCoueseList() {
        super(Action.getCourseList);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();

        InlineKeyboardMarkup keyboardMarkup = ChatUtils.getAllTagKeyboard(Action.chooseTag);

        LoggerFactory.getLogger(GetCoueseList.class)
                .debug("user {} in process chooseCourse button",
                        update.getCallbackQuery().getFrom().getUserName());
        ChatUtils.sendInlineKeyboard(userId,
                "Выберете актуальный курс",
                keyboardMarkup);
    }
}
