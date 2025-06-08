package bot.core.model.messageProcessing;

import bot.core.control.Session;
import bot.core.model.Group;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.util.GroupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.User;

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

        String botUsername = DataUtils.getBotName();

        for (User user : newMembers) {
            if (Boolean.TRUE.equals(user.getIsBot()) && botUsername.equalsIgnoreCase(user.getUserName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void process(MessageContext ctx, Session session) {
        long chatId = ctx.getChatId();

        String groupName = ctx.getChatName();
        log.info("Bot added to group: " + groupName);

        DataUtils.addNewGroup(ctx.getChatName(), chatId);
    }
}
