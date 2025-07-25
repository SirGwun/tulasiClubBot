package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.model.Group;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Приглашает в группу, запрашивает подтверждение
 */
public class ChooseGroupHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ChooseGroupHandler.class);
    private final Action action = Action.chooseGroup;

    @Override
    public String getFormat() {
        return action + "_<groupId>";
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Action.chooseGroup.toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 2) return false;
        if (!data[0].equalsIgnoreCase(Action.chooseGroup.toString())) return false;
        try {
            Long.parseLong(data[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_");
        Long groupId = Long.parseLong(data[1]);
        long userId = cq.getMessage().getChatId();
        log.info("User {} set group {}", userId, groupId);

        Group group = Main.dataUtils.getGroupById(groupId);

        if (group == null) {
            ChatUtils.sendMessage(userId, "Группа не найдена");
            log.warn("Группа не найдена {}", groupId);
            return;
        }

        if (group.isBotAdmin()) {
            if (isItFavoriteUser(userId)) {
                ChatUtils.addInGroup(userId, groupId, "Член избранной группы");
            } else {
                SessionController.getInstance().setUserGroupId(userId, groupId);
                ChatUtils.sendMessage(userId, "Выбрана группа: " + group.getName() + "\nТеперь пришлите подтверждение оплаты");
            }
        } else {
            ChatUtils.sendMessage(userId, "Бот не выходит в группу или не является в ней администратором");
        }
    }


    private boolean isItFavoriteUser(Long userId) {
        return areUserInGroup(userId, Main.dataUtils.getFavoriteGroupId());
    }

    private boolean areUserInGroup(long userId, long groupId) {
        try {
            org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember getChatMember = new org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember();
            getChatMember.setChatId(groupId);
            getChatMember.setUserId(userId);
            org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember chatMember = Main.paymentBot.execute(getChatMember);
            String status = chatMember.getStatus();
            return status.equals("member")
                    || status.equals("administrator")
                    || status.equals("creator")
                    || status.equals("restricted");
        } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
            log.warn("Не удалось получить статус пользователя в избранной группе {} ", userId);
        }
        return false;
    }
}
