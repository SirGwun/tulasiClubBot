package bot.core.control.messageProcessing;

import bot.core.model.Session;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditHelpProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(EditInfoProcessor.class);

    @Override
    public boolean canProcess(MessageContext message, Session session) {
        return session.getState().isEditingHelp() && message.isFromAdmin() && !message.getText().equals("/cancel");
    }

    @Override
    public void process(MessageContext message, Session session) {
        Main.dataUtils.setHelp(message.getText());
        session.getState().cansel();
        ChatUtils.sendMessage(message.getChatId(), "Инструкция для пользователей изменена");
    }
}
