package bot.core.model.messageProcessing;

import bot.core.control.SessionState;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditInfoProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(EditInfoProcessor.class);

    @Override
    public boolean canProcess(MessageContext message, SessionState state) {
        return state.isEditingInfo() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(MessageContext message, SessionState state) {
        log.info("Editing info for chatId={}", message.getChatId());
        DataUtils.setInfo(message.getText());
        state.editInfo();
        ChatUtils.sendMessage(message.getChatId(), "Информация изменена");
    }
}
