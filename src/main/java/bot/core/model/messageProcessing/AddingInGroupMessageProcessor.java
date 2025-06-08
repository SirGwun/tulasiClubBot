package bot.core.model.messageProcessing;

import bot.core.Main;
import bot.core.control.Session;
import bot.core.model.Group;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);


    //Когда бота добавили в группу
    @Override
    public boolean canProcess(MessageContext ctx, Session session) {
        return ctx.isFromGroup() && isBotAddedToGroup(ctx) && ctx.isFromAdmin();
    }

    private boolean isBotAddedToGroup(MessageContext ctx) {
        List<User> newMembers = ctx.getMessage().getNewChatMembers();
        if (newMembers == null || newMembers.isEmpty()) return false;

        String botUsername = Main.dataUtils.getBotName();

        for (User user : newMembers) {
            if (Boolean.TRUE.equals(user.getIsBot()) && botUsername.equalsIgnoreCase(user.getUserName())) {
                return true;
            }
        }
        return false;
    }

    //todo Изменить способ хранения и валидации групп чтобы можно было хранить имена групп с любыми символами
    @Override
    public void process(MessageContext ctx, Session session) {
        long chatId = ctx.getChatId();

        String groupName = ctx.getChatName();
        log.info("Bot added to group: " + groupName);

        Main.dataUtils.addNewGroup(ctx.getChatName(), chatId);
    }
}
