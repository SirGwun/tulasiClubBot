package bot.core;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import org.slf4j.Logger;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot bot;
    public static void main(String[] args) {
        bot = new PaymentBot();
        init(bot);
        log.info("Бот запущен");
    }

    public static void init(LongPollingBot bot) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
        }
    }
}
