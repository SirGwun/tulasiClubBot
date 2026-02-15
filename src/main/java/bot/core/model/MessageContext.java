package bot.core.model;

import bot.core.Main;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.List;

public record MessageContext(Message message) {

    public Message message() {
        return message;
    }

    public boolean isFromAdmin() {
        return message.getFrom().getId() == Main.dataUtils.getAdminId();
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

    public boolean hasPhoto() {
        return message.hasPhoto();
    }

    public List<PhotoSize> getPhoto() {
        return message.getPhoto();
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
