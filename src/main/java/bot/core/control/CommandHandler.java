package bot.core.control;

import bot.core.Main;
import bot.core.PaymentBot;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private EditingSessionState state;
    private long userId;

    private final String START_MESSAGE = """
                Здравствуйте!
                
                Вас приветствует, бот-помощник.
                
                Прошу вас сделать пожертвование за участие в лекции на карту:
                
                2202203650939848 (руб) Сбербанк получатель Милана Дмитриевна С.\s
                4400430384625882 (теньге) KASPI получатель Darshan Singkh
                
                Обязательно получите чек и далее действуйте по инструкции:
               
                Инструкция
                
                1. Нажимаете Меню
                2. Выбрать группу (/set_group)
                3. Выбираете интересующую вас лекцию.
                4. Отправляете чек об оплате (документ или скриншот, фото).
                5. Как только пройдёт проверка, получаете ссылку на доступ к лекции (проверка занимает до одного дня, но мы стараемся как можно скорее)
                
                🔹 Чеки в формате PDF проверяются автоматически — доступ в большинстве случаев открывается мгновенно.
                """;

    public CommandHandler(EditingSessionState state, long userId) {
        this.state = state;
        this.userId = userId;
    }

    public void handle(MessageContext message) {
        if (message.isCommand()) {
            String[] data = message.getText().split(" ");
            log.info("New command {}", data[0]);
            handleCommand(data[0]);
        } else {
            log.warn("handleCommand call with no command {}", message.getText());
        }
    }

    public void handleCommand(String command) {
        switch (command) {
            case "/start":
                handleStartCommand();
                break;
            case "/set_group":
                handleSetGroupCommand();
                break;
            case "/new_group":
                handleNewGroupCommand();
                break;
            case "/cancel":
                handleCancelCommand();
                break;
            case "/info":
                handleInfoCommand();
                break;
            case "/help":
                handleHelpCommand();
                break;
            case "/edit_info":
                handleEditInfoCommand();
                break;
            case "/edit_help":
                handleEditHelpCommand();
                break;
            case "/del":
                handleDelCommand();
                break;
            case "/catalog":
                handleCatalogCommand();
                break;
            default:
                handleUnknownCommand();
                break;
        }
    }

    private void handleStartCommand(long userID) {
        log.info("User {} started bot", userID);
        ChatUtils.sendMessage(userID, START_MESSAGE);
    }

    private void handleSetGroupCommand() {
        log.info("User {} set group", userId);

        if (DataUtils.getGroupList().isEmpty()) {
            ChatUtils.sendMessage(userId "Нет доступных групп");
            return;
        }

        InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(userID, "setGroup");
        if (MessageUtils.hasExceptedGroup(allGroupKeyboard)) {
            ChatUtils.sendMessage(userId, "Группы помеченные \"!\" либо не существуют, либо бот не является в них админом\n\nРекомендую их удалить");
        }

        ChatUtils.sendInlineKeyboard(userId, "Выберите группу", allGroupKeyboard);
    }

    private void handleCatalogCommand() {
        log.info("user {} get /catalog command", userId);
        String catalog = DataUtils.getCatalog();

        if (catalog == null) {
            ChatUtils.sendMessage(userId, "Каталог пока пуст");
            log.info("Каталог пуст");
            return;
        }

        List<String> messages = MessageUtils.splitMessage(catalog, 4096);
        for (String message : messages) {
            ChatUtils.sendMessage(userId, message);
        }
    }

    private void handleNewGroupCommand() {
        log.info("User {} create new group", userId);
        if (userId == DataUtils.getAdminID()) {
            ChatUtils.sendMessage(userId, "Введите название новой группы");
            state.setWaitingGroupName(true);
        } else {
            ChatUtils.sendMessage(userId, "Данная команда доступна только администратору");
        }
    }

    private void handleCancelCommand(long userID) {
        log.info("User {} cancel command", userID);
        if (userID == DataUtils.getAdminID()) {
            PaymentBot.newGroup = false;
            PaymentBot.newGroupName = null;
            ChatUtils.sendMessage(userID, "Режим работы над командой отменен");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleInfoCommand(long userID) {
        ChatUtils.sendMessage(userID, DataUtils.getInfo());
    }

    private void handleHelpCommand(long userID) {
        ChatUtils.sendMessage(userID, DataUtils.getHelp());
    }

    private void handleEditInfoCommand(long userID) {
        log.info("User {} edit info", userID);
        if (userID == DataUtils.getAdminID()) {
            PaymentBot.editInfo = true;
            ChatUtils.sendMessage(userID, "Введите новое описание группы");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleEditHelpCommand(long userID) {
        log.info("User {} edit help", userID);
        if (userID == DataUtils.getAdminID()) {
            PaymentBot.editHelp = true;
            ChatUtils.sendMessage(userID, "Введите новое сообщение помощи");
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleUnknownCommand(long userID, String message) {
        log.info("User {} send unknown command {}", userID, message);
        ChatUtils.sendMessage(userID, "Неизвестная команда");
    }
}
