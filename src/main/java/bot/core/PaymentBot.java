package bot.core;

import java.io.InputStream;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    private final long adminChatId = 7066573077L; // ID админа
    private final long groupID = -4282643859L;

    private String bottocken;
    Validator validator = new Validator();

    public PaymentBot() {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                log.error("Sorry, unable to find config.properties");
                return;
            }
            Properties properties = new Properties();
            properties.load(input);
            bottocken = properties.getProperty("bottocken");
        } catch (FileNotFoundException e) {
            Main.log.error("File not found", e);
        } catch (IOException e) {
            Main.log.error("IO error", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && !update.getMessage().isGroupMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            long userId = message.getFrom().getId();
            //System.out.println(userId + " " + chatId);

            if (message.hasDocument() || message.hasPhoto()) {
                handlePayment(message, chatId, userId);
            } else {
                sendMessage(chatId, "Пожалуйста приложите документ или фото платежа");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
        if (update.hasMessage() && update.getMessage().isGroupMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            System.out.println(chatId);
        }
    }

    private void handlePayment(Message message, long chatId, long userId) {
        boolean valid = validator.isValidPayment(message);

        if (message.hasDocument() && !valid) {
            sendMessage(chatId, "Оплата не подтверждена");
        } else if (valid) {
            addInGroup(userId);
            sendMessage(chatId, "Оплата подтверждена");
        } else {
            sendOuHumanValidation(message);
            sendMessage(chatId, "Ваше подтверждение отправлено на проверку. Пожалуйста, подождите.\n \n" +
                    "Как только проверка завершится, бот пришлет вам ссылку для вступления в группу.");
        }
    }


    private void sendOuHumanValidation(Message message) {
        long userId = message.getFrom().getId();

        try {
            ForwardMessage forwardMessage = new ForwardMessage();
            forwardMessage.setChatId(adminChatId);
            forwardMessage.setFromChatId(message.getChatId());
            forwardMessage.setMessageId(message.getMessageId());
            Message forwardedMessage = execute(forwardMessage);

            // Отправляем сообщение с кнопками
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(adminChatId);
            sendMessage.setText("Примите или отклоните пользователя");
            sendMessage.setReplyMarkup(getValidationKeyboard(forwardedMessage.getMessageId(), userId));
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения администратору", e);
        }
    }

    private InlineKeyboardMarkup getValidationKeyboard(int messageId, long userId) {
        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("Принимаю");
        confirmButton.setCallbackData("confirm_" + messageId + "_" + userId);

        InlineKeyboardButton declineButton = new InlineKeyboardButton();
        declineButton.setText("Отказываю");
        declineButton.setCallbackData("decline_" + messageId + "_" + userId);

        List<InlineKeyboardButton> row = Arrays.asList(confirmButton, declineButton);
        List<List<InlineKeyboardButton>> rows = Arrays.asList(row);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        return keyboard;
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
                removeUserFromGroup(userId);
                deleteMessage(adminChatId, callbackQuery.getMessage().getMessageId());
                deleteMessage(adminChatId, messageId);
                break;
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        System.out.println("delited messageId: " + messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при удалении сообщения", e);
        }
    }

    private void removeUserFromGroup(long userId) {
        System.out.println("Del form group");
        sendMessage(userId, "Ваша заявка была отклонена, \n" +
                "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
        BanChatMember ban = new BanChatMember();
        ban.setChatId(groupID);
        ban.setUserId(userId);
        try {
            execute(ban);
        } catch (TelegramApiException e) {
            log.error("Ошибка при бане пользователя в группе", e);
        }
    }

    private void addInGroup(long userId) {
        CreateChatInviteLink inviteLink = new CreateChatInviteLink();
        inviteLink.setChatId(groupID);
        inviteLink.setName("Добро пожаловать на курс!");
        // Получаем текущее время в секундах
        long nowSeconds = Instant.now().getEpochSecond();
        long expireSeconds = nowSeconds + (24 * 60 * 60);
        if (expireSeconds > Integer.MAX_VALUE || expireSeconds < Integer.MIN_VALUE) {
            throw new ArithmeticException("Unix timestamp выходит за пределы диапазона Integer");
        }
        Integer expireDate = (int) expireSeconds;
        //System.out.println("Unix timestamp для даты через день: " + expireDate);
        inviteLink.setExpireDate(expireDate);
        inviteLink.setMemberLimit(1);

        try {
            ChatInviteLink invite = execute(inviteLink);
            String inviteLinkUrl = invite.getInviteLink();
            SendMessage message = new SendMessage();
            message.setChatId(userId);
            message.setText("Здравствуйте!\n\nОплата подтверждена. Для присоединения к группе перейдите по ссылке ниже:\n\n" +
                    "<a href=\"" + inviteLinkUrl + "\">Присоединиться к курсу</a>\n\n" +
                    "Мы рады вас видеть!");
            message.setParseMode(ParseMode.HTML);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу", e);
        }
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }


    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        }
        catch (TelegramApiException e) {
            Main.log.error("Ошибка при отправке сообщения", e);
        }
    }
    @Override
    public String getBotToken() {
        return bottocken;
    }

    @Override
    public String getBotUsername() {
        return "tulasiClubBot";
    }
}
