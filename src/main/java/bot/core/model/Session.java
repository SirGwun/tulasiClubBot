package bot.core.model;

import bot.core.Legacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serial;
import java.io.Serializable;

public class Session implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(Session.class);
    @Serial
    private static final long serialVersionUID = 3L;
    long userId;
    String userName;
    Long groupId;
    SessionState state;

    public Session(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        state = new SessionState();
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public SessionState getState() {
        return state;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        if (userName == null) {
            GetChat getChat = new GetChat(String.valueOf(userId));
            try {
                Chat chat = Legacy.paymentBot.execute(getChat);
                userName = chat.getUserName();
                String firstName = chat.getFirstName();
                String lastName = chat.getLastName();
                if (userName == null) {
                    userName = (firstName == null ? "не_удалось_получить_имя" : firstName) +
                            (lastName == null ? "" : " " + lastName);
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка при получении имени пользователя {}", e.getMessage());
            }
        }
        return userName;
    }
}
