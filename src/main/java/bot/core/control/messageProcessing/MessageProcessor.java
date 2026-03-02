package bot.core.control.messageProcessing;


import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

public interface MessageProcessor extends Serializable {
    boolean canProcess(Update update);
    void process(Update update);
}
