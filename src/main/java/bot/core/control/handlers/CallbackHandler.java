package bot.core.control.handlers;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.control.TimerController;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.Instant;
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
            case "getJoinRequestedLink": {
                String link = data[1];
                long chatId = Long.parseLong(data[2]);
                ChatUtils.sendMessage(chatId,
                        "Если вы уже вступили в группу, но не можете её найти — воспользуйтесь ссылкой ниже.\n\n" +
                                "⚠️ Внимание: если вы ещё не вступали в группу, сначала перейдите по одноразовой ссылке выше.\n" +
                                link);
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
        long groupId = SessionController.getInstance().getUserSession(targetUserId).getGroupId();
        String userName = SessionController.getInstance()
                .getUserSession(targetUserId)
                .getUserName();
        String groupName = Main.dataUtils.getGroupName(groupId);

        TimerController.stopTimer(targetUserId, groupId);
        try {
            if (TimerController.hasTimer(targetUserId, groupId)) {
                ChatUtils.sendMessage(targetUserId, "Ваша заявка была отклонена, \n" +
                        "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
            } else {
                if (Helper.areUserInGroup(targetUserId, groupId)) {
                    BanChatMember ban = new BanChatMember();
                    ban.setChatId(String.valueOf(groupId));
                    ban.setUserId(targetUserId);
                    ban.setUntilDate((int) Instant.now().getEpochSecond() + 60); // 60 сек достаточно, чтобы Telegram «забыл» участника
                    Main.bot.execute(ban);

                    UnbanChatMember unban = new UnbanChatMember(); // сразу разбаниваем, чтобы человек мог пере-войти в будущем
                    unban.setChatId(String.valueOf(groupId));
                    unban.setUserId(targetUserId);
                    Main.bot.execute(unban);
                    log.info("User {} was kicked from group {}", userName, groupId);

                    ChatUtils.sendMessage(Long.parseLong(Main.dataUtils.getHistoryId()),
                            "Пользователь @" + userName + " был удален из группы " + groupName);
                } else {
                    log.info("User {} already isn’t a member of group {}", targetUserId, groupId);
                }
            }
        } catch (TelegramApiRequestException e) {
            log.error("UnbanChatMember Bad Request: method is available for supergroup and channel chats only");
        } catch (TelegramApiException e) {
            log.error("Can’t remove user {} from group {}", targetUserId, groupId);
        }
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
                ChatUtils.addInGroup(selectingUserId, selectedGroupId, "Член избранной группы");
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
        if (TimerController.hasTimer(targetUserId, groupId)) {
            TimerController.stopTimer(targetUserId, groupId);
            ChatUtils.addInGroup(targetUserId, groupId, "Одобрение админа");
        }
    }

    private static class Helper {
        private static boolean areUserInGroup(long userId, long groupId) {
            try {
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(groupId);
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

        private static boolean isItFavoriteUser(Long userId) {
            return areUserInGroup(userId, Main.dataUtils.getFavoriteGroupId());
        }
    }
}
