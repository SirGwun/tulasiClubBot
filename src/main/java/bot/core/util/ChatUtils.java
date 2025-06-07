package bot.core.util;

import bot.core.Main;
import bot.core.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

/** Utility methods for interacting with chats. */
public final class ChatUtils {
    private static final Logger log = LoggerFactory.getLogger(ChatUtils.class);

    private ChatUtils() {
        // utility class
    }

    /**
     * Send a simple text message to chat.
     */
    public static void sendMessage(long chatId, String message) {
        SendMessage sendMessage = createMessage(chatId, message);
        execute(sendMessage);
    }

    /**
     * Send a message with inline keyboard.
     */
    public static void sendInlineKeyboard(long chatId, String message, InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
        execute(sendMessage);
    }

    private static SendMessage createMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    private static void execute(SendMessage message) {
        try {
            Main.bot.execute(message);
            log.info("Отправлено сообщение для {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения {}", e.getMessage());
        }
    }

    /**
     * Keyboard with confirmation/decline buttons for payment check.
     */
    public static InlineKeyboardMarkup getValidationKeyboard(int messageId, long userId) {
        InlineKeyboardButton confirm = createConfirmButton(messageId, userId);
        InlineKeyboardButton decline = createDeclineButton(messageId, userId);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(Collections.singletonList(Arrays.asList(confirm, decline)));
        return keyboard;
    }

    /**
     * Keyboard containing all groups available for user.
     */
    public static InlineKeyboardMarkup getAllGroupKeyboard(long userId, String callBack) {
        Properties groupList = DataUtils.getGroupList();

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Map.Entry<Object, Object> group : groupList.entrySet()) {
            String groupId = group.getValue().toString();
            boolean botIsAdmin = GroupUtils.isBotAdminInGroup(groupId);
            if (userId == DataUtils.getAdminID() || botIsAdmin) {
                String groupName = group.getKey().toString();
                switch (callBack) {
                    case "setGroup" -> buttons.add(createSetGroupButton(groupName, groupId, botIsAdmin));
                    case "delGroup" -> buttons.add(createDeleteGroupButton(groupName, groupId, botIsAdmin));
                    default -> buttons.add(createGroupButton(groupName, groupId, callBack, botIsAdmin));
                }
            }
        }

        buttons.sort(Comparator.comparing(InlineKeyboardButton::getText));

        int columnCount = getColumnCount(buttons.size());
        List<List<InlineKeyboardButton>> rows = distributeButtons(buttons, columnCount);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private static InlineKeyboardButton createButton(String text, String callback) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callback);
        return button;
    }

    /**
     * Create button used to confirm forwarded payment.
     * Callback format: {@code confirm_<messageId>_<userId>}.
     */
    private static InlineKeyboardButton createConfirmButton(int messageId, long userId) {
        return createButton("Принимаю", "confirm_" + messageId + "_" + userId);
    }

    /**
     * Create button used to decline forwarded payment.
     * Callback format: {@code decline_<messageId>_<userId>}.
     */
    private static InlineKeyboardButton createDeclineButton(int messageId, long userId) {
        return createButton("Отказываю", "decline_" + messageId + "_" + userId);
    }

    /**
     * Create button to choose a group for /set_group command.
     * Callback format: {@code setGroup_<groupId>}.
     */
    private static InlineKeyboardButton createSetGroupButton(String name, String id, boolean isAdmin) {
        return createGroupButton(name, id, "setGroup", isAdmin);
    }

    /**
     * Create button to remove a group with /del command.
     * Callback format: {@code delGroup_<groupId>}.
     */
    private static InlineKeyboardButton createDeleteGroupButton(String name, String id, boolean isAdmin) {
        return createGroupButton(name, id, "delGroup", isAdmin);
    }

    /**
     * Create button for confirming admin rights of the bot in a group.
     * Callback format: {@code confirmAdmin_<groupId>}.
     */
    private static InlineKeyboardButton createConfirmAdminButton(Group group) {
        return createButton(
                "Бот администратор в " + group.getName().replace("-", " "),
                "confirmAdmin_" + group.getId()
        );
    }

    private static InlineKeyboardButton createGroupButton(String name, String id, String callBack, boolean isAdmin) {
        String groupName = name.replace("-", " ");
        String title = isAdmin ? groupName : "!" + groupName;
        return createButton(title, callBack + "_" + id);
    }

    private static int getColumnCount(int size) {
        if (size <= 10) {
            return 1;
        } else if (size <= 20) {
            return 2;
        }
        return 3;
    }

    private static List<List<InlineKeyboardButton>> distributeButtons(List<InlineKeyboardButton> buttons, int columnCount) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        int totalRows = (int) Math.ceil((double) buttons.size() / columnCount);
        for (int rowIndex = 0; rowIndex < totalRows; rowIndex++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                int idx = rowIndex + colIndex * totalRows;
                if (idx < buttons.size()) {
                    row.add(buttons.get(idx));
                }
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Keyboard to confirm admin rights of bot in group.
     */
    public static InlineKeyboardMarkup getConfirmAdminStatusKeyboard(Group group) {
        InlineKeyboardButton button = createConfirmAdminButton(group);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(Collections.singletonList(Collections.singletonList(button)));
        return keyboard;
    }

    /**
     * Delete message in chat.
     */
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

