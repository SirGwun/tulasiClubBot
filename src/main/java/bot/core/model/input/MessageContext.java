package bot.core.model.input;

import bot.core.Legacy;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Incoming message with methods around
 * */

public record MessageContext(Message message) {

    public boolean isFromAdmin() {
        return message.getFrom().getId() == Legacy.dataUtils.getAdminId();
    }

    public boolean isCommand() {
        return message.hasText() && message.getText().startsWith("/");
    }

    public boolean notFromGroup() {
        String type = message.getChat().getType();
        return !type.equals("group") && !type.equals("supergroup") && !message.getChat().isChannelChat() && !message.isChannelMessage();
    }

    public String getText() {
        if (hasText()) {
            return message.getText();
        } else {
            return "";
        }
    }

    public boolean hasPayment() {
        return message.hasDocument() || message.hasPhoto();
    }

    public boolean hasText() {
        return message.hasText();
    }

    public long getFromId() {
        return message.getFrom().getId();
    }

    public long getChatId() {
        return message.getChatId();
    }

}
