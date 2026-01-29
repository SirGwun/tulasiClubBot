package bot.core.control.messageProcessing;

import bot.core.Legacy;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import bot.core.model.Group;

import java.util.Objects;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);

    @Override
    public boolean canProcess(Update update) {
        return isBotAddedToGroup(update) && update.getMyChatMember().getFrom().getId() == Legacy.dataUtils.getAdminId();
    }

    private boolean isBotAddedToGroup(Update update) {
        if (!update.hasMyChatMember()) return false;
        User user = update.getMyChatMember().getNewChatMember().getUser();
        return user.getUserName().equalsIgnoreCase(Legacy.paymentBot.getBotUsername());
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
        String chatType = ("group".equals(type) || "supergroup".equals(type)) ? "группы" : "канала";

        if (Legacy.dataUtils.getGroupById(chatId) != null) {           // чат известен → удаляем
            log.info("Bot left from {}", chatName);
            Legacy.dataUtils.removeGroup(chatId);
            ChatUtils.sendMessage(fromId, "Бот был удален из " + chatType + " " + chatName);
        } else {                                                     // чат не найден
            log.info("Попытка удалить несуществующую группу: {}", chatName);
        }
    }

    private void processChatAddition(long chatId, String chatName, Long fromId, String type) {
        String chatType = ("group".equals(type) || "supergroup".equals(type)) ? "группу" : "канал";

        Group byId   = Legacy.dataUtils.getGroupById(chatId);
        Group byName = Legacy.dataUtils.getGroupByName(chatName);

        if (byId == null && byName == null) {                        // полностью новый чат
            log.info("Bot added to {}", chatName);
            Legacy.dataUtils.addNewGroup(chatName, chatId);
            ChatUtils.sendMessage(fromId, "Вы успешно добавили бота в " + chatType + " " + chatName);
            return;
        }

        if (byId != null && !Objects.equals(byId.getName(), chatName)) {  // id тот же, имя изменилось
            byId.setName(chatName);
            Legacy.dataUtils.saveGroupList();
            return;
        }

        if (byId == null) {                        // имя занято другим id
            ChatUtils.sendMessage(fromId,
                    "Группа с именем «" + chatName + "» уже зарегистрирована.\n" +
                            "Переименуйте её или удалите добавленную (/del) и попробуйте ещё раз.");
            return;
        }

        log.warn("Добавление уже существующей группы {}", byId);     // id и имя совпали
    }
}
