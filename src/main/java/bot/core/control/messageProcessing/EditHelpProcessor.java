package bot.core.control.messageProcessing;

import bot.core.Legacy;
import bot.core.control.SessionService;
import bot.core.model.Session;
import bot.core.model.input.MessageContext;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class EditHelpProcessor implements MessageProcessor {

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionService.getInstance()
                .openSession(update.getMessage().getFrom());
        return session.getState().isEditingHelp() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionService.getInstance().getSession(message.getFromId());
        Legacy.dataUtils.setHelp(message.getText());
        session.getState().cansel();
        ChatUtils.sendMessage(message.getChatId(), "Инструкция для пользователей изменена");
    }
}
