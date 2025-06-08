package bot.core.model.messageProcessing;

import bot.core.control.Session;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditInfoProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(EditInfoProcessor.class);

    @Override
    public boolean canProcess(MessageContext message, Session session) {
        return session.getState().isEditingInfo() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(MessageContext message, Session session) {
        log.info("Editing info for chatId={}", message.getChatId());
        Main.dataUtils.setInfo(message.getText());
        session.getState().editInfo();
        ChatUtils.sendMessage(message.getChatId(), "Информация изменена");
    }
}
