package bot.core.model;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageProcessor {
    public boolean canProcess(Message message);
    public void process(Message message);
}
