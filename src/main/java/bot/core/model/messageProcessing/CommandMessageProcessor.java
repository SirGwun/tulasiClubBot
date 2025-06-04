package bot.core.model.messageProcessing;

import bot.core.control.CommandHandler;
import bot.core.control.SessionState;
import bot.core.model.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(EditInfoProcessor.class);
    private final SessionState state;

    public CommandMessageProcessor(SessionState state) {
        this.state = state;
    }

    @Override
    public boolean canProcess(MessageContext message) {
        return message.isCommand();
    }

    @Override
    public void process(MessageContext message) {
        CommandHandler handler = new CommandHandler(state, message.getChatId());
        handler.handle(message);
    }
}