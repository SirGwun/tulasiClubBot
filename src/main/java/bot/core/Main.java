package bot.core;

import bot.core.control.Session;
import bot.core.util.DataUtils;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot bot;
    public static DataUtils dataUtils;
    public static Map<Long, Session> sessionByUser = new ConcurrentHashMap<>();
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
            if (isTest) {
                log.info("Тестовый режим");
                dataUtils.testMode();
            } else {
                //во избежание работы 2 инстенсов на продакшене и ошибок, с этим связаных
                try {
                    Thread.sleep(5000); //5 секунд
                } catch (InterruptedException e) {
                    log.error("Error sleep {}", e.getMessage());
                }
            }
            bot = new PaymentBot(dataUtils.getBotToken());
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<Long, Session> getSessionByUser() {
        return sessionByUser;
    }
}
