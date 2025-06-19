package bot.core.util;

import bot.core.Main;
import bot.core.control.SessionState;
import bot.core.model.SessionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.xml.crypto.Data;
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
    public static InlineKeyboardMarkup getAllGroupKeyboard(String callBack, Long userId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (Map.Entry<String, Long> entry : Main.dataUtils.getGroupMap().entrySet()) {
            String groupName = entry.getKey();
            Long groupId = entry.getValue();

            if (isBotAdminInGroup(groupId)) {
                buttons.add(createButton(groupName, callBack + "_" + groupId));
            } else if (Main.dataUtils.getAdminId() == userId) {
                buttons.add(createButton("!" + groupName, callBack + "_" + groupId));
            }
        }

        buttons.sort(Comparator.comparing(InlineKeyboardButton::getText));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(distributeButtons(buttons));
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


    private static InlineKeyboardButton createGroupButton(String groupName, Long id, String callBack) {
        return createButton(groupName, callBack + "_" + id);
    }

    private static int getColumnCount(int size) {
        if (size <= 10) {
            return 1;
        } else if (size <= 20) {
            return 2;
        }
        return 3;
    }

    private static List<List<InlineKeyboardButton>> distributeButtons(List<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        int columnCount = getColumnCount(buttons.size());

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

    public static void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            Main.bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при удалении сообщения", e);
        }
    }

    public static boolean isBotAdminInGroup(Long groupId) {
        try {
            // Проверяем, что группа существует, получив количество участников
            GetChatMemberCount getChatMemberCount = new GetChatMemberCount(String.valueOf(groupId));
            int memberCount = Main.bot.execute(getChatMemberCount);
            if (memberCount > 0) {
                // Получаем список администраторов группы
                GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
                getChatAdministrators.setChatId(groupId);
                List<ChatMember> admins = Main.bot.execute(getChatAdministrators);

                // Проверяем, есть ли бот среди администраторов
                String botUsername = Main.dataUtils.getBotName();
                for (ChatMember admin : admins) {
                    if (admin.getUser().getUserName().equals(botUsername)) {
                        return true; // Бот является администратором в группе
                    }
                }
            }
        } catch (TelegramApiException e) {
            log.info("Бот не адмистратор в группе {}", groupId);
        }
        return false; // Группа не существует или бот не является администратором
    }

    public static void addInGroup(long userId, Long groupId, String reason) {
        String groupName = Main.dataUtils.getGroupName(groupId);
        if (groupName == null) {
            log.error("Попытка добавить в неизвестную группу {}", groupId);
            return;
        }

        String userName = SessionController.getInstance()
                .getUserSession(userId)
                .getUserName();

        try {
            String historyLink = createInviteLink(groupId, groupName, true);
            sendToHistoryChat(userName, groupName, historyLink, reason);

            String userInviteLink = createOneTimeInviteLink(groupId);
            sendInviteToUser(userId, groupName, userInviteLink);

            // TODO: детектировать переход по ссылке и сообщать, где найти группу

        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу \n {}", e.getMessage());
        }
    }

    private static String createInviteLink(Long groupId, String groupName, boolean joinRequest) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setName(groupName);
        link.setCreatesJoinRequest(joinRequest);
        return Main.bot.execute(link).getInviteLink();
    }

    private static String createOneTimeInviteLink(Long groupId) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setName("Присоединиться к курсу");
        link.setExpireDate(0); // бессрочно
        link.setMemberLimit(1); // одноразовая
        return Main.bot.execute(link).getInviteLink();
    }

    private static void sendToHistoryChat(String userName, String groupName, String link, String reason) throws TelegramApiException {
        String message = "Пользователю @" + userName + " отправлено приглашение в группу " +
                "<a href=\"" + link + "\">" + groupName + "</a>\nПричина: " + reason;

        SendMessage msg = new SendMessage();
        msg.setChatId(Main.dataUtils.getHistroyId());
        msg.setText(message);
        msg.setParseMode("HTML");

        Main.bot.execute(msg);
    }

    private static void sendInviteToUser(long userId, String groupName, String link) throws TelegramApiException {
        String messageText = "Для присоединения к группе перейдите по ссылке ниже:\n\n" +
                "<a href=\"" + link + "\">" + groupName
                + " - Мы рады вас видеть!" + "</a>";

        SendMessage msg = new SendMessage();
        msg.setChatId(userId);
        msg.setText(messageText);
        msg.setParseMode("HTML");

        Main.bot.execute(msg);
    }

    public static void sendPhoto(SendPhoto sendPhoto) {
        try {
            Main.bot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке фото {}", e.getMessage());
        }
    }
}

