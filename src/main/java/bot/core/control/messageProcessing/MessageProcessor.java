package bot.core.control.messageProcessing;


import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageProcessor {
    boolean canProcess(Update update);
    void process(Update update);
}
