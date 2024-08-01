package bot.core.util;

import bot.core.Group;
import bot.core.Main;
import bot.core.PaymentBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static InlineKeyboardMarkup getAllGroupKeyboard(long userId) {
        Properties groupList = ConfigUtils.getGroupList();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry<Object, Object> group: groupList.entrySet()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getKey().toString());
            button.setCallbackData("setGroup_" + group.getValue());
            System.out.println(button.getCallbackData());
            row.add(button);
            rows.add(row);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public static InlineKeyboardMarkup getKonfirmAdminStatusKeyboard(Group group) {
        InlineKeyboardMarkup keyboaed = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Подтверждаю, что бот - администратор в группе " + PaymentBot.getNewGroupName());
        button.setCallbackData("confirmAdmin_" + group.getId());
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        rows.add(row);
        keyboaed.setKeyboard(rows);
        return keyboaed;
    }
}
