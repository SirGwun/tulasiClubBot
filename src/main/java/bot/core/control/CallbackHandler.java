package bot.core.control;

import bot.core.Main;
import bot.core.model.SessionController;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(CallbackHandler.class);
    //Map(userId, groupID>
    Map<Long, Long> groupMap = new HashMap<>();

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String[] data = callbackQuery.getData().split("_");
        String action = data[0];
        long userClickedButtonId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        switch (action) {
            case "confirm": {
                int originMessageId = Integer.parseInt(data[1]);
                long targetUserId = Long.parseLong(data[2]);
                handleConfirmAction(targetUserId, userClickedButtonId);

                ChatUtils.deleteMessage(userClickedButtonId, messageId);
                ChatUtils.deleteMessage(userClickedButtonId, originMessageId);
                break;
            }
            case "decline": {
                int originMessageId = Integer.parseInt(data[1]);
                ChatUtils.deleteMessage(userClickedButtonId, messageId);
                ChatUtils.deleteMessage(userClickedButtonId, originMessageId);

                long targetUserId = Long.parseLong(data[2]);
                handleDeclineAction(targetUserId);
                break;
            }
            case "setGroup": {
                Long groupId = Long.parseLong(data[1]);
                handleSetGroupAction(groupId, userClickedButtonId, messageId);
                break;
            }
            case "delGroup": {
                Long groupId = Long.parseLong(data[1]);
                handleDelGroupAction(userClickedButtonId, messageId, groupId);
                break;
            }
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            Main.bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }



    private void handleDelGroupAction(long userId, int messageId, Long groupId) {
        if (Main.dataUtils.getGroupMap().containsValue(groupId)) {
            Main.dataUtils.removeGroup(groupId);
            ChatUtils.sendMessage(userId, "Группа удалена");
            ChatUtils.deleteMessage(userId, messageId);
        } else {
            ChatUtils.sendMessage(userId, "Группа не найдена");
        }
    }



    private void handleDeclineAction(long targetUserId) {
        log.info("decline {}", targetUserId);

        ChatUtils.sendMessage(targetUserId, "Ваша заявка была отклонена, \n" +
                "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
    }


    private void handleSetGroupAction(Long selectedGroupId, long selectingUserId, int messageId) {
        log.info("User {} set group {}", selectingUserId, selectedGroupId);

        if (!Main.dataUtils.getGroupMap().containsValue(selectedGroupId)) {
            ChatUtils.sendMessage(selectingUserId, "Группа не найдена");
            return;
        }
        String groupName = Main.dataUtils.getGroupName(selectedGroupId);

        if (ChatUtils.isBotAdminInGroup(selectedGroupId)) {
            if (Helper.isItFavoriteUser(selectingUserId)) {
                ChatUtils.addInGroup(selectingUserId, selectedGroupId);
            } else {
                SessionController.getInstance().setUserGroupId(selectingUserId, selectedGroupId);
                ChatUtils.sendMessage(selectingUserId, "Выбрана группа: " + groupName + "\nТеперь пришлите подтверждение оплаты");
            }
        } else {
            ChatUtils.sendMessage(selectingUserId, "Бот не выходит в группу или не является в ней администратором");
        }
    }

    private void handleConfirmAction(long targetUserId, long userCLickedButtonId) {
        log.info("Admin {} confirm {}", userCLickedButtonId, targetUserId);

        Long groupId = SessionController.getInstance().getUserSession(targetUserId).getGroupId();

        if (groupId == null)
            groupId = Main.dataUtils.getDefaulfGroup();

        ChatUtils.addInGroup(targetUserId, groupId);
    }
    private static class Helper {
        private static boolean isItFavoriteUser(Long userId) {
            try {
                GetChatMember getChatMember  = new GetChatMember();
                getChatMember.setChatId(Main.dataUtils.getFavoriteGroupId());
                getChatMember.setUserId(userId);
                ChatMember chatMember = Main.bot.execute(getChatMember);
                String status = chatMember.getStatus();
                return status.equals("member")
                        || status.equals("administrator")
                        || status.equals("creator")
                        || status.equals("restricted");
            } catch (TelegramApiException e) {
                log.warn("Не удалось получить статус пользователя в избранной группе {} ", userId);
            }
            return false;
        }
    }
}
