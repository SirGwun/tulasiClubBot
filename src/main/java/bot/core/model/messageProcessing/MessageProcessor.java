package bot.core.model.messageProcessing;


import bot.core.model.MessageContext;
import bot.core.control.Session;

public interface MessageProcessor {
    boolean canProcess(MessageContext message, Session session);
    void process(MessageContext message, Session session);
}
