package bot.core.control;

import bot.core.Main;
import bot.core.PaymentBot;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    public void handleCommand(String command, long userID, PaymentBot paymentBot) {
        PaymentBot.log.info("New command {}", command);
        String[] data = command.split(" ");
        switch (data[0]) {
            case "/start":
                handleStartCommand(userID);
                break;
            case "/set_group":
                handleSetGroupCommand(userID);
                break;
            case "/new_group":
                handleNewGroupCommand(userID);
                break;
            case "/cancel":
                handleCancelCommand(userID);
                break;
            case "/info":
                handleInfoCommand(userID);
                break;
            case "/help":
                handleHelpCommand(userID);
                break;
            case "/edit_info":
                handleEditInfoCommand(userID);
                break;
            case "/edit_help":
                handleEditHelpCommand(userID);
                break;
            case "/del":
                handleDelCommand(userID);
                break;
            case "/catalog":
                handleCatalogCommand(userID);
                break;
            default:
                handleUnknownCommand(userID, command);
                break;
        }
    }

    private void handleStartCommand(long userID) {
        log.info("User {} started bot", userID);
        ChatUtils.sendMessage(
                userID,
                "Здравствуйте!\n\n" +
                        "Вас приветствует, бот-помощник.\n\n" +
                        "Прошу вас сделать пожертвование за участие в лекции на карту:\n\n" +
                        "2202203650939848 (руб) Сбербанк получатель Милана Дмитриевна С. \n" +
                        "4400430384625882 (теньге) KASPI получатель Darshan Singkh\n\n" +
                        "Обязательно получите чек и далее действуйте по инструкции:"
        );

        ChatUtils.sendMessage(
                userID,
                "Инструкция \n\n" +
                        "1. Нажимаете Меню\n" +
                        "2. Выбрать группу (/set_group)\n" +
                        "3. Выбираете интересующую вас лекцию.\n" +
                        "4. Отправляете чек об оплате (документ или скриншот, фото).\n" +
                        "5. Как только пройдёт проверка, получаете ссылку на доступ к лекции (проверка занимает до одного дня, но мы стараемся как можно скорее)\n\n" +
                        "🔹 Чеки в формате PDF проверяются автоматически — доступ в большинстве случаев открывается мгновенно."
        );
    }

    private void handleSetGroupCommand(long userID) {
        log.info("User {} set group", userID);

        if (DataUtils.getGroupList().isEmpty()) {
            ChatUtils.sendMessage(userID, "Нет доступных групп");
            return;
        }
        InlineKeyboardMarkup allGroupKeyboard = ChatUtils.getAllGroupKeyboard(userID, "setGroup");
        boolean hasGroupException = false;
        for (List<InlineKeyboardButton> row : allGroupKeyboard.getKeyboard()) {
            for (InlineKeyboardButton button : row) {
                if (button.getText().startsWith("!")) {
                    hasGroupException = true;
                }
            }
        }
        if (hasGroupException) {
            ChatUtils.sendMessage(userID, "Группы помеченые \"!\" либо не существуют, либо бот не являеться в них админом\n\nРекомендую их удалить");
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userID);
        sendMessage.setText("Выберите группу");
        sendMessage.setReplyMarkup(allGroupKeyboard);
        try {
            Main.bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на команду /setGroup {}", e.getMessage());
        }
    }

    private void handleCatalogCommand(long userID) {
        log.info("user {} get /catalog command", userID);
        String catalog = DataUtils.getCatalog();
        if (catalog != null) {
            List<String> messages = MessageUtils.splitMessage(catalog, 4096);
            for (String message : messages) {
                ChatUtils.sendMessage(userID, message);
            }
        } else {
            ChatUtils.sendMessage(userID, "Каталог пока пуст");
            log.info("Ошибка при чтении каталога");
        }
    }

    private void handleNewGroupCommand(long userID) {
        log.info("User {} create new group", userID);
        if (userID == DataUtils.getAdminID()) {
            ChatUtils.sendMessage(userID, "Введите название новой группы ");
            PaymentBot.newGroup = true;
        } else {
            ChatUtils.sendMessage(userID, "Данная команда доступна только администратору");
        }
    }

    private void handleCancelCommand(long userID) {
        PaymentBot.log.info("User {} cancel command", userID);
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
