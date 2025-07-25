package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.model.TimerController;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 */
public class ConfirmHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ConfirmHandler.class);
    private final Action action = Action.confirm;

    @Override
    public String getFormat() {
        return action + "_<messageId>_<userId>";
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Action.confirm + "_");
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 3) return false;
        if (!data[0].equalsIgnoreCase(Action.confirm.toString())) return false;
        try {
            Integer.parseInt(data[1]);
            Long.parseLong(data[2]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_");
        int originMessageId = Integer.parseInt(data[1]);
        long targetUserId = Long.parseLong(data[2]);
        long chatId = cq.getMessage().getChatId();
        int messageId = cq.getMessage().getMessageId();

        log.info("Admin {} confirm {}", chatId, targetUserId);
        Long groupId = SessionController.getInstance().getUserSession(targetUserId).getGroupId();
        if (TimerController.hasTimer(targetUserId, groupId) || !isUserAdded(targetUserId, groupId)) {
            TimerController.stopTimer(targetUserId, groupId);
            ChatUtils.addInGroup(targetUserId, groupId, "Одобрение админа");
        } else {
            log.info("Already added by timer");
        }
        ChatUtils.deleteMessage(chatId, messageId);
        ChatUtils.deleteMessage(chatId, originMessageId);
    }

    private boolean isUserAdded(long userId, Long groupId) {
        GetChatMember getChatMember = new GetChatMember(String.valueOf(groupId), userId);
        try {
            ChatMember chatMember = Main.paymentBot.execute(getChatMember);
            return chatMember != null;
        } catch (TelegramApiException e) {
            log.warn("User not added");
            return false;
        }
    }
}
