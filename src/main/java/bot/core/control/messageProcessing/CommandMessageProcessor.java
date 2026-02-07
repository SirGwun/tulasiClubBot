package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.Command;
import bot.core.model.*;
import bot.core.control.SessionController;
import bot.core.util.ChatUtils;
import bot.core.control.callbackHandlers.Action;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class CommandMessageProcessor implements MessageProcessor {

    @Override
    public boolean canProcess(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        return update.hasMessage() && message.isCommand();
    }

    @Override
    public void process(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .openSessionIfNeeded(update.getMessage().getFrom());
        CommandHandler handler = new CommandHandler(session.getState(), message.getFromId());
        handler.handle(message);
    }

    static class CommandHandler {
        private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
        private static final String CMD_LOG = "User {} use {}";
        private final SessionState state;
        private final long userId;

        public CommandHandler(SessionState state, long userId) {
            this.state = state;
            this.userId = userId;
        }

        public void handle(MessageContext message) {
            if (message.isCommand()) {
                String[] data = message.getText().split(" ", 2);
                String command = data[0];
                String args = data.length > 1 ? data[1] : "";
                handleCommand(command, args);
            } else {
                log.warn("handleCommand call with no command {}", message.getText());
            }
        }

        public void handleCommand(String commandStr, String args) {
            try {
                Command command = Command.valueOf(commandStr.substring(1));
                if (userId == Main.dataUtils.getAdminId()) {
                    handleAdminCommand(command, args);
                } else {
                    handleUserCommand(command);
                }
            } catch (IllegalArgumentException e) {
                handleUnknownCommand(commandStr);
            }
        }

        private void handleAdminCommand(Command command, String args) {
            switch (command) {
                case Command.start:
                    handleStartCommand();
                    break;
                case Command.menu:
                    handleMenuCommand();
                    break;
                case Command.set_tag:
                    handleSetTagCommand();
                    break;
                case Command.add_tag:
                    handleAddTagCommand(args);
                    break;
                case Command.choose_course:
                    handleSetCourseCommand();
                    break;
                case Command.help:
                    handleHelpCommand();
                    break;
                case Command.catalog:
                    handleCatalogCommand();
                    break;
                //***************
                case Command.cancel:
                    handleCancelCommand();
                    break;
                case Command.edit_info:
                    handleEditInfoCommand();
                    break;
                case Command.edit_help:
                    handleEditHelpCommand();
                    break;
                case Command.set_payment_info:
                    handleSetPaymentInfo();
                    break;
                case Command.del:
                    handleDelCommand();
                    break;
                case Command.say:
                    handleSayCommand(args);
                    break;
                case Command.set_timer:
                    handleSetTimerCommand(args);
                    break;
                case Command.spread:
                    handleSpreadCommand(args);
                    break;
                case Command.getuserlist:
                    handleGetUserList();
                    break;
                default:
                    log.warn("Неизвестная команда {}", command);
                    break;
            }
        }

        private void handleGetUserList() {
            List<Group> groupList = Main.dataUtils.getGroupList();

            for (Group group : groupList) {
                System.out.println(group);
            }

            System.out.println("_____________________");

            Map<Long, User> userMap = new HashMap<>();
            List<String> userNiknameList = new ArrayList<>();

            for (Group group : groupList) {
                GetChatMember chatMember = new GetChatMember();
                chatMember.setChatId(group.getId());
                GetChat getChat = new GetChat(group.getId() + "");
                try {
                    Chat chat = Main.paymentBot.execute(getChat);
                    userNiknameList = chat.getActiveUsernames();
                    for (String nik : userNiknameList) {
                        System.out.println(nik);
                    }
                } catch (TelegramApiException e) {
                    log.error("can't execute telegram getChat gor " + group);
                }
            }
        }

        private void handleSpreadCommand(String args) {

            Set<Long> userIdSet = SessionController.getSessionMap().keySet();

            BroadcastMessage message = new BroadcastMessage(
                    "test message",
                    "AgACAgIAAxkBAAIBQ2exampleFILEID" // file_id фото
            );

            spreadToChatIds(userIdSet, message);
        }


        private void spreadToChatIds(Set<Long> chatIds, BroadcastMessage message) {

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



        private void printUserListBySessions() {
            Map<Long, Session> sessionMap = SessionController.getSessionMap();
            List<String[]> tempList = new ArrayList<>();
            List<String> resultList = new ArrayList<>();

            for (Map.Entry<Long, Session> entry : sessionMap.entrySet()) {

                Long userId = entry.getKey();
                Session session = entry.getValue();

                String userName = session.getUserName();
                Long groupId = session.getGroupId();
                String groupName;

                if (groupId == null) {
                    groupName = "группа не известна";
                } else {
                    groupName = Main.dataUtils.getGroupName(groupId);
                    if (groupName == null) groupName = "группа не известна";
                }

                tempList.add(new String[]{
                        groupName,
                        userName == null ? "" : userName,
                        userId.toString()
                });
            }

            tempList.sort(Comparator
                    .comparing((String[] arr) -> arr[0], String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(arr -> arr[1], String.CASE_INSENSITIVE_ORDER)
            );

            System.out.println("========= USER LIST =========");

            String currentGroup = "";

            for (String[] arr : tempList) {

                String groupName = arr[0];
                String userName = arr[1];
                String userId = arr[2];

                if (!currentGroup.equals(groupName)) {
                    currentGroup = groupName;
                    System.out.println();
                    System.out.println("GROUP: " + groupName);
                }

                String line = "   " + userName + " (" + userId + ")";
                System.out.println(line);
                resultList.add(line);
            }

            System.out.println();
            System.out.println("TOTAL USERS: " + resultList.size());
        }

        private void handleUserCommand(Command command) {
            switch (command) {
                case Command.start:
                    handleStartCommand();
                    break;
                case Command.menu:
                    handleMenuCommand();
                    break;
                case Command.choose_course:
                    handleSetCourseCommand();
                    break;
                case Command.help:
                    handleHelpCommand();
                    break;
                case Command.catalog:
                    handleCatalogCommand();
                    break;
                default:
                    log.warn("Неизвестная команда {}", command);
                    break;
            }
        }

        private void handleStartCommand() {
            log.info(CMD_LOG, userId, Command.start);
            ChatUtils.sendMessage(userId,
                    """
                            Здравствуйте!
                                                        
                            Вас приветствует, бот-помощник курсов
                            Школы Аюрведы и здорового образа жизни "Tulasi"
                            """);
            ChatUtils.sendMainMenu(userId);
        }

        private void handleMenuCommand() {
            log.info(CMD_LOG, userId, Command.menu);
            ChatUtils.sendMainMenu(userId);
        }


        private void handleSetCourseCommand() {
            log.info(CMD_LOG, userId, Command.choose_course);
            InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllTagKeyboard(Action.chooseTag);

            if (allGroupKeyboard.getKeyboard().isEmpty()) {
                ChatUtils.sendMessage(userId, "Нет доступных групп");
                return;
            }

            if (hasExceptedGroup(allGroupKeyboard)) {
                ChatUtils.sendMessage(userId, "Бот не входит в группы помеченные '!', либо не админ в них");
            }
            ChatUtils.sendInlineKeyboard(userId, "Выберите курс", allGroupKeyboard);
        }

        private void handleSetTagCommand() {
            log.info(CMD_LOG, userId, Command.set_tag);
            InlineKeyboardMarkup allTagKeyboard = ChatUtils.getAllTagKeyboard(Action.setTag);

            ChatUtils.sendInlineKeyboard(userId,
                    "Установите тег который будет присваиваться всем добавленным далее группам/каналам",
                    allTagKeyboard);
        }

        private void handleAddTagCommand(String tagName) {
            log.info(CMD_LOG, userId, Command.add_tag);
            if (tagName.isEmpty()) {
                ChatUtils.sendMessage(userId, "Формат команды /addTag <new tag>");
                return;
            }
            ChatUtils.sendMessage(userId, "Новый курс добавлен " + tagName);
            Main.dataUtils.addNewTag(tagName);
        }

        private boolean hasExceptedGroup(InlineKeyboardMarkup allGroupKeyboard) {
            boolean hasGroupException = false;
            for (List<InlineKeyboardButton> row : allGroupKeyboard.getKeyboard()) {
                for (InlineKeyboardButton button : row) {
                    if (button.getText().startsWith("!")) {
                        hasGroupException = true;
                    }
                }
            }
            return hasGroupException;
        }

        private void handleCatalogCommand() {
            log.info(CMD_LOG, userId, Command.catalog);
            String catalog = Main.dataUtils.getCatalog();

            if (catalog == null) {
                ChatUtils.sendMessage(userId, "Каталог пока пуст");
                log.info("Каталог пуст");
                return;
            }

            List<String> messages = splitMessage(catalog);
            for (String message : messages) {
                ChatUtils.sendMessage(userId, message);
            }
        }

        private List<String> splitMessage(String text) {
            List<String> messages = new ArrayList<>();
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + 4096, text.length());

                if (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
                    int lastSpace = text.lastIndexOf(' ', end);
                    int lastNewLine = text.lastIndexOf('\n', end);
                    int breakPoint = Math.max(lastSpace, lastNewLine);
                    if (breakPoint > start) {
                        end = breakPoint;
                    }
                }

                messages.add(text.substring(start, end));
                start = end;

                while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
                    start++;
                }
            }
            return messages;
        }

        private void handleCancelCommand() {
            log.info(CMD_LOG, userId, Command.cancel);
            EditingActions action = state.cansel();
            ChatUtils.sendMessage(userId, "Режим работы над командой" + action.toString() + "отменен");
        }

        private void handleDelCommand() {
            log.info(CMD_LOG, userId, Command.del);

            if (Main.dataUtils.getGroupList().isEmpty()) {
                ChatUtils.sendMessage(userId, "Не найдено ни одной группы");
                return;
            }

            InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(Action.delGroup, userId);
            ChatUtils.sendInlineKeyboard(userId, "Выберете группу для удаления", allGroupKeyboard);
        }

        private void handleHelpCommand() {
            ChatUtils.sendMessage(userId, Main.dataUtils.getHelp());
        }

        private void handleEditInfoCommand() {
            log.info(CMD_LOG, userId, Command.edit_info);
            state.editInfo();
            ChatUtils.sendMessage(userId, "Введите новое описание группы");
        }

        private void handleEditHelpCommand() {
            log.info(CMD_LOG, userId, Command.edit_help);
            state.editHelp();
            ChatUtils.sendMessage(userId, "Введите новое сообщение помощи");
        }

        private void handleSetPaymentInfo() {
            log.info(CMD_LOG, userId, Command.set_payment_info);
            state.editPaymentInfo();
            ChatUtils.sendMessage(userId, "Пришлите сообщение содержащее информацию о методах оплаты");
        }

        private void handleSayCommand(String args) {
            log.info(CMD_LOG, userId, Command.say);
            if (args == null || args.isBlank()) {
                ChatUtils.sendMessage(userId, "Формат: /say @username текст");
                return;
            }

            String[] parts = args.trim().split(" ", 2);
            if (parts.length < 2) {
                ChatUtils.sendMessage(userId, "Формат: /say @username текст");
                return;
            }

            String username = parts[0].startsWith("@") ? parts[0].substring(1) : parts[0];
            String text = parts[1];
            log.info("Admin {} use say command to @{}", userId, username);

            Long targetId = SessionController.getInstance().getUserIdByUsername(username);
            if (targetId == null) {
                try {
                    GetChat getChat = new GetChat("@" + username);
                    Chat chat = Main.paymentBot.execute(getChat);
                    targetId = chat.getId();
                } catch (TelegramApiException e) {
                    log.warn("Unable to find user via Telegram API: {}", e.getMessage());
                }
            }

            if (targetId == null) {
                ChatUtils.sendMessage(userId, "Пользователь @" + username + " не найден");
                return;
            }

            ChatUtils.sendMessage(targetId, text);
            ChatUtils.sendMessage(userId, "Сообщение отправлено пользователю @" + username);
        }

        private void handleSetTimerCommand(String args) {
            int MAX_TIMER_MINUTES = 525600;
            int TIMER_DISABLED = -1;
            log.info(CMD_LOG, userId, (Command.set_timer + " args: " + args));
            try {
                int timeMin = Integer.parseInt(args);

                if ((timeMin > 0 && timeMin <= MAX_TIMER_MINUTES) || timeMin == TIMER_DISABLED) {
                    Main.dataUtils.setTimerMinutes(timeMin);
                    String message = timeMin == TIMER_DISABLED
                            ? "Новые таймеры теперь не будут добавляться"
                            : "Установлено, теперь новые таймеры будут срабатывать через " + timeMin + " минут после добавления";
                    ChatUtils.sendMessage(userId, message);
                } else {
                    ChatUtils.sendMessage(userId,
                            "Время должно быть положительным числом от 1 до " + MAX_TIMER_MINUTES +
                                    " или " + TIMER_DISABLED + " для отключения таймера");
                }
            } catch (NumberFormatException e) {
                if (args.isEmpty())
                    log.error("No args on set_timer");
                else
                    log.error("Failed to parse timer value: {}", args);
                ChatUtils.sendMessage(userId, "Формат - /set_timer <время в минутах>");
            }
        }

        private void handleUnknownCommand(String message) {
            log.info("User {} send unknown command {}", userId, message);
            ChatUtils.sendMessage(userId, "Неизвестная команда");
        }
    }
}