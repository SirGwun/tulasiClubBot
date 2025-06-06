package bot.core.model.messageProcessing;

import bot.core.control.Session;
import bot.core.model.MessageContext;

public class HistoryForwardProcessor implements MessageProcessor {
    @Override
    public boolean canProcess(MessageContext message, Session session) {
        return false;
    }

    @Override
    public void process(MessageContext message, Session session) {

    }
}
