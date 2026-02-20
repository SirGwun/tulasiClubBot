package bot.core.model;

import bot.core.Main;
import bot.core.control.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Session {
    private static final Logger log = LoggerFactory.getLogger(Session.class);

    long userId;
    String userName;
    Long groupId;
    EditingActions action;

    public Session(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        action = EditingActions.NONE;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public EditingActions getAction() {
        return action;
    }

    public void setAction(EditingActions action) {
        this.action = action;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        if (userName == null) {
            GetChat getChat = new GetChat(String.valueOf(userId));
            try {
                Chat chat = Main.paymentBot.execute(getChat);
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
