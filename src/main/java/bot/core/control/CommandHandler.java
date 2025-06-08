package bot.core.control;

import bot.core.model.EditingActions;
import bot.core.model.MessageContext;
import bot.core.Main;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private final SessionState state;
    private final long userId;

    public CommandHandler(SessionState state, long userId) {
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
        if (userId == Main.dataUtils.getAdminId()) {
            handleAdminCommand(command);
        } else {
           handleUserCommand(command);
        }
    }

    private void handleAdminCommand(String command) {
        switch (command) {
            case "/start":
                handleStartCommand();
                break;
            case "/set_group":
                handleSetGroupCommand();
                break;
            case "/info":
                handleInfoCommand();
                break;
            case "/help":
                handleHelpCommand();
            case "/catalog":
                handleCatalogCommand();
                break;
            //***************
            case "/cancel":
                handleCancelCommand();
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
            default:
                handleUnknownCommand(command);
                break;
        }
    }

    private void handleUserCommand(String command) {
        switch (command) {
            case "/start":
                handleStartCommand();
                break;
            case "/set_group":
                handleSetGroupCommand();
                break;
            case "/info":
                handleInfoCommand();
                break;
            case "/help":
                handleHelpCommand();
            case "/catalog":
                handleCatalogCommand();
                break;
            default:
                handleUnknownCommand(command);
                break;
        }
    }

    private void handleStartCommand() {
        log.info("User {} started bot", userId);
        String START_MESSAGE = """
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
        ChatUtils.sendMessage(userId, START_MESSAGE);
    }


    private void handleSetGroupCommand() {
        log.info("User {} set group", userId);
        InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard("setGroup", userId);

        if (allGroupKeyboard.getKeyboard().isEmpty()) {
            ChatUtils.sendMessage(userId, "Нет доступных групп");
            return;
        }

        //todo научится различать такие группы и по разному их обрабатывать
        if (hasExceptedGroup(allGroupKeyboard)) {
            ChatUtils.sendMessage(userId, "В группах помеченных ! бот либо не является админом, либо их больше не существует");
        }
        ChatUtils.sendInlineKeyboard(userId, "Выберите группу", allGroupKeyboard);
    }

    private boolean hasExceptedGroup(InlineKeyboardMarkup allGroupKeyboard) {
        boolean hasGroupException = false;
        for (List<InlineKeyboardButton> row : allGroupKeyboard.getKeyboard()) {
            for (InlineKeyboardButton button : row) {
                if (button.getText().startsWith("!")) {
                    hasGroupException = true;
                }
            }
        }
        return hasGroupException;
    }

    private void handleCatalogCommand() {
        log.info("user {} get /catalog command", userId);
        String catalog = Main.dataUtils.getCatalog();

        if (catalog == null) {
            ChatUtils.sendMessage(userId, "Каталог пока пуст");
            log.info("Каталог пуст");
            return;
        }

        List<String> messages = splitMessage(catalog, 4096);
        for (String message : messages) {
            ChatUtils.sendMessage(userId, message);
        }
    }

    private List<String> splitMessage(String text, int maxLength) {
        List<String> messages = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());

            if (end < text.length() && !Character.isWhitespace(text.charAt(end))) {
                int lastSpace = text.lastIndexOf(' ', end);
                int lastNewLine = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastSpace, lastNewLine);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }

            messages.add(text.substring(start, end));
            start = end;

            while (start < text.length() && Character.isWhitespace(text.charAt(start))) {
                start++;
            }
        }
        return messages;
    }

    private void handleCancelCommand() {
        log.info("User used {} cancel command", userId);
        EditingActions action = state.cansel();
        ChatUtils.sendMessage(userId, "Режим работы над командой" + action.toString() + "отменен");
    }

    private void handleDelCommand() {
        log.info("user {} get /del command", userId);

        if (Main.dataUtils.getGroupList().isEmpty()) {
            ChatUtils.sendMessage(userId, "Не найдено ни одной группы");
            return;
        }

        InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard("delGroup" ,userId);
        ChatUtils.sendInlineKeyboard(userId, "Выберете группу для удаления", allGroupKeyboard);
    }

    private void handleInfoCommand() {
        ChatUtils.sendMessage(userId, Main.dataUtils.getInfo());
    }

    private void handleHelpCommand() {
        ChatUtils.sendMessage(userId, Main.dataUtils.getHelp());
    }

    private void handleEditInfoCommand() {
        log.info("User {} edit info", userId);
        state.editInfo();
        ChatUtils.sendMessage(userId, "Введите новое описание группы");
    }

    private void handleEditHelpCommand() {
        log.info("User {} edit help", userId);
        state.editInfo();
        ChatUtils.sendMessage(userId, "Введите новое сообщение помощи");
    }

    private void handleUnknownCommand(String message) {
        log.info("User {} send unknown command {}", userId, message);
        ChatUtils.sendMessage(userId, "Неизвестная команда");
    }
}
