package bot.core;

import bot.core.control.Command;
import bot.core.control.messageProcessing.CallbackProcessor;
import bot.core.control.messageProcessing.*;

import bot.core.util.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;

@Component
public class PaymentBot extends TelegramLongPollingBot {
    private final Config config;
    private final Logger log = LoggerFactory.getLogger(PaymentBot.class);

    private final HistoryForwardProcessor historyForwardProcessor  = new HistoryForwardProcessor();
    private final List<MessageProcessor> processors = Arrays.asList(
            new AddingInGroupMessageProcessor(),
            new CallbackProcessor(),
            new CommandMessageProcessor(),
            new CommonMessageProcessor(),
            new EditHelpProcessor(),
            new EditPaymentInfoProcessor()
    );

    public PaymentBot(Config config) {
        super(config.getMainBotToken());
        this.config = config;
        init();
    }

    public void init() {
        try {
            TelegramBotsApi telegramBotsApi =
                    new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            log.info("{} запущен", config.getMainBotName());
        } catch (TelegramApiException e) {
            log.error("Ошибка при регистрации paymentBot в telegram " + e.getMessage());
            throw new IllegalStateException(e);
        }
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
        return config.getMainBotName();
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
            execute(new SetMyCommands(adminCommands, new BotCommandScopeChat(Long.toString(Legacy.dataUtils.getAdminId())), null));
        } catch (Exception e) {
            log.error("Error setting bot commands {}", e.getMessage());
        }
    }
}
