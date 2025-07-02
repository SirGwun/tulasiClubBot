package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import bot.core.model.Group;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);

    @Override
    public boolean canProcess(Update update) {
        return isBotAddedToGroup(update) && update.getMyChatMember().getFrom().getId() == Main.dataUtils.getAdminId();
    }

    private boolean isBotAddedToGroup(Update update) {
        if (!update.hasMyChatMember()) return false;
        User user = update.getMyChatMember().getNewChatMember().getUser();
        return user.getUserName().equalsIgnoreCase(Main.dataUtils.getBotName());
    }

    @Override
    public void process(Update update) {
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        String status = myChatMember.getNewChatMember().getStatus();
        if (status.equalsIgnoreCase("left")
                || status.equalsIgnoreCase("kicked"))
            processChatLeft(
                    myChatMember.getChat().getId(),
                    myChatMember.getChat().getTitle(),
                    myChatMember.getFrom().getId(),
                    myChatMember.getChat().getType()
            );
        else if (status.equalsIgnoreCase("administrator")
                || status.equalsIgnoreCase("creator")) {
            processChatAddition(
                    myChatMember.getChat().getId(),
                    myChatMember.getChat().getTitle(),
                    myChatMember.getFrom().getId(),
                    myChatMember.getChat().getType()
            );
        }
    }

    private void processChatLeft(long chatId, String chatName, Long fromId, String type) {
        String chatType = (type.equals("group") || type.equals("supergroup")) ? "группы" : "канала";

        if (!isItNewChat(chatName, chatId)) {
            log.info("Bot left from group: " + chatName);
            Main.dataUtils.removeGroup(chatId);
            ChatUtils.sendMessage(fromId, "Бот был удален из " + chatType + " " + chatName);
        } else {
            log.info("Попытка удалить несуществующую группу: " + chatName);
        }
    }

    private void processChatAddition(long chatId, String chatName, Long fromId, String type) {

        String chatType = (type.equals("group") || type.equals("supergroup")) ? "группу" : "канал";

        if (isItNewChat(chatName, chatId)) {
            log.info("Bot added to group: " + chatName);
            Main.dataUtils.addNewGroup(chatName, chatId);
            ChatUtils.sendMessage(fromId, "Вы успешно добавили бота в " + chatType + " " + chatName);
        } else {
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
}
