package bot.core.control.messageProcessing;

import bot.core.control.handlers.CommandHandler;
import bot.core.model.MessageContext;
import bot.core.control.SessionController;
import bot.core.model.Session;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CommandMessageProcessor implements MessageProcessor {

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;
        MessageContext message = new MessageContext(update.getMessage());
        return message.isCommand();
    }

    @Override
    public void process(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .openSessionIfNeeded(update.getMessage().getFrom());
        CommandHandler handler = new CommandHandler(session.getState(), message.getFromId());
        handler.handle(message);
    }
}