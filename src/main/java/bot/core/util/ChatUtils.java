package bot.core.util;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.control.callbackHandlers.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import bot.core.model.Group;

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
    public static void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        execute(sendMessage);
    }

    /**
     * Send a message with inline keyboard.
     */
    public static void sendInlineKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        execute(sendMessage);
    }

    public static void sendMainMenu(long chatId) {
        String text = """
        Здравствуйте!
        Вас приветствует, бот-помощник курсов
        Школы Аюрведы и здорового образа жизни "Tulasi"

        Вы находитесь в главном меню: 
        Если вы хотите узнать подробнее о курсах, получить инструкции или выбрать курс — 
        воспользуйтесь кнопками ниже.
        """;
        String paymentInfo = Main.dataUtils.getPaymentInfo();
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton coursesDescription = new InlineKeyboardButton("Описание курсов и лекций");
        coursesDescription.setCallbackData(Action.getCourseDescription + "_" + chatId);

        InlineKeyboardButton getInstruction = new InlineKeyboardButton("Инструкция");
        getInstruction.setCallbackData(Action.getInstruction + "_" + chatId);

        InlineKeyboardButton getPaymentInstruction = new InlineKeyboardButton("Способы оплаты");
        getPaymentInstruction.setCallbackData(Action.getPaymentInstruction + "_" + chatId);

        InlineKeyboardButton chooseCourse = new InlineKeyboardButton("Выбрать курс");
        chooseCourse.setCallbackData(Action.chooseCourse + "_" + chatId);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(Arrays.asList(coursesDescription, getInstruction, getPaymentInstruction));
        rows.add(Collections.singletonList(chooseCourse));
        keyboardMarkup.setKeyboard(rows);

        log.debug("Sent main menu to {}", chatId);
        sendInlineKeyboard(chatId, text, keyboardMarkup);
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
    public static InlineKeyboardMarkup getAllGroupKeyboard(Action callBack, Long userId) {
       return getTaggedGroupKeyboard(callBack, userId, null);
    }

    public static InlineKeyboardMarkup getTaggedGroupKeyboard(Action callBack, Long userId, String tag) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (Group group : Main.dataUtils.getGroupList()) {
            if (group.getTag().equals(tag) || tag == null) {
                String groupName = group.getName();
                Long groupId = group.getId();
                if (isBotAdminInGroup(groupId)) {
                    buttons.add(createButton(groupName, callBack + "_" + groupId));
                } else if (Main.dataUtils.getAdminId() == userId) {
                    buttons.add(createButton("!" + groupName, callBack + "_" + groupId));
                }
            }
        }

        buttons.sort(Comparator.comparing(InlineKeyboardButton::getText));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(distributeButtons(buttons));
        return keyboard;
    }

    public static InlineKeyboardMarkup getAllTagKeyboard(Action callback) {
        Map<Integer, String> tags = Main.dataUtils.getTagMap();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : tags.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(entry.getValue());
            button.setCallbackData(callback + "_" + entry.getKey());
            buttons.add(Collections.singletonList(button));
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(buttons);
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
        return createButton("Принимаю", Action.confirm.toString() + "_" + messageId + "_" + userId);
    }

    /**
     * Create button used to decline forwarded payment.
     * Callback format: {@code decline_<messageId>_<userId>}.
     */
    private static InlineKeyboardButton createDeclineButton(int messageId, long userId) {
        return createButton("Отказываю", Action.decline.toString() + "_" + messageId + "_" + userId);
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
            String historyLink = getJoinRequestedLink(groupId, groupName);
            sendToHistoryChat(userName, groupName, historyLink, reason);

            String userInviteLink = createOneTimeInviteLink(groupId);
            sendInviteToUser(userId, groupId, groupName, userInviteLink);
        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу {}", e.getMessage());
        }
    }

    private static String getJoinRequestedLink(Long groupId, String groupName) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setCreatesJoinRequest(true);
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
        msg.setChatId(Main.dataUtils.getHistoryId());
        msg.setText(message);
        msg.setParseMode("HTML");
        msg.setDisableWebPagePreview(true);

        Main.bot.execute(msg);
    }

    private static void sendInviteToUser(long userId, long groupId, String groupName, String link) throws TelegramApiException {
        String messageText =
                "Для присоединения к группе перейдите по ссылке ниже:\n\n" +
                        "<a href=\"" + link + "\">" + groupName + " — мы рады вас видеть!</a>\n\n" +
                        "После того как вы воспользуетесь этой ссылкой, группа появится во вкладке \"Все чаты\".\n" +
                        "Если не можете её найти — воспользуйтесь кнопкой ниже.";

        SendMessage msg = new SendMessage();
        msg.setChatId(userId);
        msg.setText(messageText);
        msg.setParseMode("HTML");

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Я вступил, но не могу найти группу");
        button.setCallbackData(Action.getJoinRequestedLink.toString() + "_"
                + getJoinRequestedLink(groupId, groupName) + "_"
                + userId);

        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        replyMarkup.setKeyboard(List.of(List.of(button)));

        msg.setReplyMarkup(replyMarkup);

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

