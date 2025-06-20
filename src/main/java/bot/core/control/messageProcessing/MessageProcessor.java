package bot.core.control.messageProcessing;


import bot.core.model.MessageContext;
import bot.core.model.Session;

public interface MessageProcessor {
    boolean canProcess(MessageContext message, Session session);
    void process(MessageContext message, Session session);
}
