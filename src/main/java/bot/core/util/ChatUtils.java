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
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry<Object, Object> group: groupList.entrySet()) {
            if (GroupUtils.isBotAdminInGroup(group.getValue().toString()) || userId == ConfigUtils.getAdminID()) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                if (GroupUtils.isBotAdminInGroup(group.getValue().toString()))
                    button.setText(group.getKey().toString().replaceAll("-", " "));
                else {
                    button.setText("!" + group.getKey().toString().replaceAll("-", " "));
                    ChatUtils.sendMessage(userId, "В группах, отмеченых знаком ! бот либо не состоит либо не являеться админстратором" +
                            "\nРекомендуеться удалить эти группы при помощи команды /del");
                }
                button.setCallbackData(callBack + "_" + group.getValue().toString());
                row.add(button);
                rows.add(row);
            } else {
                Main.log.info("Бот не является администратором в группе {}", group.getValue().toString().replaceAll("-", " "));
            }
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
