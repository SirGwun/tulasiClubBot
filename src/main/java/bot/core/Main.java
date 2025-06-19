package bot.core;

import bot.core.control.Session;
import bot.core.model.SessionController;
import bot.core.util.DataUtils;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot bot;
    public static DataUtils dataUtils;
    public static boolean isTest = false;

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.equals("--test")) {
                isTest = true;
                break;
            }
        }
        init();
        log.info("Бот запущен");
    }

    public static void init() {
        try {
            dataUtils = new DataUtils();
            SessionController.getInstance();
            if (isTest) {
                log.info("Тестовый режим");
                dataUtils.testMode();
            }
            bot = new PaymentBot(dataUtils.getBotToken());
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
