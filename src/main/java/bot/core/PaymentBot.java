package bot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Objects;
import java.util.Properties;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    Validator validator = new Validator();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
                handleCommand(update.getMessage().getText(), update.getMessage().getChatId());
            }
            handleIncomingMessage(update.getMessage());
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
         if (userID == ConfigUtils.getAdminChatId()) {
             if (data[0].equalsIgnoreCase("/setGroup")) {
                 Properties groupList = ConfigUtils.getGroupList();
                 for (Object group: groupList.keySet()) {
                     String groupName = (String) group;
                     if (data[1].equalsIgnoreCase(groupName)) {
                         ConfigUtils.updateConfig("groupID", groupList.getProperty(groupName));
                         ChatUtils.sendMessage(userID, "Текущая группа установлена");
                         return;
                     }
                 }
                 ChatUtils.sendMessage(userID, "Группа не найдена");
             }
         } else {
             ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
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
        int messageId = Integer.parseInt(data[1]);
        long userId = Long.parseLong(data[2]);
        long adminChatId = callbackQuery.getMessage().getChatId();

        switch (action) {
            case "confirm":
                addInGroup(userId);
                deleteMessage(adminChatId, callbackQuery.getMessage().getMessageId());
                deleteMessage(adminChatId, messageId);
                break;
            case "decline":
                decline(userId);
                deleteMessage(adminChatId, callbackQuery.getMessage().getMessageId());
                deleteMessage(adminChatId, messageId);
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

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotToken() {
        return ConfigUtils.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return "tulasiClubBot";
    }
}
