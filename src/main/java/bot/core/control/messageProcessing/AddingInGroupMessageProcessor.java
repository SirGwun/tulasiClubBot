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
import bot.core.model.Group;

import java.util.List;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);


    //поменять
    @Override
    public boolean canProcess(MessageContext ctx, Session session) {
        return false;
    }

    public boolean canProcess(Update update) {
        return isBotAddedToGroup(update) && update.getMyChatMember().getFrom().getId() == Main.dataUtils.getAdminId();
    }

    private boolean isBotAddedToGroup(Update update) {
        if (!update.hasMyChatMember()) return false;
        User user = update.getMyChatMember().getNewChatMember().getUser();
        String status = update.getMyChatMember().getNewChatMember().getStatus();
        System.out.println(status);
        if (!user.getUserName().equalsIgnoreCase(Main.dataUtils.getBotName())) return false;
        if (status.equalsIgnoreCase("left") || status.equalsIgnoreCase("kicked")) return false;

        return true;
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
        Group group = Main.dataUtils.getGroupByName(chatName);
        Long savedChatId = group == null ? null : group.getId();
        return savedChatId == null || savedChatId.equals(chatId);
    }

    private void addBot(String chatName, Long chatId) {
        log.info("Bot added to group: " + chatName);
        Main.dataUtils.addNewGroup(chatName, chatId);
    }
}
