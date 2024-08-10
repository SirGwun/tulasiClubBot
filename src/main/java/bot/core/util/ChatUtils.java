package bot.core.util;

import bot.core.Group;
import bot.core.Main;
import bot.core.PaymentBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    public static InlineKeyboardMarkup getAllGroupKeyboard(long userId, String callBack) {
        Properties groupList = ConfigUtils.getGroupList();

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (Map.Entry<Object, Object> group : groupList.entrySet()) {
            if (userId == ConfigUtils.getAdminID()) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                String groupName = group.getKey().toString().replaceAll("-", " ");
                if (GroupUtils.isBotAdminInGroup(group.getValue().toString())) {
                    button.setText(groupName);
                } else {
                    button.setText("!" + groupName);
                }
                button.setCallbackData(callBack + "_" + group.getValue().toString());
                buttons.add(button);
            } else if (GroupUtils.isBotAdminInGroup(group.getValue().toString())) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                String groupName = group.getKey().toString().replaceAll("-", " ");
                button.setText(groupName);
                button.setCallbackData(callBack + "_" + group.getValue().toString());
                buttons.add(button);
            }

        }

        // Сортировка кнопок по имени
        buttons.sort(Comparator.comparing(InlineKeyboardButton::getText));

        int columnCount;
        if (buttons.size() <= 10) {
            columnCount = 1;
        } else if (buttons.size() <= 20) {
            columnCount = 2;
        } else {
            columnCount = 3;
        }
        // Формирование строк для клавиатуры
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Разбиение кнопок на столбцы и строки
        int totalRows = (int) Math.ceil((double) buttons.size() / columnCount);
        for (int rowIndex = 0; rowIndex < totalRows; rowIndex++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                int buttonIndex = rowIndex + colIndex * totalRows;
                if (buttonIndex < buttons.size()) {
                    row.add(buttons.get(buttonIndex));
                }
            }
            rows.add(row);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public static InlineKeyboardMarkup getKonfirmAdminStatusKeyboard(Group group) {
        InlineKeyboardMarkup keyboaed = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Бот администратора в " + PaymentBot.getNewGroupName().replace("-", " "));
        button.setCallbackData("confirmAdmin_" + group.getId());
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        rows.add(row);
        keyboaed.setKeyboard(rows);
        return keyboaed;
    }

    public static void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            Main.bot.execute(deleteMessage);
            Main.log.info("Deleted message {}", messageId);
        } catch (TelegramApiException e) {
            Main.log.error("Ошибка при удалении сообщения", e);
        }
    }
}
