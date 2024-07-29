package bot.core;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import org.slf4j.Logger;

import java.util.List;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot bot;
    public static boolean isTest = false;
    public static void main(String[] args) {
        for (String arg : args) {
            isTest = arg.equals("--test");
        }
        bot = new PaymentBot();
        init(bot);
        log.info("Бот запущен");
//        System.out.println("Группа 1 " + isBotAdminInGroup(-1002211393792L));
//        System.out.println("Группа 2 " + isBotAdminInGroup(-4282090457L));
//        System.out.println("Группа 3 " + isBotAdminInGroup(-42L));
    }

    public static void init(LongPollingBot bot) {
        try {
            if (isTest) {
                log.info("Тестовый режим");
                ConfigUtils.testMode();
            }
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
        }
    }
}
