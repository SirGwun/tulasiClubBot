package bot.core.control.messageProcessing;

import bot.core.control.rout.classify.enums.Commands;
import bot.core.model.input.MessageContext;
import bot.core.control.SessionService;
import bot.core.model.Session;
import bot.core.model.answers.Answer;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandMessageProcessor implements MessageProcessor {
    Map<Commands, Answer> answerMap = new HashMap<>();
    private SessionService sessionService;

    public CommandMessageProcessor() {

    }

    public CommandMessageProcessor(SessionService sessionService ,List<Answer> answers) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean canProcess(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        return update.hasMessage() && message.isCommand();
    }

    @Override
    public void process(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        Long userId = message.getFromId();

        CommandHandler handler = new CommandHandler(sessionService.getEditingActionState(userId), message.getFromId());
        handler.handle(message);
    }
}