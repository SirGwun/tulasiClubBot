package bot.core.control.messageProcessing;

import bot.core.Legacy;
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
        return message.isFromAdmin() && session.getState().isEditPaymentInfo();
    }

    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance().getUserSession(message.getFromId());
        Legacy.dataUtils.setPaymentInfo(message.getText());
        session.getState().cansel();
        ChatUtils.sendMessage(message.getFromId(), "Информация об оплате изменена");
    }
}
