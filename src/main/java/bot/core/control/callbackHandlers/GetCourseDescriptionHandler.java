package bot.core.control.callbackHandlers;

import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GetCourseDescriptionHandler extends AbstractCallbackHandler {
    public GetCourseDescriptionHandler() {
        super(Action.getCourseDescription, 2);
    }

    @Override
    public void handle(Update update) {
        ChatUtils.sendMessage(update.getCallbackQuery().getFrom().getId(),
                "Описание курсов будет добавлено вскоре");
    }
}
