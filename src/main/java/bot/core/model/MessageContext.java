package bot.core.model;

import bot.core.Main;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageContext  {
    private final Message message;

    public MessageContext(Message message) {
        this.message = message;
    }

    public boolean isFromAdmin() {
        return message.getFrom().getId() == Main.dataUtils.getAdminId();
    }

    public boolean isCommand() {
        return message.hasText() && message.getText().startsWith("/");
    }

    public boolean isFromGroup() {
        String type = message.getChat().getType();
        return type.equals("group") || type.equals("supergroup") || message.getChat().isChannelChat() || message.isChannelMessage();
    }

    public String getChatName() {
        return message.getChat().getTitle();
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

    public Message getMessage() {
        return message;
    }

    public long getFromId() {
        return message.getFrom().getId();
    }

    public long getChatId() {
        return message.getChatId();
    }
}
