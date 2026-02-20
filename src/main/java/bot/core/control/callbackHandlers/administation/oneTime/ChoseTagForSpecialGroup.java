package bot.core.control.callbackHandlers.administation.oneTime;

import bot.core.Main;
import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import bot.core.control.callbackHandlers.Action;
import bot.core.model.SpecialGroup;
import bot.core.model.Tag;
import bot.core.repos.GroupRepository;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ChoseTagForSpecialGroup extends AbstractCallbackHandler {
    Logger logger = LoggerFactory.getLogger(ChoseTagForSpecialGroup.class);

    private final long id;
    private final String name;

    public ChoseTagForSpecialGroup(long specialGroupId, String specialGroupName) {
        super(Action.specialGroupTagChoose);
        id = specialGroupId;
        name = specialGroupName;
    }

    @Override
    public void handle(Update update) {
        Main.paymentBot.deleteOneTimeHandler(getAction());

        long fromId = update.getCallbackQuery().getFrom().getId();

        String queryData = update.getCallbackQuery().getData();
        String[] splitData = queryData.split("_");

        GroupRepository repository = new GroupRepository();

        if (splitData[1] == null || splitData[1].isEmpty()) {
            error(fromId);
            return;
        }

        Tag tag = repository.findTagById(Integer.parseInt(splitData[1])).orElse(null);

        if (tag == null) {
            error(fromId);
            return;
        }

        if (repository.saveSpecialGroup(new SpecialGroup(id, name, tag.getName()))) {
            ChatUtils.sendMessage(fromId, "Группа " + name + " успешно добавлена как избранная группа для тега " + tag.getName());
        } else {
            ChatUtils.sendMessage(fromId, "Произошла ошибка при записи в базу данных");
        }

        ChatUtils.deleteMessage(update.getCallbackQuery().getFrom().getId(),
                update.getCallbackQuery().getMessage().getMessageId());
    }

    private void error(long userId) {
        logger.error("Не верная колебк дата, нет такого тега");
        ChatUtils.sendMessage(userId, "Нет такого тега");
    }
}
