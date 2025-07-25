package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.callbackHandlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.EnumMap;
import java.util.Map;

public class CallbackProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(CallbackProcessor.class);

    @Override
    public boolean canProcess(Update update) {
        return update.hasCallbackQuery();
    }

    @Override
    public void process(Update update) {
        handleCallbackQuery(update);
    }

    private final Map<Action, CallbackHandler> handlers = new EnumMap<>(Action.class);

    {
        CallbackHandler[] list = new CallbackHandler[]{
                new ConfirmHandler(),
                new DeclineHandler(),
                new ChooseGroupHandler(),
                new SetTagHandler(),
                new DelGroupHandler(),
                new GetJoinRequestedLinkHandler(),
                new ChooseTagHandler(),
                new GetInstructionHandler(),
                new ChooseAllCourseHandler(),
                new GetPaymentInstructionHandler(),
                new GetCourseDescriptionHandler(),
                new LeftPointerButtonHandler(),
                new RightPointerButtonHandler()
        };
        for (CallbackHandler handler : list) {
            handlers.put(handler.getAction(), handler);
        }
    }

    public void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        String data = callbackQuery.getData();
        String actionString = data.split("_", 2)[0];
        Action action = null;
        try {
            action = Action.valueOf(actionString);
        } catch (IllegalArgumentException e) {
            log.error("Unknown callback action {}", actionString);
        }
        CallbackHandler handler = handlers.get(action);
        if (handler != null && handler.match(update)) {
            if (!handler.isFormatCorrect(data)) {
                log.error("!!!Incorrect callback format \nprovided: {} \nrecuaerd: {}",
                        data,
                        handler.getFormat());
            } else {
                handler.handle(update);
            }
        } else {
            log.error("No handler for action {}", actionString);
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            Main.paymentBot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }
}
