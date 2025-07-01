package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.callbackHandlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

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

    private final List<CallbackHandler> handlers = Arrays.asList(
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
            new GetCourseDescriptionHandler()
    );

    public void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        for (CallbackHandler handler : handlers) {
            if (handler.match(update)) {
                if (!handler.isFormatCorrect(callbackQuery.getData())) {
                    log.error("!!!Incorrect callback format \nprovided: {} \nrecuaerd: {}",
                            callbackQuery.getData(),
                            handler.getFormat());
                } else {
                    handler.handle(update);
                }
                break;
            }
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            Main.bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }
}
