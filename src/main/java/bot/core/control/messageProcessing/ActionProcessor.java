package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.model.*;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActionProcessor implements MessageProcessor {
    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;

        MessageContext message = new MessageContext(update.getMessage());
        SessionState session = SessionController.getInstance().openSessionIfNeeded(update.getMessage().getFrom()).getState();

        return session.getAction() != EditingActions.NONE
                && message.isFromAdmin()
                && !message.getText().equals("/cancel");
    }

    @Override
    public void process(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        SessionState session = SessionController.getInstance().openSessionIfNeeded(update.getMessage().getFrom()).getState();

        switch (session.getAction()) {
            case EDIT_HELP -> {
                handleEditHelp(message);
            }
            case EDIT_PAYMENT_INFO -> {
                handleEditPaymentInfo(message);
            }
            case SENDING_SPREAD -> {
                handleSendSpread(message);
            }
        }

        session.setAction(EditingActions.NONE);
    }

    private void handleEditHelp(MessageContext message) {
        Main.dataUtils.setHelp(message.getText());
        ChatUtils.sendMessage(message.getChatId(), "Инструкция для пользователей изменена");
    }

    private void handleEditPaymentInfo(MessageContext message) {
        Main.dataUtils.setPaymentInfo(message.getText());
        ChatUtils.sendMessage(message.getFromId(), "Информация об оплате изменена");
    }

    private void handleSendSpread(MessageContext message) {
        ChatUtils.spreadToIds(Main.dataUtils.getUsrList(), message);
    }
}
