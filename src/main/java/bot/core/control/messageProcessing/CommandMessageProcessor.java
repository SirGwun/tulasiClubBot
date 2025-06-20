package bot.core.control.messageProcessing;

import bot.core.control.handlers.CommandHandler;
import bot.core.model.Session;
import bot.core.model.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(EditInfoProcessor.class);

    @Override
    public boolean canProcess(MessageContext message, Session session) {
        return message.isCommand();
    }

    @Override
    public void process(MessageContext message, Session session) {
        CommandHandler handler = new CommandHandler(session.getState(), message.getFromId());
        handler.handle(message);
    }
}