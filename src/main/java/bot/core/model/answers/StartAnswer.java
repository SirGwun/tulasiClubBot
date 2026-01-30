package bot.core.model.answers;

import bot.core.PaymentBot;
import bot.core.control.rout.classify.enums.Commands;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class StartAnswer implements Answer {
    PaymentBot bot;
    String text = """
                        Здравствуйте!
                        
                        Вас приветствует, бот-помощник курсов
                        Школы Аюрведы и здорового образа жизни "Tulasi"
                        """;

    public StartAnswer(PaymentBot bot) {
        this.bot = bot;
    }

    @Override
    public void send(long chatId) throws TelegramApiException {
        bot.log.debug("Send Answer to command {} for chat {}", Commands.start, chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        bot.execute(sendMessage);
    }
}
