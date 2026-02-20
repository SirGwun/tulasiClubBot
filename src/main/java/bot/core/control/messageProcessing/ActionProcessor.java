package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.SessionService;
import bot.core.model.*;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ActionProcessor implements MessageProcessor {
    SessionService sessionService = SessionService.getInstance();
    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;

        MessageContext message = new MessageContext(update.getMessage());
        long userId = update.getMessage().getFrom().getId();

        return sessionService.getAction(userId) != EditingActions.NONE
                && message.isFromAdmin()
                && !message.getText().equals("/cancel");
    }

    @Override
    public void process(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        Long userId = update.getMessage().getFrom().getId();

        switch (sessionService.getAction(userId)) {
            case EditingActions.EDIT_HELP -> {
                handleEditHelp(message);
                sessionService.setSessionAction(userId, EditingActions.NONE);
            }
            case EditingActions.EDIT_PAYMENT_INFO -> {
                handleEditPaymentInfo(message);
                sessionService.setSessionAction(userId, EditingActions.NONE);
            }
            case EditingActions.SENDING_SPREAD_WITHOUT_BUTTON -> {
                handleSendSpreadWithoutButton(message);
                sessionService.setSessionAction(userId, EditingActions.NONE);
            }
            case EditingActions.SENDING_SPREAD -> {
                handleSendSpread(message);
                sessionService.setSessionAction(userId, EditingActions.NONE);
            }
        }
    }

    private void handleEditHelp(MessageContext message) {
        Main.dataUtils.setHelp(message.getText());
        ChatUtils.sendMessage(message.getChatId(), "Инструкция для пользователей изменена");
    }

    private void handleEditPaymentInfo(MessageContext message) {
        Main.dataUtils.setPaymentInfo(message.getText());
        ChatUtils.sendMessage(message.getFromId(), "Информация об оплате изменена");
    }

    private void handleSendSpreadWithoutButton(MessageContext message) {
        //ChatUtils.spreadToIds(List.of(Main.dataUtils.getAdminId()), message, false);
        ChatUtils.spreadToIds(Main.dataUtils.getUsrList(), message, false);
        ChatUtils.sendMessage(message.getChatId(), "Сообщение успешно разослано");
    }

    private void handleSendSpread(MessageContext message) {
        //ChatUtils.spreadToIds(List.of(Main.dataUtils.getAdminId()), message, true);
        ChatUtils.spreadToIds(Main.dataUtils.getUsrList(), message, true);
        ChatUtils.sendMessage(message.getChatId(), "Сообщение успешно разослано c кнопкой");
    }
}
