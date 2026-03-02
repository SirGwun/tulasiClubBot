package bot.core.util;

import bot.core.Main;
import bot.core.control.SessionService;
import bot.core.control.callbackHandlers.Action;
import bot.core.model.BaseGroup;
import bot.core.model.MessageContext;
import bot.core.model.User;
import bot.core.repos.GroupRepository;
import bot.core.repos.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.MessageId;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import bot.core.model.Group;

import java.util.*;

/**
 * Utility methods for interacting with chats.
 */
public final class ChatUtils {
    private static final Logger log = LoggerFactory.getLogger(ChatUtils.class);
    public static final String ARROWED_STILE = "arrowed";
    public static final String COMMON_STILE = "common";

    private static GroupRepository groupRepository = new GroupRepository();
    private static UserRepository userRepository = new UserRepository();

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
                Выберете нужную кнопку ↓
                """;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

//        InlineKeyboardButton coursesDescription = new InlineKeyboardButton("Каталог");
//        coursesDescription.setCallbackData(Action.getCourseDescription + "_" + chatId);
//        coursesDescription.setUrl("https://t.me/c/2388702610/1039");
        InlineKeyboardButton chooseCourse = new InlineKeyboardButton("Выбрать курс");
        chooseCourse.setCallbackData(Action.getCourseList + "_" + chatId);

        InlineKeyboardButton getInstruction = new InlineKeyboardButton("Инструкция");
        getInstruction.setCallbackData(Action.getInstruction + "_" + chatId);

        InlineKeyboardButton getPaymentInstruction = new InlineKeyboardButton("Способы оплаты");
        getPaymentInstruction.setCallbackData(Action.getPaymentInstruction + "_" + chatId);


        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(chooseCourse));
        rows.add(Arrays.asList(getInstruction, getPaymentInstruction));
        keyboardMarkup.setKeyboard(rows);

        log.debug("Sent main menu to {}", chatId);
        sendInlineKeyboard(chatId, text, keyboardMarkup);
    }

    public static void spreadToIds(List<Long> chatIds, MessageContext context, boolean withButton) {
        long fromChatId = context.getChatId();
        int messageId = context.message().getMessageId();
        chatIds.remove(Main.dataUtils.getAdminId());

        for (Long targetChatId : chatIds) {

            CopyMessage copiedMessage = new CopyMessage();
            copiedMessage.setFromChatId(fromChatId);
            copiedMessage.setChatId(targetChatId);
            copiedMessage.setMessageId(messageId);

            try {
                MessageId result = Main.paymentBot.execute(copiedMessage);

                if (withButton) {
                    addInlineKeyboard(targetChatId, result.getMessageId());
                }
            } catch (TelegramApiException e) {
                log.error("Cant execute copy message to {}", targetChatId);
            }
        }
    }

    private static void addInlineKeyboard(Long chatId, Long messageId) throws TelegramApiException {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        String buttonText = "Перейти к курсу - " + Main.dataUtils.getActualGroupTag();
        long actualTag = Main.dataUtils.getTagId(Main.dataUtils.getActualGroupTag());

        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(Action.chooseTag + "_" + actualTag);

        row.add(button);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId((int) messageId.longValue());
        editMarkup.setReplyMarkup(keyboardMarkup);

        Main.paymentBot.execute(editMarkup);
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

        InlineKeyboardButton bayAllCourse = new InlineKeyboardButton("\uD83D\uDC8E Курс целиком");
        bayAllCourse.setCallbackData(Action.buyWholeCourse + "_" + tag);
        buttons.addFirst(bayAllCourse);

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
        Map<Long, String> tags = Main.dataUtils.getTagMap();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        String actualTag = Main.dataUtils.getActualGroupTag();
        Long actualTagId = null;

        for (Map.Entry<Long, String> entry : tags.entrySet()) {
            if (entry.getValue().equals(actualTag)) {
                actualTagId = entry.getKey();
                break;
            }
        }

        if (actualTagId != null) {
            InlineKeyboardButton actualButton = new InlineKeyboardButton();
            actualButton.setText("\uD83C\uDF93 " + actualTag + " (Текущий)");
            actualButton.setCallbackData(callback.name() + "_" + actualTagId);
            buttons.add(Collections.singletonList(actualButton));
        }

        for (Map.Entry<Long, String> entry : tags.entrySet()) {
            if (!entry.getValue().equals(actualTag)) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("\uD83D\uDCE6 " + entry.getValue() + " (Запись)");
                button.setCallbackData(callback.name() + "_" + entry.getKey());
                buttons.add(Collections.singletonList(button));
            }
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
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(groupId);
            getChatMember.setUserId(Main.paymentBot.getMe().getId());

            ChatMember chatMember = Main.paymentBot.execute(getChatMember);
            String status = chatMember.getStatus();

            return "administrator".equals(status) || "creator".equals(status);
        } catch (TelegramApiException e) {
            log.debug("Бот не в группе или ошибка доступа {}", groupId);
            return false;
        }
    }

    public static void addInGroup(long userId, Long groupId, String reason) {

        Optional<BaseGroup> groupOptional = groupRepository.findAnyGroupById(groupId);

        if (groupOptional.isEmpty()) {
            log.error("Попытка добавить в неизвестную группу {}", groupId);
            return;
        }

        BaseGroup group = groupOptional.get();
        String groupName = group.getName();

        String userName = userRepository.findByChatId(userId)
                .orElse(new User(userId))
                .getName();

        String userInviteLink = createOneTimeInviteLink(groupId);
        sendInviteToUser(userId, groupId, groupName, userInviteLink);

        String historyLink = getJoinRequestedLink(groupId);
        sendToHistoryChat(userName, groupName, historyLink, reason);
    }

    private static String getJoinRequestedLink(Long groupId)  {
        try {
            CreateChatInviteLink link = new CreateChatInviteLink();
            link.setChatId(groupId);
            link.setCreatesJoinRequest(true);
            return Main.paymentBot.execute(link).getInviteLink();
        } catch (TelegramApiException e) {
            log.error("Ошибка при создании JoinRequestedLink на {}", groupId);
            return null;
        }
    }

    private static String createOneTimeInviteLink(Long groupId) {
        try {
            CreateChatInviteLink link = new CreateChatInviteLink();
            link.setChatId(groupId);
            link.setName("Присоединиться к курсу");
            link.setExpireDate(0); // бессрочно
            link.setMemberLimit(1); // одноразовая
            return Main.paymentBot.execute(link).getInviteLink();
        } catch (TelegramApiException e) {
            log.error("Ошибка при создании OneTimeInviteLink на {}", groupId);
            return null;
        }
    }

    private static void sendToHistoryChat(String userName, String groupName, String link, String reason) {
        String message = "Пользователю @" + userName + " отправлено приглашение в группу " +
                "<a href=\"" + link + "\">" + groupName + "</a>\nПричина: " + reason;
        try {
            SendMessage msg = new SendMessage();
            msg.setChatId(Main.dataUtils.getHistoryId());
            msg.setText(message);
            msg.setParseMode("HTML");
            msg.setDisableWebPagePreview(true);

            Main.paymentBot.execute(msg);
        } catch (TelegramApiException e) {
            log.error("Ошибка при пересылке сообщения в чат истории {} {}", Main.dataUtils.getHistoryId(), userName);
        }
    }

    private static void sendInviteToUser(long userId, long groupId, String groupName, String link) {
        String messageText =
                "━━━━━━━━━━━━━━━━━━\n" +
                        "✅ ДОСТУП К ГРУППЕ\n" +
                        "━━━━━━━━━━━━━━━━━━\n\n" +
                        "Чтобы присоединиться к группе, нажмите на ссылку ниже ↓:\n\n" +
                        "<a href=\"" + link + "\">🔗 " + groupName + "</a>\n\n" +
                        "❗️ Важно:\n" +
                        "После перехода по ссылке группа автоматически появится\n" +
                        "в ваших чатах.\n\n" +
                        "Если вы не можете найти лекцию — воспользуйтесь кнопкой ниже ↓.";

        try {
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
        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу {}", e.getMessage());
        }
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

