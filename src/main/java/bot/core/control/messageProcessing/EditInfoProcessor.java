package bot.core.control.messageProcessing;

import bot.core.model.Session;
import bot.core.model.MessageContext;
import bot.core.control.SessionController;
import bot.core.util.ChatUtils;
import bot.core.Main;
import org.telegram.telegrambots.meta.api.objects.Update;

public class EditInfoProcessor implements MessageProcessor {

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .openSessionIfNeeded(update.getMessage().getFrom());
        return session.getState().isEditingInfo() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance().getUserSession(message.getFromId());
        Main.dataUtils.setInfo(message.getText());
        session.getState().cansel();
        ChatUtils.sendMessage(message.getChatId(), "Информация изменена");
    }
}
