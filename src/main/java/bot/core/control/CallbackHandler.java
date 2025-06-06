package bot.core.control;

import bot.core.Main;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.util.GroupUtils;
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
        long userID = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        switch (action) {
            case "confirm":
                handleConfirmInGroupAction(callbackQuery, data, userID, messageId);
                break;
            case "decline":
                handleDeclineAction(callbackQuery, data, userID, messageId);
                break;
            case "setGroup":
                handleSetGroupAction(callbackQuery, data, userID, messageId);
                break;
            case "confirmAdmin":
                handleConfirmAdminAction(callbackQuery, data, userID);
                break;
            case "delGroup":
                handleDelGroupAction(callbackQuery, data, userID);
        }
    }



    private void handleDelGroupAction(CallbackQuery callbackQuery, String[] data, long userID) {
        String groupId = data[1];
        if (DataUtils.getGroupList().containsValue(groupId)) {
            DataUtils.removeGroup(groupId);
            ChatUtils.sendMessage(userID, "Группа удалена");
            ChatUtils.deleteMessage(userID, callbackQuery.getMessage().getMessageId());
        } else {
            ChatUtils.sendMessage(userID, "Группа не найдена");
        }
    }


    private void handleConfirmInGroupAction(CallbackQuery callbackQuery, String[] data, long userId, int messageId) {

        log.info("User {} confirm {}", userId, data[2]);


        if (groupMap.containsKey(userId)) {
            GroupUtils.addInGroup(Long.parseLong(data[2]), groupMap.get(userId));
            groupMap.remove(userId);
        } else {
            GroupUtils.addInGroup(DataUtils.getMainGroupID(), groupMap.get(userId));
        }


        ChatUtils.deleteMessage(userId, messageId);
        ChatUtils.deleteMessage(userId, Integer.parseInt(data[1]));

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            Main.bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void handleDeclineAction(CallbackQuery callbackQuery, String[] data, long userId, int messageId) {
        log.info("User {} decline {}", userId, data[2]);
        decline(Long.parseLong(data[2]));
        ChatUtils.deleteMessage(userId, messageId);
        ChatUtils.deleteMessage(userId, Integer.parseInt(data[1]));

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            Main.bot.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }
    //todo !!!не защищено от паралельного выполнения, очень опасно!
    private void handleSetGroupAction(CallbackQuery callbackQuery, String[] data, long userID, int messageId) {
        log.info("User {} set group {}", userID, data[1]);
        Properties groupList = DataUtils.getGroupList();
        if (!groupList.containsValue(data[1])) {
            ChatUtils.sendMessage(userID, "Группа не найдена");
            return;
        }
        String groupID = data[1];
        String groupName = "";
        Set<Map.Entry<Object, Object>> entries = groupList.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            if (entry.getValue().equals(data[1])) {
                groupName = entry.getKey().toString();
                break;
            }
        }

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

        if (GroupUtils.isBotAdminInGroup(groupID)) {
            if (userID == DataUtils.getAdminID()) {
                DataUtils.updateConfig("groupID", groupID);
                ChatUtils.deleteMessage(userID, messageId);
                ChatUtils.sendMessage(userID, "Группа выбрана " + groupName.replaceAll("-", " "));
            } else {
                groupMap.put(userID, groupID);
                ChatUtils.sendMessage(userID, "Выбрана группа: " + groupName.replaceAll("-", " ") + "\nТеперь пришлите подтверждение оплаты");
            }
        } else {
            ChatUtils.sendMessage(userID, "Бот не выходит в группу или не являеться в ней администратором");
        }

        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void handleConfirmAdminAction(CallbackQuery callbackQuery, String[] data, long userID) {
        log.info("User {} confirm admin {}", userID, data[1]);

        // Обработка запроса
        String groupId = data[1];
        if (GroupUtils.isBotAdminInGroup(groupId)) {
            if (newGroupName == null) {
                ChatUtils.sendMessage(userID, "Имя группы пусто");
                log.error("Имя группы пусто");
            } else if (DataUtils.addNewGroup(newGroupName, Long.parseLong(groupId))) {
                ChatUtils.sendMessage(userID, "Группа добавлена");
                newGroupName = null;
                newGroup = false;
            } else {
                ChatUtils.sendMessage(userID, "Не удалось добавить группу");
                log.error("Не удалось добавить группу {}", groupId);
            }
        } else {
            ChatUtils.sendMessage(DataUtils.getAdminID(), "Бот не являеться администратором в группе " + newGroupName);
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }
}
