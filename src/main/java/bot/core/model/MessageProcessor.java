package bot.core.model;


public interface MessageProcessor {
    public boolean canProcess(MessageContext message);
    public void process(MessageContext message);
}
