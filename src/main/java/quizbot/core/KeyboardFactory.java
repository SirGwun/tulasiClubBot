package quizbot.core;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import quizbot.model.AnswerScale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class KeyboardFactory {
    public static InlineKeyboardMarkup answerButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText(AnswerScale.fromValue(i).getDescription());
            b.setCallbackData("ANSWER_" + i);
            row.add(b);
            rows.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    InlineKeyboardMarkup mainMenu() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton about = new InlineKeyboardButton();
        about.setText("Помощь");
        about.setCallbackData("HELP");
        rows.add(Collections.singletonList(about));

        InlineKeyboardButton doshaTest = new InlineKeyboardButton();
        doshaTest.setText("Тест на определение доши");
        doshaTest.setCallbackData("startTest_doshaTest");
        rows.add(Collections.singletonList(doshaTest));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
