package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.model.Session;
import bot.core.model.SessionState;
import bot.core.model.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HistoryForwardProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(HistoryForwardProcessor.class);
    @Override
    public boolean canProcess(MessageContext message, Session session) {
        SessionState state = session.getState();
        return  !message.isCommand() &&
                !state.isEditingHelp() &&
                !state.isEditingInfo() &&
                !message.isFromGroup() &&
                (message.hasText() || message.hasPayment());
    }


    @Override
    public void process(MessageContext message, Session session) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(Main.dataUtils.getHistoryId());
        forwardMessage.setMessageId(message.getMessage().getMessageId());
        forwardMessage.setFromChatId(message.getChatId());

        try {
            Main.bot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Не удалось переслать сообщение");
        }
    }
}
