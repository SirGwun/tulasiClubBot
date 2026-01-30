package bot.core.control.callbackHandlers;

import bot.core.Legacy;
import bot.core.control.rout.classify.enums.Callbacks;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public class DelGroupHandler implements CallbackHandler {
    private final Callbacks action = Callbacks.delGroup;
    @Override
    public String getFormat() {
        return action + "_<groupId>";
    }

    @Override
    public Callbacks getAction() {
        return action;
    }

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Callbacks.delGroup.toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 2) return false;
        if (!data[0].equalsIgnoreCase(Callbacks.delGroup.toString())) return false;
        try {
            Long.parseLong(data[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_");
        long groupId = Long.parseLong(data[1]);
        long userId = cq.getMessage().getChatId();
        int messageId = cq.getMessage().getMessageId();

        if (Legacy.dataUtils.containsGroupId(groupId)) {
            Legacy.dataUtils.removeGroup(groupId);
            ChatUtils.sendMessage(userId, "Группа удалена");
            ChatUtils.deleteMessage(userId, messageId);
        } else {
            ChatUtils.sendMessage(userId, "Группа не найдена");
        }
    }
}
