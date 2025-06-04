package bot.core.control;

import bot.core.Main;
import bot.core.model.EditingActions;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SessionState {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public String pendingGroupName = null;
    EditingActions action;

    public boolean isEditingHelp(Message message) {
        return editingHelp && message.getChatId() == DataUtils.getAdminID();
    }

    public boolean isNewGroupMember(Message message) {
        if (pendingGroupName == null) return false;

        if (message.isGroupMessage() && message.getNewChatMembers() != null) {
            for (User user : message.getNewChatMembers()) {
                try {
                    if (user.getId().equals(Main.bot.getMe().getId())) {
                        return true;
                    }
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
            }
            return false;
        }

        return message.isChannelMessage(); // бот получил сообщение из канала, этого достаточно
    }

    public String getPendingGroupName() {
        return pendingGroupName;
    }
    public void setPendingGroupName(String pendingGroupName) {
        this.pendingGroupName = pendingGroupName;
    }

    public boolean isWaitingGroupName() {
        return action == EditingActions.ADD_GROUP;
    }
    public void waitGroupName() {
        action = EditingActions.ADD_GROUP;
    }

    public boolean isEditingInfo() {
        return action == EditingActions.EDIT_INFO;
    }
    public void editInfo() {
        action = EditingActions.EDIT_INFO;
    }

    public boolean isEditingHelp() {
        return action == EditingActions.EDIT_HELP;
    }
    public void editHelp(boolean editingHelp) {
        action = EditingActions.EDIT_HELP;
    }

    public EditingActions cansel() {
        EditingActions currentAction = action;
        action = EditingActions.NONE;
        return currentAction;
    }
}
