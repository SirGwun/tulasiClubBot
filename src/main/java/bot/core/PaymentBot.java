package bot.core;

import bot.core.control.Command;
import bot.core.control.messageProcessing.CallbackProcessor;
import bot.core.control.messageProcessing.*;

import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PaymentBot extends TelegramLongPollingBot {
    private static final String token;
    private static final String name;

    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    private final HistoryForwardProcessor historyForwardProcessor  = new HistoryForwardProcessor();
    private final List<MessageProcessor> processors = Arrays.asList(
            new AddingInGroupMessageProcessor(),
            new CallbackProcessor(),
            new CommandMessageProcessor(),
            new CommonMessageProcessor(),
            new EditHelpProcessor(),
            new EditPaymentInfoProcessor()
    );

    static {
        boolean amvera = System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1");

        if (amvera) {
            token = System.getenv( "BOTTOCKEN");
            name = System.getenv("BOTNAME");
        } else {
            try (InputStream secretInput = DataUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }
                Properties secretProperties = new Properties();
                secretProperties.load(secretInput);
                if (Main.test) {
                    token = secretProperties.getProperty("testBotToken");
                    name = secretProperties.getProperty("testBotName");
                } else {
                    token = secretProperties.getProperty("botToken");
                    name = secretProperties.getProperty("botName");
                }
            } catch (IOException e) {
                log.error("Не удалось прочитать токен и имя PaymentBot бота");
                throw new RuntimeException("Не удалось загрузить секреты", e);
            }
            log.info("PaymentBot инициализирован");
        }
    }

    public PaymentBot() {
        super(token);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            log.info("{} запущен", name);
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
        }
    }

    public static String getToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String chatTitle = message.getChat().getTitle();
            if (chatTitle == null) chatTitle = "Личных сообщений";
            log.debug("Получено новое сообщение от {} из {}", message.getFrom().getUserName(), chatTitle);
        }

        if (historyForwardProcessor.canProcess(update)) {
            historyForwardProcessor.process(update);
        }

        for (MessageProcessor processor : processors) {
            if (processor.canProcess(update)) {
                log.debug("Запущен процессор {}", processor.getClass());
                processor.process(update);
                return;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        setBotCommands();
    }

    private void setBotCommands() {
        // Команды для всех пользователей
        List<BotCommand> defaultCommands = new ArrayList<>();
        defaultCommands.add(new BotCommand("/" + Command.menu, "Главное меню"));
        defaultCommands.add(new BotCommand("/" + Command.choose_course, "Выбрать курс"));

        // Команды для администраторов
        List<BotCommand> adminCommands = new ArrayList<>();
        adminCommands.add(new BotCommand("/" + Command.menu, "Главное меню"));
        adminCommands.add(new BotCommand("/" + Command.choose_course, "Выбрать курс"));
        adminCommands.add(new BotCommand("/" + Command.set_tag, "Установить тег с которым будет добавляться группа"));
        adminCommands.add(new BotCommand("/" + Command.add_tag, "Добавить тег"));
        adminCommands.add(new BotCommand("/" + Command.set_payment_info, "Установить информацию об оплате в /start"));
        adminCommands.add(new BotCommand("/" + Command.say, "Отправить сообщение пользователю (@<username> <text>)"));
        adminCommands.add(new BotCommand("/" + Command.del, "Удалить группу"));
        adminCommands.add(new BotCommand("/" + Command.edit_help, "Изменить помощь"));
        adminCommands.add(new BotCommand("/" + Command.cancel, "Отменить действие"));
        adminCommands.add(new BotCommand("/" + Command.set_timer, "Установить время для таймеров (в минутах)"));

        try {
            execute(new SetMyCommands(defaultCommands, new BotCommandScopeAllPrivateChats(), null));
            execute(new SetMyCommands(adminCommands, new BotCommandScopeChat(Long.toString(Main.dataUtils.getAdminId())), null));
        } catch (Exception e) {
            log.error("Error setting bot commands {}", e.getMessage());
        }
    }
}
