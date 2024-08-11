package bot.core;

import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.util.GroupUtils;
import bot.core.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.*;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    Validator validator;
    Map<Long, String> groupMap = new HashMap<>();
    public static String newGroupName = null;
    private static boolean newGroup = false;
    private static boolean editInfo = false;
    private static boolean editHelp = false;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleIncomingUpdate(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleIncomingUpdate(Message message) {
        if (!message.getChat().getType().equals("group") && !message.getChat().getType().equals("supergroup")) {
            if (message.hasText() && message.getText().startsWith("/")) {
                handleCommand(message.getText(), message.getChatId());
                return;
            }

            if (isCreatingNewGroup(message)) {
                processNewGroupCreation(message);
                return;
            }

            if (isEditingInfo(message)) {
                processInfoEditing(message);
                return;
            }

            if (isEditingHelp(message)) {
                processHelpEditing(message);
                return;
            }

            if (!Main.isTest) {
                forwardMessageToHistory(message);
            }
        } else {
            if (isNewGroupMember(message)) {
                processNewGroupMember(message);
                return;
            }
        }
        handleIncomingMessage(message);
    }

    private void handleIncomingMessage(Message message) {
        long chatId = message.getChatId();
        log.info("New message from {}", message.getChatId());
        if (!message.getChat().getType().equals("group") && !message.getChat().getType().equals("supergroup")) {
            long userId = message.getFrom().getId();
            if (message.hasDocument() || message.hasPhoto()) {
                handlePayment(message, chatId, userId);
            } else {
                ChatUtils.sendMessage(chatId, "Пожалуйста приложите документ или фото платежа");
            }
        }
    }

    private void handleCommand(String command, long userID) {
        log.info("New command {}", command);
        String[] data = command.split(" ");
        switch (data[0]) {
            case "/start":
                handleStartCommand(userID);
                break;
            case "/set_group":
                handleSetGroupCommand(userID);
                break;
            case "/new_group":
                handleNewGroupCommand(userID);
                break;
            case "/cancel":
                handleCancelCommand(userID);
                break;
            case "/info":
                handleInfoCommand(userID);
                break;
            case "/help":
                handleHelpCommand(userID);
                break;
            case "/edit_info":
                handleEditInfoCommand(userID);
                break;
            case "/edit_help":
                handleEditHelpCommand(userID);
                break;
            case "/del":
                handleDelCommand(userID);
                break;
            case "/catalog":
                handleCatalogCommand(userID);
                break;
            default:
                handleUnknownCommand(userID, command);
                break;
        }
    }

    private void handleCatalogCommand(long userID) {
        log.info("user {} get /catalog command", userID);
        String catalog = DataUtils.getCatalog();
        if (catalog != null) {
            // Разбиваем каталог на части, каждая из которых не превышает 4096 символов
            List<String> messages = splitMessage(catalog, 4096);
            for (String message : messages) {
                ChatUtils.sendMessage(userID, message);
            }
        } else {
            ChatUtils.sendMessage(userID, "Каталог пока пуст");
            log.info("Ошибка при чтении каталога");
        }
    }

    // Метод для разбиения строки на части заданной длины, не разрывая слова
    private List<String> splitMessage(String text, int maxLength) {
        List<String> messages = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());
            // Проверяем, что не разрываем слово
            if (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
                // Ищем последний пробел или перенос строки перед end
                int lastSpace = text.lastIndexOf(' ', end);
                int lastNewLine = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastSpace, lastNewLine);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }
            messages.add(text.substring(start, end));
            start = end;

            // Пропускаем пробелы в начале следующего сегмента
            while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
                start++;
            }
        }
        return messages;
    }

    private void handleDelCommand(long userID) {
        log.info("user {} get /del command", userID);
        if (userID == DataUtils.getAdminID()) {
            if (DataUtils.getGroupList().isEmpty()) {
                ChatUtils.sendMessage(userID, "Нет доступных групп");
                return;
            }
            InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(userID, "delGroup");
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userID);
            sendMessage.setText("Выберете группу для удаления");
            sendMessage.setReplyMarkup(allGroupKeyboard);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа на команду /del {}", e.getMessage());
            }
        } else {
            ChatUtils.sendMessage(userID, "У вас нет прав на выполнение этой команды");
        }
    }

    private void handlePayment(Message message, long chatId, long userId) {
        log.info("New payment from {}", userId);
        boolean valid = validator.isValidPayment(message);

        if (valid) {
            addInGroup(userId);
            forwardMessageToHistory(message, "Добавлен в группу автопроверкой");
            log.info("Автоматическая проверка подтвердила оплату");
        } else {
            validator.sendOuHumanValidation(message);
            ChatUtils.sendMessage(chatId, "Ваше подтверждение отправлено на проверку. Пожалуйста, подождите.\n \n" +
                    "Как только проверка завершится, бот пришлет вам ссылку для вступления в группу.");
        }
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String[] data = callbackQuery.getData().split("_");
        String action = data[0];
        long userID = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        switch (action) {
            case "confirm":
                handleConfirmAction(callbackQuery, data, userID, messageId);
                break;
            case "decline":
                handleDeclineAction(callbackQuery, data, userID, messageId);
                break;
            case "setGroup":
                handleSetGroupAction(callbackQuery, data, userID, messageId);
                break;
            case "confirmAdmin":
                handleConfirmAdminAction(callbackQuery, data, userID);
                break;
            case "delGroup":
                handleDelGroupAction(callbackQuery, data, userID);
        }
    }

    private void handleDelGroupAction(CallbackQuery callbackQuery, String[] data, long userID) {
        String groupId = data[1];
        if (DataUtils.getGroupList().containsValue(groupId)) {
            DataUtils.removeGroup(groupId);
            ChatUtils.sendMessage(userID, "Группа удалена");
            ChatUtils.deleteMessage(userID, callbackQuery.getMessage().getMessageId());
        } else {
            ChatUtils.sendMessage(userID, "Группа не найдена");
        }
    }


    private boolean isCreatingNewGroup(Message message) {
        return newGroup && message.getChatId() == DataUtils.getAdminID()
                && message.hasText() && !message.getText().equals("/cancel");
    }

    private void processNewGroupCreation(Message message) {
        log.info("New group started");
        String name = message.getText();
        if (name.length() > 128) {
            ChatUtils.sendMessage(message.getChatId(), "Слишком длинное имя группы, пожалуйста, используйте не более 128 символов");
            return;
        }
        if (GroupUtils.isValidGroupName(name)) {
            newGroupName = name.replace(" ", "-");
            newGroupName = newGroupName.replace("_", "-");
            newGroup = false;
            ChatUtils.sendMessage(message.getChatId(), "Имя группы установлено на \"" + name + "\".\n\n" +
                    "Теперь добавьте бота в нужную группу и предоставьте ему права администратора.\n\n" +
                    "После этого введённое вами имя будет присвоено группе, в которую добавлен бот. \n\n" +
                    "Обратите внимание, что имя группы в Telegram останется прежним, изменения касаются только внутренней логики бота.");
        } else {
            ChatUtils.sendMessage(message.getChatId(), "Некорректное имя группы");
        }
    }

    private boolean isNewGroupMember(Message message) {
        return newGroupName != null && message.getNewChatMembers() != null;
    }

    private void processNewGroupMember(Message message) {
        log.info("Bot added to new group");
        for (User newMember : message.getNewChatMembers()) {
            try {
                if (newMember.getId().equals(this.getMe().getId())) {
                    log.info("Новая группа определена {}", newGroupName.replace("-", " "));
                    if (DataUtils.getGroupList().containsValue(message.getChatId().toString())) {
                        Set<Map.Entry<Object, Object>> entries = DataUtils.getGroupList().entrySet();
                        String groupName = "";
                        for (Map.Entry<Object, Object> entry : entries) {
                            if (entry.getValue().equals(message.getChatId().toString())) {
                                groupName = entry.getKey().toString();
                                break;
                            }
                        }
                        ChatUtils.sendMessage(DataUtils.getAdminID(), "Группа уже есть в списке. Имя группы "
                                + groupName + "\nПожалуйста, просто используйте уже добавленную группу с помощю команды /set_group");
                        newGroupName = null;
                        newGroup = false;
                        return;
                    }
                    InlineKeyboardMarkup keyboard = ChatUtils.getKonfirmAdminStatusKeyboard(new Group(newGroupName, message.getChatId()));
                    sendAdminConfirmationMessage(newGroupName, keyboard);
                    return;
                }
            } catch (TelegramApiException e) {
                log.error("Error add new group {}", newGroupName, e);
            }
        }
    }

    private void sendAdminConfirmationMessage(String groupName, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(DataUtils.getAdminID());
        sendMessage.setText("Дайте боту права администратора в \"" + groupName.replace("-", " ")
                + "\"\n\nПосле нажмите кнопку подтверждения");
        sendMessage.setReplyMarkup(keyboard);
        execute(sendMessage);
    }

    private boolean isEditingInfo(Message message) {
        return editInfo && message.getChatId() == DataUtils.getAdminID();
    }

    private void processInfoEditing(Message message) {
        log.info("Editing info");
        if (message.hasText() && message.getText().equals("/cancel")) {
            editInfo = false;
            ChatUtils.sendMessage(message.getChatId(), "Редактирование info отменено");
        } else {
            DataUtils.setInfo(message.getText());
            editInfo = false;
            ChatUtils.sendMessage(message.getChatId(), "Информация изменена");
        }
    }

    private boolean isEditingHelp(Message message) {
        return editHelp && message.getChatId() == DataUtils.getAdminID();
    }

    private void processHelpEditing(Message message) {
        log.info("Editing help");
        if (message.hasText() && message.getText().equals("/cancel")) {
            editHelp = false;
            ChatUtils.sendMessage(message.getChatId(), "Редактирование help отменено");
        } else {
            DataUtils.setHelp(message.getText());
            editHelp = false;
            ChatUtils.sendMessage(message.getChatId(), "Сообщение help изменено");
        }
    }

    private void forwardMessageToHistory(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(DataUtils.getHistroyID());
        sendMessage.setText(text);
        forwardMessageToHistory(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в историю {}", e.getMessage());
        }
    }

    private void forwardMessageToHistory(Message message) {
        log.info("Forwarding message to history");
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(DataUtils.getHistroyID());
        forwardMessage.setMessageId(message.getMessageId());
        forwardMessage.setFromChatId(message.getChatId());
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Не пересылаемое сообщение");
        }
    }

    private void handleStartCommand(long userID) {
        log.info("User {} started bot", userID);
        ChatUtils.sendMessage(userID, "Привет! \uD83D\uDC4B\n" +
                "\n" +
                "Я бот, который помогает быстро и удобно обрабатывать подтверждения оплаты обучения в @Tulasikl. " +
                "Просто отправьте мне фото или документ, подтверждающий вашу оплату, и я добавлю вас в обучающую группу. \uD83C\uDF93\uD83D\uDCDA\n" +
                "\n" +
                "По умолчанию я добавляю в группу \"" + DataUtils.getGroupName(DataUtils.getMainGroupID()) + "\" , но если вы хотите выбрать другую, " +
                "воспользуйтесь меню слева от поля ввода и командой /set_group.\n" +
                "\n" +
                "Давайте начнем!");

    }

    private void handleSetGroupCommand(long userID) {
        log.info("User {} set group", userID);

        if (DataUtils.getGroupList().isEmpty()) {
            ChatUtils.sendMessage(userID, "Нет доступных групп");
            return;
        }
        InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(userID, "setGroup");
        boolean hasGroupException = false;
        for (List<InlineKeyboardButton> row : allGroupKeyboard.getKeyboard()) {
            for (InlineKeyboardButton button : row) {
                if (button.getText().startsWith("!")) {
                    hasGroupException = true;
                }
            }
        }
        if (hasGroupException) {
            ChatUtils.sendMessage(userID, "Группы помеченые \"!\" либо не существуют, либо бот не являеться в них админом\n\nРекомендую их удалить");
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userID);
        sendMessage.setText("Выберите группу");
        sendMessage.setReplyMarkup(allGroupKeyboard);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на команду /setGroup {}", e.getMessage());
        }
    }

    private void handleNewGroupCommand(long userID) {
        log.info("User {} create new group", userID);
        if (userID == DataUtils.getAdminID()) {
            ChatUtils.sendMessage(userID, "Введите название новой группы ");
            newGroup = true;
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleCancelCommand(long userID) {
        log.info("User {} cancel command", userID);
        if (userID == DataUtils.getAdminID()) {
            newGroup = false;
            newGroupName = null;
            ChatUtils.sendMessage(userID, "Режим работы над командой отменен");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleInfoCommand(long userID) {
        ChatUtils.sendMessage(userID, DataUtils.getInfo());
    }

    private void handleHelpCommand(long userID) {
        ChatUtils.sendMessage(userID, DataUtils.getHelp());
    }

    private void handleEditInfoCommand(long userID) {
        log.info("User {} edit info", userID);
        if (userID == DataUtils.getAdminID()) {
            editInfo = true;
            ChatUtils.sendMessage(userID, "Введите новое описание группы");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleEditHelpCommand(long userID) {
        log.info("User {} edit help", userID);
        if (userID == DataUtils.getAdminID()) {
            editHelp = true;
            ChatUtils.sendMessage(userID, "Введите новое сообщение помощи");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleUnknownCommand(long userID, String message) {
        log.info("User {} send unknown command {}", userID, message);
        ChatUtils.sendMessage(userID, "Неизвестная команда");
    }


    private void handleConfirmAction(CallbackQuery callbackQuery, String[] data, long userID, int messageId) {
        log.info("User {} confirm {}", userID, data[2]);
        addInGroup(Long.parseLong(data[2]));
        ChatUtils.deleteMessage(userID, messageId);
        ChatUtils.deleteMessage(userID, Integer.parseInt(data[1]));

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void handleDeclineAction(CallbackQuery callbackQuery, String[] data, long userID, int messageId) {
        log.info("User {} decline {}", userID, data[2]);
        decline(Long.parseLong(data[2]));
        ChatUtils.deleteMessage(userID, messageId);
        ChatUtils.deleteMessage(userID, Integer.parseInt(data[1]));

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void handleSetGroupAction(CallbackQuery callbackQuery, String[] data, long userID, int messageId) {
        log.info("User {} set group {}", userID, data[1]);
        Properties groupList = DataUtils.getGroupList();
        if (!groupList.containsValue(data[1])) {
            ChatUtils.sendMessage(userID, "Группа не найдена");
            return;
        }
        String groupID = data[1];
        String groupName = "";
        Set<Map.Entry<Object, Object>> entries = groupList.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            if (entry.getValue().equals(data[1])) {
                groupName = entry.getKey().toString();
                break;
            }
        }

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

        if (GroupUtils.isBotAdminInGroup(groupID)) {
            if (userID == DataUtils.getAdminID()) {
                DataUtils.updateConfig("groupID", groupID);
                ChatUtils.deleteMessage(userID, messageId);
                ChatUtils.sendMessage(userID, "Группа выбрана " + groupName.replaceAll("-", " "));
            } else {
                groupMap.put(userID, groupID);
                ChatUtils.sendMessage(userID, "Выбрана группа: " + groupName.replaceAll("-", " ") + "\nТеперь пришлите подтверждение оплаты");
            }
        } else {
            ChatUtils.sendMessage(userID, "Бот не выходит в группу или не являеться в ней администратором");
        }

        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void handleConfirmAdminAction(CallbackQuery callbackQuery, String[] data, long userID) {
        log.info("User {} confirm admin {}", userID, data[1]);

        // Обработка запроса
        String groupId = data[1];
        if (GroupUtils.isBotAdminInGroup(groupId)) {
            if (newGroupName == null) {
                ChatUtils.sendMessage(userID, "Имя группы пусто");
                log.error("Имя группы пусто");
            } else if (DataUtils.addNewGroup(newGroupName, Long.parseLong(groupId))) {
                ChatUtils.sendMessage(userID, "Группа добавлена");
                newGroupName = null;
                newGroup = false;
            } else {
                ChatUtils.sendMessage(userID, "Не удалось добавить группу");
                log.error("Не удалось добавить группу {}", groupId);
            }
        } else {
            ChatUtils.sendMessage(DataUtils.getAdminID(), "Бот не являеться администратором в группе " + newGroupName);
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void decline(long userId) {
        try {
            log.info("Откланен запрос {} в группу {}", GroupUtils.getUserName(userId, DataUtils.getMainGroupID()),
                    GroupUtils.getGroupName(DataUtils.getMainGroupID()));
            ChatUtils.sendMessage(userId, "Ваша заявка была отклонена, \n" +
                    "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
        } catch (TelegramApiException e) {
            log.error("Error decline user request {} to group {}", userId, DataUtils.getMainGroupID());
        }
    }

    private void addInGroup(long userId) {
        CreateChatInviteLink inviteLink;
        if (groupMap.containsKey(userId)) {
            inviteLink = GroupUtils.createInviteLink(Long.parseLong(groupMap.get(userId)));
            groupMap.remove(userId);
        } else {
            inviteLink = GroupUtils.createInviteLink(DataUtils.getMainGroupID());
        }

        try {
            SendMessage message = new SendMessage();
            message.setChatId(userId);
            message.setText("Здравствуйте!\n\nОплата подтверждена. Для присоединения к группе перейдите по ссылке ниже:\n\n" +
                    "<a href=\"" + execute(inviteLink).getInviteLink() + "\">Присоединиться к курсу</a>\n\n" +
                    "Мы рады вас видеть!");
            message.setParseMode(ParseMode.HTML);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу \n {}", e.getMessage());
        }
    }

    private void setBotCommands() {
        //todo разобраться, как убрать / в чатах групп

        // Команды для всех пользователей
        List<BotCommand> defaultCommands = new ArrayList<>();
        defaultCommands.add(new BotCommand("/set_group", "Выбрать группу"));
        defaultCommands.add(new BotCommand("/catalog", "Каталог всех лекций"));
        defaultCommands.add(new BotCommand("/info", "Информация о группе"));
        defaultCommands.add(new BotCommand("/help", "Помощь"));

        // Команды для администраторов
        List<BotCommand> adminCommands = new ArrayList<>();
        adminCommands.add(new BotCommand("/new_group", "Добавить группу"));
        adminCommands.add(new BotCommand("/set_group", "Установить текущую группу"));
        adminCommands.add(new BotCommand("/del", "Удалить группу"));
        adminCommands.add(new BotCommand("/edit_info", "Изменить информацию"));
        adminCommands.add(new BotCommand("/edit_help", "Изменить помощь"));
        adminCommands.add(new BotCommand("/cancel", "Отменить действие"));
        try {
            execute(new SetMyCommands(defaultCommands, new BotCommandScopeAllPrivateChats(), null));
            long adminChatId = DataUtils.getAdminID();
            execute(new SetMyCommands(adminCommands, new BotCommandScopeChat(Long.toString(DataUtils.getAdminID())), null));
        } catch (Exception e) {
            log.error("Error setting bot commands {}", e.getMessage());
        }
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotToken() {
        return DataUtils.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return DataUtils.getBotName();
    }

    public static String getNewGroupName() {
        return newGroupName;
    }


    @Override
    public void onRegister() {
        super.onRegister();
        setBotCommands();
        validator = new Validator();
    }
}
