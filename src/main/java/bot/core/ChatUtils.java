package bot.core;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

public class ChatUtils {

    public static void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            Main.bot.execute(sendMessage);
            Main.log.info("Отправлено сообщение для {}", sendMessage.getChatId());
        }
        catch (TelegramApiException e) {
            Main.log.error("Ошибка при отправке сообщения {}", e.getMessage());
        }
    }

    public static InlineKeyboardMarkup getValidationKeyboard(int messageId, long userId) {
        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("Принимаю");
        confirmButton.setCallbackData("confirm_" + messageId + "_" + userId);

        InlineKeyboardButton declineButton = new InlineKeyboardButton();
        declineButton.setText("Отказываю");
        declineButton.setCallbackData("decline_" + messageId + "_" + userId);

        List<InlineKeyboardButton> row = Arrays.asList(confirmButton, declineButton);
        List<List<InlineKeyboardButton>> rows = Arrays.asList(row);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        return keyboard;
    }
}
