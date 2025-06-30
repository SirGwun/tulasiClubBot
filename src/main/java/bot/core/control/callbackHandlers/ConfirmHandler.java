package bot.core.control.callbackHandlers;

import bot.core.control.SessionController;
import bot.core.control.TimerController;
import bot.core.util.ChatUtils;
import bot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ConfirmHandler implements callbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ConfirmHandler.class);

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("confirm_");
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 3) return false;
        try {
            Integer.parseInt(data[1]);
            Long.parseLong(data[2]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_");
        int originMessageId = Integer.parseInt(data[1]);
        long targetUserId = Long.parseLong(data[2]);
        long chatId = cq.getMessage().getChatId();
        int messageId = cq.getMessage().getMessageId();

        log.info("Admin {} confirm {}", chatId, targetUserId);
        Long groupId = SessionController.getInstance().getUserSession(targetUserId).getGroupId();
        if (TimerController.hasTimer(targetUserId, groupId)) {
            TimerController.stopTimer(targetUserId, groupId);
            ChatUtils.addInGroup(targetUserId, groupId, "Одобрение админа");
        } else {
            log.info("Already added by timer");
        }
        ChatUtils.deleteMessage(chatId, messageId);
        ChatUtils.deleteMessage(chatId, originMessageId);
        return true;
    }

    @Override
    public String getFormat() {
        return "confirm_<messageId>_<userId>";
    }
}
