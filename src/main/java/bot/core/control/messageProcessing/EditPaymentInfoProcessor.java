package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.model.Session;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;

public class EditPaymentInfoProcessor implements MessageProcessor {
    @Override
    public boolean canProcess(MessageContext message, Session session) {
        return message.isFromAdmin() && session.getState().isEditPaymentInfo();
    }

    @Override
    public void process(MessageContext message, Session session) {
        Main.dataUtils.setPaymentInfo(message.getText());
        session.getState().cansel();
        ChatUtils.sendMessage(message.getFromId(), "Информация об оплате изменена");
    }
}
