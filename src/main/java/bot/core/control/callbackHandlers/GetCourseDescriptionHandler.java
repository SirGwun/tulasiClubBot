package bot.core.control.callbackHandlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public class GetCourseDescriptionHandler extends AbstractCallbackHandler {
    public GetCourseDescriptionHandler() {
        super(Action.getCourseDescription);
    }

    @Override
    public void handle(Update update) {

    }
}
