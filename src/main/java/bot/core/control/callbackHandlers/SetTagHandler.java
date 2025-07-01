package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import bot.core.control.callbackHandlers.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class SetTagHandler implements CallbackHandler {
    @Override
    public String getFormat() {
        return "setTag_<tagId>";
    }

    private static final Logger log = LoggerFactory.getLogger(SetTagHandler.class);

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Action.setTag + "_");
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 2) return false;
        if (!data[0].equalsIgnoreCase(Action.setTag.toString())) return false;
        try {
            Integer.parseInt(data[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_");
        String tagId = data[1];
        long userId = cq.getMessage().getChatId();
        int messageId = cq.getMessage().getMessageId();

        Map<Integer, String> tags = Main.dataUtils.getTagMap();
        String tag = tags.get(Integer.parseInt(tagId));
        if (tag == null) {
            log.error("Попытка прочитать несуществующий тег {} ", tagId);
            return;
        }
        log.info("User {} set tag {}", userId, tag);
        Main.dataUtils.setGroupTag(tag);
        ChatUtils.deleteMessage(userId, messageId);
        ChatUtils.sendMessage(userId,
                "Тег успешно выбран - " + tag);
    }
}
