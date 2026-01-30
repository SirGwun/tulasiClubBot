package bot.core.control.callbackHandlers;

import bot.core.Legacy;
import bot.core.control.rout.classify.enums.Callbacks;
import bot.core.util.ChatUtils;
import bot.core.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;

public class RightPointerButtonHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(RightPointerButtonHandler.class);
    @Override
    public Callbacks getAction() {
        return Callbacks.rightArrow;
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
        String tag = Legacy.dataUtils.getTagMap().get(Integer.parseInt(data[1]));
        Long userId = update.getCallbackQuery().getFrom().getId();

        List<InlineKeyboardButton> buttons = ChatUtils.getTagetButtonList(
                Callbacks.chooseGroup,
                userId,
                tag);
        buttons.sort(Comparator.comparingInt(button -> Utils.firstPositiveNumber(button.getText())));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(ChatUtils.arrowedStyleKeyboard(
                buttons,
                tag,
                Integer.parseInt(data[2]),
                Callbacks.rightArrow));


        var msg = update.getCallbackQuery().getMessage();
        try {
            ChatUtils.updateMessageWithKeyboard(
                    msg.getChatId(),
                    msg.getMessageId(),
                    "Выберите интересующую вас группу",
                    keyboard
            );
        } catch (TelegramApiException e) {
            log.error("Ошибка при обновлении списка листанием {}", e.getMessage());
        }
    }
}
