package bot.core;

import org.telegram.telegrambots.meta.api.objects.Message;

public class Validator {
    public boolean isValidPayment(Message message) {
        //todo проверка документа или фото
        //пока бессмысленно
        if (message.hasDocument() || message.hasPhoto()) {
            return false;
        }
        return false;
    }
}
