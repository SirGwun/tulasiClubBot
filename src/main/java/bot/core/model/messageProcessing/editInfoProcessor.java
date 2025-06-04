package bot.core.model.messageProcessing;

import bot.core.control.EditingSessionState;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class editInfoProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(editInfoProcessor.class);
    private final EditingSessionState state;

    public editInfoProcessor(EditingSessionState session) {
        this.state = session;
    }

    @Override
    public boolean canProcess(MessageContext message) {
        return state.isEditingInfo() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(MessageContext message) {
        log.info("Editing info for chatId={}", message.getChatId());
        DataUtils.setInfo(message.getText());
        state.setEditingInfo(false);
        ChatUtils.sendMessage(message.getChatId(), "Информация изменена");
    }
}
