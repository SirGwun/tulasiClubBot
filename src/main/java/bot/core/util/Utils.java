package bot.core.util;

import bot.core.control.callbackHandlers.Action;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class Utils {

    public static int firstPositiveNumber(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (Character.isDigit(s.charAt(i))) {
                int start = i;
                // collect the full digit sequence
                while (i < n && Character.isDigit(s.charAt(i))) i++;
                // check that the sequence is not preceded by a minus sign
                if (start == 0 || s.charAt(start - 1) != '-') {
                    int value = Integer.parseInt(s.substring(start, i));
                    if (value > 0) return value;   // positive number found
                }
            }
        }
        return -1;
    }

    static InlineKeyboardButton createButton(String text, String callback) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callback);
        return button;
    }

    /**
     * Create button used to confirm forwarded payment.
     * Callback format: {@code confirm_<messageId>_<userId>}.
     */
    static InlineKeyboardButton createConfirmButton(int messageId, long userId) {
        return createButton("Принимаю", Action.confirm.toString() + "_" + messageId + "_" + userId);
    }

    /**
     * Create button used to decline forwarded payment.
     * Callback format: {@code decline_<messageId>_<userId>}.
     */
    static InlineKeyboardButton createDeclineButton(int messageId, long userId) {
        return createButton("Отказываю", Action.decline.toString() + "_" + messageId + "_" + userId);
    }

    static int getColumnCount(int size) {
        if (size <= 10) {
            return 1;
        } else if (size <= 20) {
            return 2;
        }
        return 3;
    }
}
