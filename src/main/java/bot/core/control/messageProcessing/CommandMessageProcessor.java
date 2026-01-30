package bot.core.control.messageProcessing;

import bot.core.control.rout.classify.enums.Commands;
import bot.core.model.input.MessageContext;
import bot.core.control.SessionController;
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

    public CommandMessageProcessor() {

    }

    public CommandMessageProcessor(List<Answer> answers) {

    }

    @Override
    public boolean canProcess(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        return update.hasMessage() && message.isCommand();
    }

    @Override
    public void process(Update update) {
        MessageContext message = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .openSessionIfNeeded(update.getMessage().getFrom());
        CommandHandler handler = new CommandHandler(session.getState(), message.getFromId());
        handler.handle(message);
    }
}