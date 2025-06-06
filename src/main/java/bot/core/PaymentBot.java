package bot.core;

import bot.core.control.CallbackHandler;
import bot.core.control.SessionState;
import bot.core.model.Group;
import bot.core.model.MessageContext;
import bot.core.model.messageProcessing.*;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.util.GroupUtils;
import bot.core.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    CallbackHandler callbackHandler = new CallbackHandler();
    Validator validator;
    Map<Long, SessionState> sessionByUser = new ConcurrentHashMap<>();
    List<MessageProcessor> processors = Arrays.asList(
            new CommandMessageProcessor(),
            new SetGroupNameProcessor(),
            new EditInfoProcessor(),
            new EditHelpProcessor(),
            new GroupMessageProcessor(),
            new HistoryForwardProcessor(),
            new CommonMessageProcessor()
    );




    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleIncomingUpdate(update.getMessage());
        } else if (update.hasMyChatMember()) {
            handleMyChatMemberUpdate(update.getMyChatMember());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleMyChatMemberUpdate(ChatMemberUpdated chatMemberUpdated) {
        try {
            Chat chat = chatMemberUpdated.getChat();
            ChatMember oldStatus = chatMemberUpdated.getOldChatMember();
            ChatMember newStatus = chatMemberUpdated.getNewChatMember();

            Long chatId = chat.getId();
            String chatType = chat.getType();
            boolean wasMember = oldStatus.getStatus().equals("member") || oldStatus.getStatus().equals("administrator");
            boolean isMemberNow = newStatus.getStatus().equals("member") || newStatus.getStatus().equals("administrator");

            // Бот добавлен в чат (группу/канал)
            if (!wasMember && isMemberNow && newStatus.getUser().getId().equals(this.getMe().getId())) {
                log.info("Bot added to {} {}", chatType, chatId);

                if (DataUtils.getGroupList().containsValue(chatId.toString())) {
                    String existingName = "";
                    for (Map.Entry<Object, Object> entry : DataUtils.getGroupList().entrySet()) {
                        if (entry.getValue().equals(chatId.toString())) {
                            existingName = entry.getKey().toString();
                            break;
                        }
                    }
                    ChatUtils.sendMessage(DataUtils.getAdminID(),
                            (chatType.equals("channel") ? "Канал" : "Группа") + " уже есть в списке. Имя: " + existingName +
                                    "\nПожалуйста, используйте уже добавленный чат с помощью команды /set_group");
                    editingSession.setPendingGroupName(null);
                    editingSession.setWaitingGroupName(false);
                    return;
                }

                InlineKeyboardMarkup keyboard = ChatUtils.getConfirmAdminStatusKeyboard(new Group(newGroupName, chatId));
                sendAdminConfirmationMessage(newGroupName, keyboard);
            }

            // Бот удалён из чата
            if (wasMember && !isMemberNow && newStatus.getUser().getId().equals(this.getMe().getId())) {
                log.info("Bot removed from {} {}", chatType, chatId);
                // Можно добавить очистку или логику при удалении бота из группы/канала
            }
        } catch (TelegramApiException e) {
            log.error("Error handling chat member update", e);
        }
    }


    private void handleIncomingUpdate(Message message) {
        long chatId = message.getChatId();
        SessionState state = sessionByUser.computeIfAbsent(chatId, id -> new SessionState());

        MessageContext ctx = new MessageContext(message);

        for (MessageProcessor processor : processors) {
            if (processor.canProcess(ctx, state)) {
                processor.process(ctx, state);
            }
        }
    }


    private void setBotCommands() {
        //todo разобраться, как убрать / в чатах групп

        // Команды для всех пользователей
        List<BotCommand> defaultCommands = new ArrayList<>();
        defaultCommands.add(new BotCommand("/set_group", "Выбрать группу"));
        defaultCommands.add(new BotCommand("/catalog", "Каталог всех лекций"));
        defaultCommands.add(new BotCommand("/info", "Информация о группе"));
        defaultCommands.add(new BotCommand("/help", "Помощь"));

        // Команды для администраторов
        List<BotCommand> adminCommands = new ArrayList<>();
        adminCommands.add(new BotCommand("/new_group", "Добавить группу"));
        adminCommands.add(new BotCommand("/set_group", "Установить текущую группу"));
        adminCommands.add(new BotCommand("/del", "Удалить группу"));
        adminCommands.add(new BotCommand("/edit_info", "Изменить информацию"));
        adminCommands.add(new BotCommand("/edit_help", "Изменить помощь"));
        adminCommands.add(new BotCommand("/cancel", "Отменить действие"));
        try {
            execute(new SetMyCommands(defaultCommands, new BotCommandScopeAllPrivateChats(), null));
            long adminChatId = DataUtils.getAdminID();
            execute(new SetMyCommands(adminCommands, new BotCommandScopeChat(Long.toString(DataUtils.getAdminID())), null));
        } catch (Exception e) {
            log.error("Error setting bot commands {}", e.getMessage());
        }
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotToken() {
        return DataUtils.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return DataUtils.getBotName();
    }

    public static String getNewGroupName() {
        return newGroupName;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        setBotCommands();
        validator = new Validator();
    }
}
