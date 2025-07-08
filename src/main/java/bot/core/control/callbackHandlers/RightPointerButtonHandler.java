package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import bot.core.util.Utils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Comparator;
import java.util.List;

public class RightPointerButtonHandler implements CallbackHandler {
    @Override
    public Action getAction() {
        return Action.rightArrow;
    }

    @Override
    public String getFormat() {
        return getAction() + "_tagId_<lastShownIndex>";
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
        String[] data = update.getCallbackQuery().getData().split("_");
        String tag = Main.dataUtils.getTagMap().get(Integer.parseInt(data[1]));
        Long userId = update.getCallbackQuery().getFrom().getId();

        List<InlineKeyboardButton> buttons = ChatUtils.getTagetButtonList(
                Action.chooseGroup,
                userId,
                tag);
        buttons.sort(Comparator.comparingInt(button -> Utils.firstPositiveNumber(button.getText())));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(ChatUtils.arrowedStyleKeyboard(
                buttons,
                tag,
                Integer.parseInt(data[2]),
                Action.rightArrow));

        ChatUtils.sendInlineKeyboard(
                userId,
                "Выберете интересующую вас группу",
                keyboard);
    }
}
