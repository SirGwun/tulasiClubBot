package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.model.EditingActions;
import bot.core.model.Session;
import bot.core.model.MessageContext;
import bot.core.control.SessionController;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class EditPaymentInfoProcessor implements MessageProcessor {
    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;

        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .openSessionIfNeeded(update.getMessage().getFrom());

        return message.isFromAdmin() && session.getState().getAction() == EditingActions.EDIT_PAYMENT_INFO;
    }

    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        MessageContext message = new MessageContext(update.getMessage());

        Session session = SessionController.getInstance().getUserSession(message.getFromId());
        Main.dataUtils.setPaymentInfo(message.getText());
        session.getState().setAction(EditingActions.NONE);
        ChatUtils.sendMessage(message.getFromId(), "Информация об оплате изменена");
    }
}
