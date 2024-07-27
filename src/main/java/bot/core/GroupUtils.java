package bot.core;

import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;

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
}
