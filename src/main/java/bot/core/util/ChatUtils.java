package bot.core.util;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.control.callbackHandlers.Action;
import bot.core.model.BroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import bot.core.model.Group;

import java.util.*;

/** Utility methods for interacting with chats. */
public final class ChatUtils {
    private static final Logger log = LoggerFactory.getLogger(ChatUtils.class);
    public static final String ARROWED_STILE = "arrowed";
    public static final String COMMON_STILE = "common";

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
        Вы находитесь в главном меню:
        
        Если вы хотите узнать подробнее о курсах, получить инструкции или выбрать курс —
        воспользуйтесь кнопками ниже.
        """;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton coursesDescription = new InlineKeyboardButton("Описание курсов и лекций");
        coursesDescription.setCallbackData(Action.getCourseDescription + "_" + chatId);
        coursesDescription.setUrl("https://t.me/c/2388702610/1039");
        InlineKeyboardButton chooseCourse = new InlineKeyboardButton("Выбрать курс");
        chooseCourse.setCallbackData(Action.chooseArchiveOrActual + "_" + chatId);

        InlineKeyboardButton getInstruction = new InlineKeyboardButton("Инструкция");
        getInstruction.setCallbackData(Action.getInstruction + "_" + chatId);

        InlineKeyboardButton getPaymentInstruction = new InlineKeyboardButton("Способы оплаты");
        getPaymentInstruction.setCallbackData(Action.getPaymentInstruction + "_" + chatId);


        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(Arrays.asList(coursesDescription, chooseCourse));
        rows.add(Arrays.asList(getInstruction, getPaymentInstruction));
        keyboardMarkup.setKeyboard(rows);

        log.debug("Sent main menu to {}", chatId);
        sendInlineKeyboard(chatId, text, keyboardMarkup);
    }

    public void spreadToIds(Set<Long> chatIds, BroadcastMessage message) {
        for (Long id : chatIds) {
            try {
                if (message.hasPhoto()) {

                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(id.toString());
                    sendPhoto.setPhoto(new InputFile(message.getPhotoFileId()));
                    sendPhoto.setCaption(message.getText());

                    Main.paymentBot.execute(sendPhoto);

                } else {

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(id.toString());
                    sendMessage.setText(message.getText());

                    Main.paymentBot.execute(sendMessage);
                }

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static void execute(SendMessage message) {
        try {
            Main.paymentBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения {}", e.getMessage());
        }
    }

    /**
     * Keyboard with confirmation/decline buttons for payment check.
     */
    public static InlineKeyboardMarkup getValidationKeyboard(int messageId, long userId) {
        InlineKeyboardButton confirm = Utils.createConfirmButton(messageId, userId);
        InlineKeyboardButton decline = Utils.createDeclineButton(messageId, userId);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(Collections.singletonList(Arrays.asList(confirm, decline)));
        return keyboard;
    }

    public static InlineKeyboardMarkup getAllGroupKeyboard(Action callBack, Long userId) {
        return getTaggedGroupKeyboard(callBack, userId, null, COMMON_STILE);
    }

    public static InlineKeyboardMarkup getTaggedGroupKeyboard(Action callBack, Long userId, String tag, String style) {
        List<InlineKeyboardButton> buttons = getTagetButtonList(callBack, userId, tag);
        buttons.sort(Comparator.comparingInt(o -> Utils.firstPositiveNumber(o.getText())));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        if (buttons.size() <= 10 || style.equals(COMMON_STILE)) {
            keyboard.setKeyboard(distributeButtons(buttons));
        } else if (style.equals(ARROWED_STILE))
            keyboard.setKeyboard(arrowedStyleKeyboard(buttons, tag, 0, Action.none));
        else
            keyboard.setKeyboard(distributeButtons(buttons));
        return keyboard;
    }

    public static List<InlineKeyboardButton> getTagetButtonList(Action callBack, Long userId, String tag) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<Group> groupList = Main.dataUtils.getGroupList();
        for (Group group : groupList) {
            if (group.getTag().equals(tag) || tag == null) {
                String groupName = group.getName();
                Long groupId = group.getId();
                if (group.isBotAdmin()) {
                    buttons.add(Utils.createButton(groupName, callBack + "_" + groupId));
                } else if (Main.dataUtils.getAdminId() == userId) {
                    buttons.add(Utils.createButton("!" + groupName, callBack + "_" + groupId));
                }
            }
        }
        return buttons;
    }

    public static List<List<InlineKeyboardButton>> arrowedStyleKeyboard(List<InlineKeyboardButton> buttons, String tag, int index, Action action) {
        int MAX_BUTTONS_IN_PAGE = 10;
        int left = 0, right = Math.min(buttons.size() - 1, MAX_BUTTONS_IN_PAGE - 1);
        if (action == Action.rightArrow) {
            left = index + 1;
            right = Math.min(index + MAX_BUTTONS_IN_PAGE, buttons.size() - 1);
        }
        if (action == Action.leftArrow) {
            left = Math.max(index - MAX_BUTTONS_IN_PAGE, 0);
            right = index - 1;
        }

        List<InlineKeyboardButton> arrows = new ArrayList<>(2);
        if (left > 0) {
            InlineKeyboardButton leftArrow = new InlineKeyboardButton("⬅️ Назад");
            leftArrow.setCallbackData(Action.leftArrow + "_" + Main.dataUtils.getTagId(tag) + "_" + left); //try
            arrows.add(leftArrow);
        }
        if (right < buttons.size() - 1) {
            InlineKeyboardButton rightArrow = new InlineKeyboardButton("Далее ➡️");
            rightArrow.setCallbackData(Action.rightArrow + "_" + Main.dataUtils.getTagId(tag) + "_" + right);
            arrows.add(rightArrow);
        }

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(12);
        for (int i = left; i <= right; i++) {
            keyboard.add(Collections.singletonList(buttons.get(i)));
        }

        keyboard.add(arrows);
        return keyboard;
    }

    private static List<List<InlineKeyboardButton>> distributeButtons(List<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        int columnCount = Utils.getColumnCount(buttons.size());

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

    public static void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            Main.paymentBot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при удалении сообщения", e);
        }
    }

    public static boolean isBotAdminInGroup(Long groupId) {
        try {
            GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
            getChatAdministrators.setChatId(groupId);
            List<ChatMember> admins = Main.paymentBot.execute(getChatAdministrators);
            String botUsername = Main.paymentBot.getBotUsername();
            for (ChatMember admin : admins) {
                if (admin.getUser().getUserName().equals(botUsername)) {
                    return true;
                }
            }
        } catch (TelegramApiException e) {
            log.debug("Бот не входит в группу или не админ {}", groupId);
        }
        return false;
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
            String historyLink = getJoinRequestedLink(groupId);
            sendToHistoryChat(userName, groupName, historyLink, reason);

            String userInviteLink = createOneTimeInviteLink(groupId);
            sendInviteToUser(userId, groupId, groupName, userInviteLink);
        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу {}", e.getMessage());
        }
    }

    private static String getJoinRequestedLink(Long groupId) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setCreatesJoinRequest(true);
        return Main.paymentBot.execute(link).getInviteLink();
    }

    private static String createOneTimeInviteLink(Long groupId) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setName("Присоединиться к курсу");
        link.setExpireDate(0); // бессрочно
        link.setMemberLimit(1); // одноразовая
        return Main.paymentBot.execute(link).getInviteLink();
    }

    private static void sendToHistoryChat(String userName, String groupName, String link, String reason) throws TelegramApiException {
        String message = "Пользователю @" + userName + " отправлено приглашение в группу " +
                "<a href=\"" + link + "\">" + groupName + "</a>\nПричина: " + reason;

        SendMessage msg = new SendMessage();
        msg.setChatId(Main.dataUtils.getHistoryId());
        msg.setText(message);
        msg.setParseMode("HTML");
        msg.setDisableWebPagePreview(true);

        Main.paymentBot.execute(msg);
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
        button.setCallbackData(Action.getJoinRequestedLink + "_"
                + getJoinRequestedLink(groupId) + "_"
                + userId);

        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        replyMarkup.setKeyboard(List.of(List.of(button)));

        msg.setReplyMarkup(replyMarkup);

        Main.paymentBot.execute(msg);
    }

    /**
     * Обновляет текст и inline-клавиатуру одного сообщения.
     * library: org.telegram.telegrambots:telegrambots-spring-boot-starter   // если ещё не используете, придётся подключить
     */
    public static void updateMessageWithKeyboard(long chatId,
                                                 int messageId,
                                                 String text,
                                                 InlineKeyboardMarkup keyboard) throws TelegramApiException {

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        Main.paymentBot.execute(edit);          // метод execute() идёт из AbsSender / TelegramLongPollingBot
    }


}

