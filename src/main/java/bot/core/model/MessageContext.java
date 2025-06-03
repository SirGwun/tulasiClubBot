package bot.core.model;

import bot.core.util.DataUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageContext  {
    private final Message message;

    private final long chatID;

    public MessageContext(Message message) {
        this.message = message;
        this.chatID = message.getChatId();
    }

    public boolean isFromAdmin() {
        return message.getChatId() == DataUtils.getAdminID();
    }

    public boolean isCommand() {
        return message.hasText() && message.getText().startsWith("/");
    }

    public boolean isGroup() {
        String type = message.getChat().getType();
        return type.equals("group") || type.equals("supergroup");
    }

    public String getText() {
        if (hasText()) {
            return message.getText();
        } else {
            return "";
        }
    }

    public boolean hasText() {
        return message.hasText();
    }

    public long getChatID() {
        return chatID;
    }
}
