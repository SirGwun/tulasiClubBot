package bot.core.control.callbackHandlers.groupNavigation.paiment;

import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import bot.core.control.callbackHandlers.Action;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PayWithRussianCardButton extends AbstractCallbackHandler {
    public PayWithRussianCardButton() {
        super(Action.payWithRussianCard);
    }

    @Override
    public void handle(Update update) {
        Integer minAmount = 600;
        if (update == null || !update.hasCallbackQuery()) {
            return;
        }

        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        Action action = Action.choosePaymentAmount; // один и тот же action

        InlineKeyboardButton amount1 = new InlineKeyboardButton(minAmount + " ₽");
        amount1.setCallbackData(action + "_600");

        InlineKeyboardButton amount2 = new InlineKeyboardButton("1000 ₽");
        amount2.setCallbackData(action + "_1000");

        InlineKeyboardButton amount3 = new InlineKeyboardButton("1500 ₽");
        amount3.setCallbackData(action + "_1500");

        InlineKeyboardButton customAmount = new InlineKeyboardButton("Ввести сумму (не менее " + minAmount + " ₽)");
        customAmount.setCallbackData(action + "_custom");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Collections.singletonList(amount1));
        rows.add(Collections.singletonList(amount2));
        rows.add(Collections.singletonList(amount3));
        rows.add(Collections.singletonList(customAmount));

        keyboardMarkup.setKeyboard(rows);

        ChatUtils.sendInlineKeyboard(chatId, "Выберите сумму пожертвования: ", keyboardMarkup);
    }
}
