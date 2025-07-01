package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import bot.core.control.callbackHandlers.Action;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public class DelGroupHandler implements CallbackHandler {
    @Override
    public String getFormat() {
        return "delGroup_<groupId>";
    }

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Action.delGroup.toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 2) return false;
        if (!data[0].equalsIgnoreCase(Action.delGroup.toString())) return false;
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
        Long groupId = Long.parseLong(data[1]);
        long userId = cq.getMessage().getChatId();
        int messageId = cq.getMessage().getMessageId();

        if (Main.dataUtils.containsGroupId(groupId)) {
            Main.dataUtils.removeGroup(groupId);
            ChatUtils.sendMessage(userId, "Группа удалена");
            ChatUtils.deleteMessage(userId, messageId);
        } else {
            ChatUtils.sendMessage(userId, "Группа не найдена");
        }
    }
}
