package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.model.Session;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);


    //Когда бота добавили в группу
    @Override
    public boolean canProcess(MessageContext ctx, Session session) {
        return ctx.isFromGroup() && isBotAddedToGroup(ctx) && ctx.isFromAdmin();
    }

    public boolean canProcess(Update update) {
        return update.hasMyChatMember() && update.getMyChatMember().getFrom().getId() == Main.dataUtils.getAdminId();
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

    @Override
    public void process(MessageContext ctx, Session session) {
        processChatAddition(
                ctx.getChatId(),
                ctx.getChatName(),
                ctx.getFromId(),
                ctx.getMessage().getChat().getType()
        );
    }

    public void process(ChatMemberUpdated myChatMember) {
        processChatAddition(
                myChatMember.getChat().getId(),
                myChatMember.getChat().getTitle(),
                myChatMember.getFrom().getId(),
                myChatMember.getChat().getType()
        );
    }

    private void processChatAddition(long chatId, String chatName, Long fromId, String type) {

        String chatType = (type.equals("group") || type.equals("supergroup")) ? "группу" : "канал";

        if (isItNewChat(chatName, chatId)) {
            addBot(chatName, chatId);
            ChatUtils.sendMessage(fromId, "Вы успешно добавили бота в " + chatType + " " + chatName);
        } else { //todo определять добавили или удалили и не отправлять во втором случае
            ChatUtils.sendMessage(fromId,
                    "Вы только что добавили в бота в группу " + chatName +
                            "\n имя которой совпадает с уже существующей\n\n" +
                            "Добавление не было произведено, пожалуйста - удалите бота из этой группы, " +
                            "смените ее имя на другое и попробуйте еще раз"
            );
        }
    }

    private boolean isItNewChat(String chatName, Long chatId) {
        Long savedChatId = Main.dataUtils.getGroupMap().get(chatName);
        return savedChatId == null || savedChatId.equals(chatId);
    }

    private void addBot(String chatName, Long chatId) {
        log.info("Bot added to group: " + chatName);
        Main.dataUtils.addNewGroup(chatName, chatId);
    }
}
