package bot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    Validator validator;

    private static String newGroupName = null;
    private static boolean newGroup = false;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (newGroup && message.hasText()) {
                String name = message.getText();
                if (GroupUtils.isValidGroupName(name)) {
                    newGroupName = name;
                    newGroup = false;
                    ChatUtils.sendMessage(message.getChatId(), "Имя группы установленно на " + name
                            + "\nТеперь добавьте бота в требудемую группу и дайте ему права администартора" +
                            "\nПосле этого имя, которое вы ввели, будет присвоено группе, в кторую вы добавили бота (только для самого бота)");
                } else {
                    ChatUtils.sendMessage(message.getChatId(), "Некорректное имя группы");
                }
            }

            if (newGroupName != null && message.getNewChatMembers() != null) {
                for (User newMember : message.getNewChatMembers()) {
                    if (newMember.getUserName().equals(getBotUsername())) {
                        if (ConfigUtils.addNewGroup(newGroupName, message.getChatId())) {
                            log.info("Новая группа добавлена {}", newGroupName);
                            ChatUtils.sendMessage(ConfigUtils.getAdminChatID(), "Новая группа добавлена " + newGroupName
                                    + "\nПожалуйста, не забудьте дать боту права администратора!" +
                                    "\nВ противном случае он не сможет работать");
                            newGroupName = null;
                        } else {
                            log.error("Не удалось добавить группу {}", newGroupName);
                        }
                    }
                }
            }

            if (message.hasText() && message.getText().startsWith("/")) {
                handleCommand(message.getText(), message.getChatId());
            } else {
                handleIncomingMessage(message);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
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
            case "/set_group":
                if (userID == ConfigUtils.getAdminChatID()) {
                    InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(userID);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(userID);
                    sendMessage.setText("Выберите группу");
                    sendMessage.setReplyMarkup(allGroupKeyboard);
                    try{
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error("Ошибка при отправке ответа на команду /setGroup {}", e.getMessage());
                    }
                } else {
                    ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
                }
                break;
            case "/new_group":
                ChatUtils.sendMessage(userID, "Введите название новой группы " +
                        "\nназвание не должно содержать пробелов или символов нижнего подчеркивания '_'!" +
                        "\nВместо пробелов используйте символ '-'");
                newGroup = true;
                break;
            default:
                ChatUtils.sendMessage(userID, "Неизвестная команда");
                break;
        }
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
        int messageId = callbackQuery.getMessage().getMessageId();
        long userID = callbackQuery.getMessage().getChatId();

        switch (action) {
            case "confirm":
                addInGroup(Long.parseLong(data[2]));
                deleteMessage(userID, callbackQuery.getMessage().getMessageId());
                deleteMessage(userID, Integer.parseInt(data[1]));
                break;
            case "decline":
                decline(Long.parseLong(data[2]));
                deleteMessage(userID, callbackQuery.getMessage().getMessageId());
                deleteMessage(userID, Integer.parseInt(data[1]));
                break;
            case "setGroup":
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
                break;
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
            log.info("Declined user request {} to group {}", GroupUtils.getUserName(userId, ConfigUtils.getGroupID()),
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
        //List<BotCommand> defaultCommands = new ArrayList<>();
//        defaultCommands.add(new BotCommand("/start", "Начать взаимодействие с ботом"));
//        defaultCommands.add(new BotCommand("/help", "Получить помощь"));

        // Команды для администраторов
        List<BotCommand> adminCommands = new ArrayList<>();
        adminCommands.add(new BotCommand("/set_group", "Установить группу, в которую бот будет добавлять после подтверждения"));
        adminCommands.add(new BotCommand("/new_group", "Добавить новую группу"));

        try {
            // Установка команд для всех пользователей
            //this.execute(new SetMyCommands(defaultCommands, new BotCommandScopeDefault(), null));
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

    @Override
    public void onRegister() {
        super.onRegister();
        setBotCommands();
        validator = new Validator();
    }
}
