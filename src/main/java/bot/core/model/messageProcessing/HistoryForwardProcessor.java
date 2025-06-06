package bot.core.model.messageProcessing;

import bot.core.control.SessionState;
import bot.core.model.MessageContext;

public class HistoryForwardProcessor implements MessageProcessor {
    @Override
    public boolean canProcess(MessageContext message, SessionState state) {
        return false;
    }

    @Override
    public void process(MessageContext message, SessionState state) {

    }
}
