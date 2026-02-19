package bot.core.control.callbackHandlers.groupNavigation;

import bot.core.control.callbackHandlers.Action;
import bot.core.control.callbackHandlers.CallbackHandler;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GetJoinRequestedLinkHandler implements CallbackHandler {
    private final Action action = Action.getJoinRequestedLink;
    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Action.getJoinRequestedLink + "_");
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        return data.length >= 3 && data[0].equalsIgnoreCase(Action.getJoinRequestedLink.toString()); // link may contain underscores
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_", 3);
        String link = data[1];
        long chatId = Long.parseLong(data[2]);
        ChatUtils.sendMessage(chatId,
                "Если вы уже вступили в группу, но не можете её найти — воспользуйтесь ссылкой ниже.\n\n" +
                        "⚠️ Внимание: если вы ещё не вступали в группу, сначала перейдите по одноразовой ссылке выше.\n" +
                        link);
    }

    @Override
    public String getFormat() {
        return action + "_<link>_<chatId>";
    }

    @Override
    public Action getAction() {
        return action;
    }
}
