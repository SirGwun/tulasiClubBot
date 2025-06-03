package bot.core.control;

import bot.core.Main;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class EditingSessionState {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public String pendingGroupName = null;
    private boolean waitingGroupName = false;
    private boolean editingInfo = false;
    private boolean editingHelp = false;



    public boolean isEditingInfo(Message message) {
        return editingInfo && message.getChatId() == DataUtils.getAdminID();
    }

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
        return waitingGroupName;
    }

    public void setWaitingGroupName(boolean waitingGroupName) {
        this.waitingGroupName = waitingGroupName;
    }

    public boolean isEditingInfo() {
        return editingInfo;
    }

    public void setEditingInfo(boolean editingInfo) {
        this.editingInfo = editingInfo;
    }

    public boolean isEditingHelp() {
        return editingHelp;
    }

    public void setEditingHelp(boolean editingHelp) {
        this.editingHelp = editingHelp;
    }
}
