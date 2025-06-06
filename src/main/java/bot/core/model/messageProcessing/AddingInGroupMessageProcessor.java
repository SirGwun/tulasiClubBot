package bot.core.model.messageProcessing;

import bot.core.Main;
import bot.core.control.Session;
import bot.core.model.Group;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);


    //Когда бота добавили в группу
    @Override
    public boolean canProcess(MessageContext ctx, Session session) {
        if (session.getState().pendingGroupName == null) return false;

        return ctx.isFromGroup() && isBotAddedToGroup(ctx);
    }

    private boolean isBotAddedToGroup(MessageContext ctx) {
        try {
            Long botId = Main.bot.getMe().getId();
            for (User user : ctx.getMessage().getNewChatMembers()) {
                if (user.getId().equals(botId)) {
                    return true;
                }
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return false;
    }


    @Override
    public void process(MessageContext ctx, Session session) {
        long chatId = ctx.getChatId();
        log.info("Bot added to new group");

        Map<Object, Object> groups = DataUtils.getGroupList();
        String chatIdStr = Long.toString(chatId);

        if (groups.containsValue(chatIdStr)) {
            String name = "неизвестно";
            for (Map.Entry<Object, Object> entry : groups.entrySet()) {
                if (chatIdStr.equals(entry.getValue())) {
                    name = entry.getKey().toString();
                    break;
                }
            }

            ChatUtils.sendMessage(
                    chatId,
                    "Группа уже есть в списке. Имя: " + name +
                            "\nПожалуйста, просто используйте уже добавленный чат с помощью команды /set_group"
            );

            session.getState().cansel();
            return;
        }

        ChatUtils.sendInlineKeyboard(
                chatId,
                "Дайте боту права администратора в " + session.getState().pendingGroupName + ".\nПосле нажмите кнопку подтверждения",
                ChatUtils.getConfirmAdminStatusKeyboard(new Group(session.getState().pendingGroupName, chatId))
        );
    }
}
