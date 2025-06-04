package bot.core.model.messageProcessing;

import bot.core.control.CommandHandler;
import bot.core.control.EditingSessionState;
import bot.core.model.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class commandMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(editInfoProcessor.class);
    private final EditingSessionState state;

    public commandMessageProcessor(EditingSessionState state) {
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