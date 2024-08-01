package bot.core;

import bot.core.util.ChatUtils;
import bot.core.util.ConfigUtils;
import bot.core.util.GroupUtils;
import bot.core.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    Validator validator;
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
        if (message.hasText() && message.getText().startsWith("/")) {
            handleCommand(message.getText(), message.getChatId());
            return;
        }

        if (isCreatingNewGroup(message)) {
            processNewGroupCreation(message);
            return;
        }

        if (isNewGroupMember(message)) {
            processNewGroupMember(message);
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
            forwardMessageToAdmin(message);
        }

        handleIncomingMessage(message);
    }

    private boolean isCreatingNewGroup(Message message) {
        return newGroup && message.getChatId() == ConfigUtils.getAdminChatID()
                && message.hasText() && !message.getText().equals("/cancel");
    }

    private void processNewGroupCreation(Message message) {
        String name = message.getText();
        if (GroupUtils.isValidGroupName(name)) {
            newGroupName = name;
            newGroup = false;
            ChatUtils.sendMessage(message.getChatId(), "Имя группы установленно на " + name
                    + "\nТеперь добавьте бота в требуемую группу и дайте ему права администратора" +
                    "\nПосле этого имя, которое вы ввели, будет присвоено группе, в которую вы добавили бота " +
                    "(имя группы в телеграмме никак не изменится, только для внутренней логики бота)");
        } else {
            ChatUtils.sendMessage(message.getChatId(), "Некорректное имя группы");
        }
    }

    private boolean isNewGroupMember(Message message) {
        return newGroupName != null && message.getNewChatMembers() != null;
    }

    private void processNewGroupMember(Message message) {
        for (User newMember : message.getNewChatMembers()) {
            try {
                if (newMember.getId().equals(this.getMe().getId())) {
                    log.info("Новая группа определена {}", newGroupName);
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
        sendMessage.setChatId(ConfigUtils.getAdminChatID());
        sendMessage.setText("Дайте боту права администратора в " + groupName
                + "\nПосле нажмите кнопку подтверждения");
        sendMessage.setReplyMarkup(keyboard);
        execute(sendMessage);
    }

    private boolean isEditingInfo(Message message) {
        return editInfo && message.getChatId() == ConfigUtils.getAdminChatID();
    }

    private void processInfoEditing(Message message) {
        if (message.hasText() && message.getText().equals("/cancel")) {
            editInfo = false;
            ChatUtils.sendMessage(message.getChatId(), "Редактирование info отменено");
        } else {
            ConfigUtils.setInfo(message.getText());
            editInfo = false;
            ChatUtils.sendMessage(message.getChatId(), "Информация изменена");
        }
    }

    private boolean isEditingHelp(Message message) {
        return editHelp && message.getChatId() == ConfigUtils.getAdminChatID();
    }

    private void processHelpEditing(Message message) {
        if (message.hasText() && message.getText().equals("/cancel")) {
            editHelp = false;
            ChatUtils.sendMessage(message.getChatId(), "Редактирование help отменено");
        } else {
            ConfigUtils.setHelp(message.getText());
            editHelp = false;
            ChatUtils.sendMessage(message.getChatId(), "Сообщение help изменено");
        }
    }

    private void forwardMessageToAdmin(Message message) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(-4286209564L);
        forwardMessage.setMessageId(message.getMessageId());
        forwardMessage.setFromChatId(message.getChatId());
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Не пересылаемое сообщение");
        }
    }

    private void handleIncomingMessage(Message message) {
        long chatId = message.getChatId();

        if (message.getChat().getType().equals("group") || message.getChat().getType().equals("supergroup")) {
            log.info("New message from group {}", chatId);
        } else {
            log.info("New message from user {}", chatId);
            long userId = message.getFrom().getId();
            if (message.hasDocument() || message.hasPhoto()) {
                handlePayment(message, chatId, userId);
            } else {
                ChatUtils.sendMessage(chatId, "Пожалуйста приложите документ или фото платежа");
            }
        }
    }

    private void handleCommand(String command, long userID) {
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
            default:
                handleUnknownCommand(userID);
                break;
        }
    }

    private void handleStartCommand(long userID) {
        ChatUtils.sendMessage(userID, "Привет! \uD83D\uDC4B\n" +
                "\n" +
                "Я бот, который помогает быстро и удобно обрабатывать подтверждения оплаты обучения в @Tulasikl. " +
                "Просто отправьте мне фото или документ, подтверждающий вашу оплату, и я добавлю вас в обучающую группу. \uD83C\uDF93\uD83D\uDCDA\n" +
                "\n" +
                "Давайте начнем!");
    }

    private void handleSetGroupCommand(long userID) {
        if (userID == ConfigUtils.getAdminChatID()) {
            InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(userID);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userID);
            sendMessage.setText("Выберите группу");
            sendMessage.setReplyMarkup(allGroupKeyboard);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа на команду /setGroup {}", e.getMessage());
            }
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleNewGroupCommand(long userID) {
        if (userID == ConfigUtils.getAdminChatID()) {
            ChatUtils.sendMessage(userID, "Введите название новой группы " +
                    "\nназвание не должно содержать пробелов или символов нижнего подчеркивания '_'!" +
                    "\nВместо пробелов используйте символ '-' (минус) \nНапример: 'group-name-12'");
            newGroup = true;
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleCancelCommand(long userID) {
        if (userID == ConfigUtils.getAdminChatID()) {
            newGroup = false;
            newGroupName = null;
            ChatUtils.sendMessage(userID, "Режим работы над командой отменен");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleInfoCommand(long userID) {
        ChatUtils.sendMessage(userID, ConfigUtils.getInfo());
    }

    private void handleHelpCommand(long userID) {
        ChatUtils.sendMessage(userID, ConfigUtils.getHelp());
    }

    private void handleEditInfoCommand(long userID) {
        if (userID == ConfigUtils.getAdminChatID()) {
            editInfo = true;
            ChatUtils.sendMessage(userID, "Введите новое описание группы");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleEditHelpCommand(long userID) {
        if (userID == ConfigUtils.getAdminChatID()) {
            editHelp = true;
            ChatUtils.sendMessage(userID, "Введите новое сообщение помощи");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleUnknownCommand(long userID) {
        ChatUtils.sendMessage(userID, "Неизвестная команда");
    }


    private void handlePayment(Message message, long chatId, long userId) {
        boolean valid = validator.isValidPayment(message);

        if (valid) {
            addInGroup(userId);
            ChatUtils.sendMessage(chatId, "Оплата подтверждена");
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
                handleConfirmAction(data, userID, messageId);
                break;
            case "decline":
                handleDeclineAction(data, userID, messageId);
                break;
            case "setGroup":
                handleSetGroupAction(data, userID, messageId);
                break;
            case "confirmAdmin":
                handleConfirmAdminAction(data, userID);
                break;
        }
    }

    private void handleConfirmAction(String[] data, long userID, int messageId) {
        addInGroup(Long.parseLong(data[2]));
        deleteMessage(userID, messageId);
        deleteMessage(userID, Integer.parseInt(data[1]));
    }

    private void handleDeclineAction(String[] data, long userID, int messageId) {
        decline(Long.parseLong(data[2]));
        deleteMessage(userID, messageId);
        deleteMessage(userID, Integer.parseInt(data[1]));
    }

    private void handleSetGroupAction(String[] data, long userID, int messageId) {
        Properties groupList = ConfigUtils.getGroupList();
        if (!groupList.containsKey(data[2])) {
            ChatUtils.sendMessage(userID, "Группа не найдена");
            return;
        }
        String groupID = groupList.getProperty(data[2]);
        if (GroupUtils.isBotAdminInGroup(groupID)) {
            ConfigUtils.updateConfig("groupID", groupID);
            deleteMessage(userID, messageId);
            ChatUtils.sendMessage(userID, "Группа для добавления изменена на " + data[2]);
        } else {
            ChatUtils.sendMessage(userID, "Бот не выходит в группу или не являеться в ней администратором");
        }
    }

    private void handleConfirmAdminAction(String[] data, long userID) {
        if (GroupUtils.isBotAdminInGroup(data[2])) {
            if (ConfigUtils.addNewGroup(data[1], Long.parseLong(data[2]))) {
                ChatUtils.sendMessage(userID, "Группа добавлена");
            } else {
                ChatUtils.sendMessage(userID, "Не удалось добавить группу");
                log.error("Не удалось добавить группу {}", data[1]);
            }
        } else {
            ChatUtils.sendMessage(ConfigUtils.getAdminChatID(), "Бот не являеться администартором в группе " + data[1]);
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
            log.info("Deleted message {}", messageId);
        } catch (TelegramApiException e) {
            log.error("Ошибка при удалении сообщения", e);
        }
    }

    private void decline(long userId) {
        try {
            log.info("Откланен запрос {} в группу {}", GroupUtils.getUserName(userId, ConfigUtils.getGroupID()),
                    GroupUtils.getGroupName(ConfigUtils.getGroupID()));
            ChatUtils.sendMessage(userId, "Ваша заявка была отклонена, \n" +
                    "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
        } catch (TelegramApiException e) {
            log.error("Error decline user request {} to group {}", userId, ConfigUtils.getGroupID());
        }
    }

    private void addInGroup(long userId) {
        CreateChatInviteLink inviteLink = GroupUtils.createInviteLink(ConfigUtils.getGroupID());

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
        // Команды для всех пользователей
        List<BotCommand> defaultCommands = new ArrayList<>();
        defaultCommands.add(new BotCommand("/info", "Информация о группе, куда сейчас идет набор"));
        defaultCommands.add(new BotCommand("/help", "Получить помощь"));

        // Команды для администраторов
        List<BotCommand> adminCommands = new ArrayList<>();
        adminCommands.add(new BotCommand("/set_group", "Установить группу, в которую бот будет добавлять после подтверждения"));
        adminCommands.add(new BotCommand("/new_group", "Добавить новую группу"));
        adminCommands.add(new BotCommand("/edit_info", "Изменить информацию о группе"));
        adminCommands.add(new BotCommand("/edit_help", "Изменить сообщение помощи"));
        adminCommands.add(new BotCommand("/cancel", "Отменить режим работы над командой"));

        try {
            // Установка команд для всех пользователей
            this.execute(new SetMyCommands(defaultCommands, new BotCommandScopeDefault(), null));
            // Установка команд для администраторов в конкретном чате (например, для группы или канала)
            this.execute(new SetMyCommands(adminCommands, new BotCommandScopeChat(Long.toString(ConfigUtils.getAdminChatID())), null));
        } catch (Exception e) {
            log.error("Error setting bot commands {}", e.getMessage());
        }
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotToken() {
        return ConfigUtils.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return ConfigUtils.getBotName();
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
