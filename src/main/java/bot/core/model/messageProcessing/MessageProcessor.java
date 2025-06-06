package bot.core.model.messageProcessing;


import bot.core.model.MessageContext;
import bot.core.control.SessionState;

public interface MessageProcessor {
    boolean canProcess(MessageContext message, SessionState state);
    void process(MessageContext message, SessionState state);
}
