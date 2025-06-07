package bot.core.control;

import bot.core.Main;
import bot.core.model.EditingActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SessionState {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public String pendingGroupName = null;
    EditingActions action;

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
    public void editHelp() {
        action = EditingActions.EDIT_HELP;
    }

    public EditingActions cansel() {
        EditingActions currentAction = action;
        action = EditingActions.NONE;
        pendingGroupName = null;
        return currentAction;
    }
}
