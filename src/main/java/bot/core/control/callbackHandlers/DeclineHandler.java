package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.control.TimerController;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.Instant;

public class DeclineHandler implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(DeclineHandler.class);
    private final Action action = Action.decline;

    @Override
    public String getFormat() {
        return action + "_<messageId>_<userId>";
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public boolean match(Update update) {
        return update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith(Action.decline.toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 3) return false;
        if (!data[0].equalsIgnoreCase(Action.decline.toString())) return false;
        try {
            Integer.parseInt(data[1]);
            Long.parseLong(data[2]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void handle(Update update) {
        CallbackQuery cq = update.getCallbackQuery();
        String[] data = cq.getData().split("_");
        int originMessageId = Integer.parseInt(data[1]);
        long targetUserId = Long.parseLong(data[2]);
        long chatId = cq.getMessage().getChatId();
        int messageId = cq.getMessage().getMessageId();

        ChatUtils.deleteMessage(chatId, messageId);
        ChatUtils.deleteMessage(chatId, originMessageId);
        handleDeclineAction(targetUserId);
    }

    private void handleDeclineAction(long targetUserId) {
        long groupId = SessionController.getInstance().getUserSession(targetUserId).getGroupId();
        String userName = SessionController.getInstance()
                .getUserSession(targetUserId)
                .getUserName();
        String groupName = Main.dataUtils.getGroupName(groupId);

        try {
            if (TimerController.hasTimer(targetUserId, groupId)) {
                ChatUtils.sendMessage(targetUserId, "Ваша заявка была отклонена, \n" +
                        "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
                ChatUtils.sendMessage(Long.parseLong(Main.dataUtils.getHistoryId()),
                        "Заявка пользователя @" +
                                userName +
                                " была отклонена, \n" +
                                "Он хотел попасть в группу " + groupName);
                TimerController.stopTimer(targetUserId, groupId);
            } else {
                if (areUserInGroup(targetUserId, groupId)) {
                    BanChatMember ban = new BanChatMember();
                    ban.setChatId(String.valueOf(groupId));
                    ban.setUserId(targetUserId);
                    ban.setUntilDate((int) Instant.now().getEpochSecond() + 60);
                    Main.bot.execute(ban);

                    UnbanChatMember unban = new UnbanChatMember();
                    unban.setChatId(String.valueOf(groupId));
                    unban.setUserId(targetUserId);
                    Main.bot.execute(unban);
                    log.info("User {} was kicked from group {}", userName, groupId);

                    ChatUtils.sendMessage(Long.parseLong(Main.dataUtils.getHistoryId()),
                            "Пользователь @" + userName + " был удален из группы " + groupName);

                    ChatUtils.sendMessage(targetUserId, "Вы были удалены из группы " + groupName + "\n" +
                            "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
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

    private boolean areUserInGroup(long userId, long groupId) {
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
}
