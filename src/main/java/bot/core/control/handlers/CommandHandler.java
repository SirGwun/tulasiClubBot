package bot.core.control.handlers;

import bot.core.model.EditingActions;
import bot.core.model.MessageContext;
import bot.core.Main;
import bot.core.model.SessionState;
import bot.core.util.ChatUtils;
import bot.core.control.TimerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
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
            String[] data = message.getText().split(" ", 2);
            String arg = data.length > 1 ? data[1] : null;
            handleCommand(data[0], arg);
        } else {
            log.warn("handleCommand call with no command {}", message.getText());
        }
    }

    public void handleCommand(String command, String arg) {
        if (userId == Main.dataUtils.getAdminId()) {
            handleAdminCommand(command, arg);
        } else {
           handleUserCommand(command);
        }
    }

    private void handleAdminCommand(String command, String arg) {
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
                break;
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
            case "/set_payment_info":
                handleSetPaymentInfo();
                break;
            case "/timer":
                handleTimerCommand(arg);
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
                break;
            case "/catalog":
                handleCatalogCommand();
                break;
            default:
                handleUnknownCommand(command);
                break;
        }
    }

    private void handleStartCommand() {
        log.info("User {} use start command", userId);

        String START_MESSAGE = """
                Здравствуйте!
                Вас приветствует, бот-помощник.
                
                Инструкция как попасть на лекцию
                                
                1. Нажимаете Меню
                2. Выбрать группу (/set_group)
                3. Выбираете интересующую вас лекцию.
                4. Отправляете чек об оплате (документ или скриншот, фото).
                5. Как только пройдёт проверка, получаете ссылку на доступ к лекции (проверка занимает до 2 часов, но мы стараемся как можно скорее)
                                
                🔹 Чеки в формате PDF проверяются автоматически — доступ в большинстве случаев открывается мгновенно.
                
                """;
        String paymentInfo = Main.dataUtils.getPaymentInfo();
        ChatUtils.sendMessage(userId, START_MESSAGE + paymentInfo);

//        SendPhoto sendPhoto = new SendPhoto();
//        File file = Main.dataUtils.getPaymentPhoto();
//        sendPhoto.setPhoto(new InputFile(file));
//        sendPhoto.setChatId(userId);
//        ChatUtils.sendPhoto(sendPhoto);
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
            ChatUtils.sendMessage(userId, "Бот не входит в группы помеченные '!', либо не админ в них");
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

        if (Main.dataUtils.getGroupMap().isEmpty()) {
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
        state.editHelp();
        ChatUtils.sendMessage(userId, "Введите новое сообщение помощи");
    }

    private void handleSetPaymentInfo() {
        log.info("User {} is set's payment info", userId);
        state.editPaymentInfo();
        ChatUtils.sendMessage(userId, "Пришлите сообщение содержащее информацию о методах оплаты");
    }

    private void handleTimerCommand(String arg) {
        if (arg == null) {
            ChatUtils.sendMessage(userId, "Не указано время таймера");
            return;
        }
        try {
            long minutes = Long.parseLong(arg.trim());
            TimerController.setDefaultTime(minutes);
            Main.dataUtils.setDefaultTime(minutes);
            if (minutes < 0) {
                ChatUtils.sendMessage(userId, "Таймер отключен");
            } else {
                ChatUtils.sendMessage(userId, "Таймер установлен на " + minutes + " минут");
            }
            log.info("Admin {} set timer to {}", userId, minutes);
        } catch (NumberFormatException e) {
            ChatUtils.sendMessage(userId, "Неверное значение таймера");
            log.warn("Admin {} entered wrong timer value {}", userId, arg);
        }
    }

    private void handleUnknownCommand(String message) {
        log.info("User {} send unknown command {}", userId, message);
        ChatUtils.sendMessage(userId, "Неизвестная команда");
    }
}
