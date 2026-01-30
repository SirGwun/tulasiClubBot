package bot.core.model.answers;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Answer {
    void send(long chatId) throws TelegramApiException;
}
