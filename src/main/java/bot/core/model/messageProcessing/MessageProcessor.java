package bot.core.model.messageProcessing;


import bot.core.model.MessageContext;

public interface MessageProcessor {
    public boolean canProcess(MessageContext message);
    public void process(MessageContext message);
}
