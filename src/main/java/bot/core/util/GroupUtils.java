package bot.core.util;

import bot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class GroupUtils {
    private static final Logger log = LoggerFactory.getLogger(GroupUtils.class);
    public static CreateChatInviteLink createInviteLink(String groupID) {
        CreateChatInviteLink inviteLink = new CreateChatInviteLink();
        inviteLink.setChatId(groupID);
        inviteLink.setName("Добро пожаловать на курс!");
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
                String botUsername = Main.dataUtils.getBotName();
                for (ChatMember admin : admins) {
                    if (admin.getUser().getUserName().equals(botUsername)) {
                        return true; // Бот является администратором в группе
                    }
                }
            }
        } catch (TelegramApiException e) {
            Main.log.info("Бот не адмистратор в группе {}", groupId);
        }
        return false; // Группа не существует или бот не является администратором
    }

    public static boolean isValidGroupName(String groupName) {
        return groupName != null && !groupName.isEmpty() && !groupName.contains("_") && groupName.length() < 128;
    }

    public static void addInGroup(long userId, String groupName) {
        CreateChatInviteLink inviteLink;
        inviteLink = createInviteLink(groupName);

        try {
            SendMessage message = new SendMessage();
            message.setChatId(userId);
            message.setText("Здравствуйте!\n\nОплата подтверждена. Для присоединения к группе перейдите по ссылке ниже:\n\n" +
                    "<a href=\"" + Main.bot.execute(inviteLink).getInviteLink() + "\">Присоединиться к курсу</a>\n\n" +
                    "Мы рады вас видеть!");
            message.setParseMode(ParseMode.HTML);
            Main.bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при добавлении пользователя в группу \n {}", e.getMessage());
        }
    }
}
