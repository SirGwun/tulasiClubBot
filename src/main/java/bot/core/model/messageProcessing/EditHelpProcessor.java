package bot.core.model.messageProcessing;

import bot.core.control.SessionState;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditHelpProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(EditInfoProcessor.class);
    private final SessionState state;

    public EditHelpProcessor(SessionState session) {
        this.state = session;
    }

    @Override
    public boolean canProcess(MessageContext message) {
        return state.isEditingHelp() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(MessageContext message) {
        log.info("Editing help for chatId={}", message.getChatId());
        DataUtils.setInfo(message.getText());
        state.editHelp();
        ChatUtils.sendMessage(message.getChatId(), "Инструкция для пользователей изменена");
    }
}
