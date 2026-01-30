package bot.core.control.callbackHandlers;

import bot.core.control.rout.classify.enums.Callbacks;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GetCourseDescriptionHandler extends AbstractCallbackHandler {
    public GetCourseDescriptionHandler() {
        super(Callbacks.getCourseDescription, 2);
    }

    @Override
    public void handle(Update update) {

    }
}
