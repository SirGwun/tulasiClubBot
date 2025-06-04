package bot.core.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
    public static List<String> splitMessage(String text, int maxLength) {
        List<String> messages = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());

            if (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
                int lastSpace = text.lastIndexOf(' ', end);
                int lastNewLine = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastSpace, lastNewLine);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }

            messages.add(text.substring(start, end));
            start = end;

            while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
                start++;
            }
        }
        return messages;
    }

    public static boolean hasExceptedGroup(InlineKeyboardMarkup allGroupKeyboard) {
        boolean hasGroupException = false;
        for (List<InlineKeyboardButton> row : allGroupKeyboard.getKeyboard()) {
            for (InlineKeyboardButton button : row) {
                if (button.getText().startsWith("!")) {
                    hasGroupException = true;
                }
            }
        }
        return hasGroupException;
    }
}
