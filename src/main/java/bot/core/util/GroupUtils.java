package bot.core.util;

import bot.core.Main;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.List;

public class GroupUtils {
    public static CreateChatInviteLink createInviteLink(long groupID) {
        CreateChatInviteLink inviteLink = new CreateChatInviteLink();
        inviteLink.setChatId(groupID);
        inviteLink.setName("Добро пожаловать на курс!");

        long nowSeconds = Instant.now().getEpochSecond();
        long expireSeconds = nowSeconds + (24 * 60 * 60);
        if (expireSeconds > Integer.MAX_VALUE || expireSeconds < Integer.MIN_VALUE) {
            throw new ArithmeticException("Unix timestamp выходит за пределы диапазона Integer");
        }
        Integer expireDate = (int) expireSeconds;
        inviteLink.setExpireDate(expireDate);
        inviteLink.setMemberLimit(1);

        return inviteLink;
    }

    public static String getUserName(long userId, long groupId) throws TelegramApiException {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(String.valueOf(groupId));
        getChatMember.setUserId(userId);

        ChatMember chatMember = Main.bot.execute(getChatMember);
        return chatMember.getUser().getUserName();
    }

    public static String getGroupName(long groupId) throws TelegramApiException {
        GetChat getChat = new GetChat();
        getChat.setChatId(String.valueOf(groupId));

        Chat chat = Main.bot.execute(getChat);
        return chat.getTitle();
    }

    public static boolean isBotAdminInGroup(String groupId) {
        try {
            // Проверяем, что группа существует, получив количество участников
            GetChatMemberCount getChatMemberCount = new GetChatMemberCount(groupId);
            int memberCount = Main.bot.execute(getChatMemberCount);
            if (memberCount > 0) {
                // Получаем список администраторов группы
                GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
                getChatAdministrators.setChatId(groupId);
                List<ChatMember> admins = Main.bot.execute(getChatAdministrators);

                // Проверяем, есть ли бот среди администраторов
                String botUsername = Main.bot.getBotUsername();
                for (ChatMember admin : admins) {
                    if (admin.getUser().getUserName().equals(botUsername)) {
                        return true; // Бот является администратором в группе
                    }
                }
            }
        } catch (TelegramApiException e) {
            Main.log.info("Попытка добавить бота в несуществующую группу {}", e.getMessage());
        }
        return false; // Группа не существует или бот не является администратором
    }

    public static boolean isValidGroupName(String groupName) {
        return groupName != null && !groupName.isEmpty() && !groupName.contains("_") && !groupName.contains(" ");
    }
}
