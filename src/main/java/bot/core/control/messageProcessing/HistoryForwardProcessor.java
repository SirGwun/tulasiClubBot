package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.SessionService;
import bot.core.model.EditingActions;
import bot.core.model.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HistoryForwardProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(HistoryForwardProcessor.class);
    SessionService sessionService = SessionService.getInstance();

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;
        MessageContext message = new MessageContext(update.getMessage());
        long userId = update.getMessage().getFrom().getId();

        return !message.isCommand() &&
                sessionService.getAction(userId) == EditingActions.NONE &&
                message.notFromGroup() &&
                (message.hasText() || message.hasPayment());
    }


    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        MessageContext message = new MessageContext(update.getMessage());
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(Main.dataUtils.getHistoryId());
        forwardMessage.setMessageId(message.message().getMessageId());
        forwardMessage.setFromChatId(message.getChatId());

        try {
            Main.paymentBot.execute(forwardMessage);
        } catch (TelegramApiException e) {
            log.error("Не удалось переслать сообщение");
        }
    }
}
