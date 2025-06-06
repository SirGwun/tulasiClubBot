package bot.core.model;

import bot.core.util.DataUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageContext  {
    private final Message message;

    private final long chatId;

    public MessageContext(Message message) {
        this.message = message;
        this.chatId = message.getChatId();
    }

    public boolean isFromAdmin() {
        return message.getChatId() == DataUtils.getAdminID();
    }

    public boolean isCommand() {
        return message.hasText() && message.getText().startsWith("/");
    }

    public boolean isFromGroup() {
        String type = message.getChat().getType();
        return type.equals("group") || type.equals("supergroup") || message.getChat().isChannelChat();
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

    public long getChatId() {
        return chatId;
    }

    public Message getMessage() {
        return message;
    }
}
