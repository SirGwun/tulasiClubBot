package bot.core;

import bot.core.control.CallbackHandler;
import bot.core.control.Session;
import bot.core.model.MessageContext;
import bot.core.model.SessionController;
import bot.core.model.messageProcessing.*;
import bot.core.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;

import java.util.*;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    CallbackHandler callbackHandler = new CallbackHandler();
    Validator validator;
    HistoryForwardProcessor historyForwardProcessor  = new HistoryForwardProcessor();
    AddingInGroupMessageProcessor addingProcessor = new AddingInGroupMessageProcessor();
    List<MessageProcessor> processors = Arrays.asList(
            new CommandMessageProcessor(),
            new CommonMessageProcessor(),
            addingProcessor,
            new EditInfoProcessor(),
            new EditHelpProcessor(),
            new EditPaymentInfoProcessor()
    );

    public PaymentBot(String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        //log.info(update.toString());
        if (update.hasMessage()) {
            handleIncomingMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallbackQuery(update.getCallbackQuery());
        } else if (addingProcessor.canProcess(update)) {
            addingProcessor.process(update.getMyChatMember());
        }
    }

    private void handleIncomingMessage(Message message) {
        long fromId = message.getFrom().getId();
        long chatId = message.getChatId();

        log.info("Получено новое сообщение от {}", fromId);
        log.info("Оно касается действий в {} - {}", chatId, message.getChat().getTitle());

        Session session = SessionController.getInstance().openSessionIfNeeded(fromId);

        MessageContext ctx = new MessageContext(message);

        if (historyForwardProcessor.canProcess(ctx, session)) historyForwardProcessor.process(ctx, session);
        for (MessageProcessor processor : processors) {
            if (processor.canProcess(ctx, session)) {
                processor.process(ctx, session);
                return;
            }
        }
    }


    @Override
    public String getBotUsername() {
        return Main.dataUtils.getBotName();
    }

    @Override
    public void onRegister() {
        super.onRegister();
        setBotCommands();
        validator = new Validator();
    }

    private void setBotCommands() {
        // Команды для всех пользователей
        List<BotCommand> defaultCommands = new ArrayList<>();
        defaultCommands.add(new BotCommand("/set_group", "Выбрать группу"));
        defaultCommands.add(new BotCommand("/catalog", "Каталог всех лекций"));
        defaultCommands.add(new BotCommand("/info", "Информация о группе"));
        defaultCommands.add(new BotCommand("/help", "Помощь"));

        // Команды для администраторов
        List<BotCommand> adminCommands = new ArrayList<>(); //todo добавить startEditCatalog endEditCatalog
        adminCommands.add(new BotCommand("/set_group", "Установить текущую группу"));
        adminCommands.add(new BotCommand("/del", "Удалить группу"));
        adminCommands.add(new BotCommand("/set_payment_info", "Установить информацию об оплате"));
        adminCommands.add(new BotCommand("/edit_info", "Изменить информацию"));
        adminCommands.add(new BotCommand("/edit_help", "Изменить помощь"));
        adminCommands.add(new BotCommand("/cancel", "Отменить действие"));
        try {
            execute(new SetMyCommands(defaultCommands, new BotCommandScopeAllPrivateChats(), null));
            execute(new SetMyCommands(adminCommands, new BotCommandScopeChat(Long.toString(Main.dataUtils.getAdminId())), null));
        } catch (Exception e) {
            log.error("Error setting bot commands {}", e.getMessage());
        }
    }
}
