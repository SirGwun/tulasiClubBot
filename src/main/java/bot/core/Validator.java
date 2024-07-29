package bot.core;

import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Validator {
    public boolean isValidPayment(Message message) {
        //todo проверка документа или фото
        //пока бессмысленно
        if (!message.hasDocument()) {
            message.hasPhoto();
        }
        return false;
    }

    public void sendOuHumanValidation(Message message) {
        long userId = message.getFrom().getId();

        try {
            ForwardMessage forwardMessage = new ForwardMessage();
            forwardMessage.setChatId(ConfigUtils.getAdminChatID());
            forwardMessage.setFromChatId(message.getChatId());
            forwardMessage.setMessageId(message.getMessageId());
            Message forwardedMessage = Main.bot.execute(forwardMessage);

            // Отправляем сообщение с кнопками
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(ConfigUtils.getAdminChatID());
            sendMessage.setText("Примите или отклоните пользователя");
            sendMessage.setReplyMarkup(ChatUtils.getValidationKeyboard(forwardedMessage.getMessageId(), userId));
            Main.bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            Main.log.error("Ошибка при отправке сообщения администратору", e);
        }
    }
}
