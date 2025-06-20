package bot.core;

import bot.core.control.PaymentBot;
import bot.core.control.SessionController;
import bot.core.util.DataUtils;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot bot;
    public static DataUtils dataUtils;

    public static void main(String[] args) {
        boolean isTest = false;
        for (String arg : args) {
            if (arg.equals("--test")) {
                isTest = true;
                break;
            }
        }
        init(isTest);
    }

    public static void init(boolean isTest) {
        try {
            dataUtils = new DataUtils();
            //dataUtils.saveSessions(new ConcurrentHashMap<>());
            SessionController.getInstance();
            if (isTest) {
                log.info("Тестовый режим");
                dataUtils.testMode();
            }
            bot = new PaymentBot(dataUtils.getBotToken());
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            log.info("Бот запущен");
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
