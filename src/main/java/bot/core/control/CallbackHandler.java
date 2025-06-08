package bot.core.control;

import bot.core.Main;
import bot.core.util.ChatUtils;
import bot.core.Main;
import bot.core.util.GroupUtils;
import bot.core.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(CallbackHandler.class);
    Map<Long, String> groupMap = new HashMap<>();

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
            case "confirmAdmin": {
                String groupId = data[1];
                handleConfirmAdminAction(groupId, userClickedButtonId);
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
                String groupId = data[1];
                handleSetGroupAction(groupId, userClickedButtonId, messageId);
                break;
            }
            case "delGroup": {
                String groupId = data[1];
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



    private void handleDelGroupAction(long userID, int messageId, String groupId) {
        if (Main.dataUtils.getGroupList().containsValue(groupId)) {
            Main.dataUtils.removeGroup(groupId);
            ChatUtils.sendMessage(userID, "Группа удалена");
            ChatUtils.deleteMessage(userID, messageId);
        } else {
            ChatUtils.sendMessage(userID, "Группа не найдена");
        }
    }



    private void handleDeclineAction(long targetUserId) {
        log.info("decline {}", targetUserId);

        ChatUtils.sendMessage(targetUserId, "Ваша заявка была отклонена, \n" +
                "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
    }


    private void handleSetGroupAction(String selectedGroupId, long selectingUserId, int callbackMessageId) {
        log.info("User {} set group {}", selectingUserId, selectedGroupId);
        Properties groupList = Main.dataUtils.getGroupList();
        if (!groupList.containsValue(selectedGroupId)) {
            ChatUtils.sendMessage(selectingUserId, "Группа не найдена");
            return;
        }
        String groupID = selectedGroupId;
        String groupName = "";
        Set<Map.Entry<Object, Object>> entries = groupList.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            if (entry.getValue().equals(selectedGroupId)) {
                groupName = entry.getKey().toString();
                break;
            }
        }

        if (GroupUtils.isBotAdminInGroup(groupID)) {
            if (selectingUserId == Main.dataUtils.getAdminID()) {
                Main.dataUtils.updateConfig("groupID", groupID);
                ChatUtils.deleteMessage(selectingUserId, callbackMessageId);
                ChatUtils.sendMessage(selectingUserId, "Группа выбрана " + groupName.replaceAll("-", " "));
            } else {
                groupMap.put(selectingUserId, groupID);
                ChatUtils.sendMessage(selectingUserId, "Выбрана группа: " + groupName.replaceAll("-", " ") + "\nТеперь пришлите подтверждение оплаты");
            }
        } else {
            ChatUtils.sendMessage(selectingUserId, "Бот не выходит в группу или не являеться в ней администратором");
        }
    }
    private void handleConfirmAction(long targetUserId, long userCLickedButtonId) {
        log.info("Admin {} confirm {}", userCLickedButtonId, targetUserId);
        String groupId = Main.getSessionByUser().get(targetUserId).getGroupId();

        GroupUtils.addInGroup(targetUserId, groupId);
    }

    private void handleConfirmAdminAction(String groupId, long userId) {
        log.info("User {} confirm that bot is admin {}", userId, groupId);
        SessionState state = Main.getSessionByUser().get(userId).getState();
        String newGroupName = Main.getSessionByUser().get(userId).getState().getPendingGroupName();

        // Обработка запроса
        if (GroupUtils.isBotAdminInGroup(groupId)) {
            if (newGroupName == null) {
                ChatUtils.sendMessage(userId, "Имя группы пусто");
                log.error("Имя группы пусто");
            } else if (Main.dataUtils.addNewGroup(newGroupName, Long.parseLong(groupId))) {
                ChatUtils.sendMessage(userId, "Группа добавлена");
                state.cansel();
            } else {
                ChatUtils.sendMessage(userId, "Не удалось добавить группу");
                log.error("Не удалось добавить группу {}", groupId);
            }
        } else {
            ChatUtils.sendMessage(Main.dataUtils.getAdminID(), "Бот не являеться администратором в группе " + newGroupName);
        }
    }
}
