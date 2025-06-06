package bot.core.util;

import bot.core.Main;
import bot.core.PaymentBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class GroupUtils {
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
                String botUsername = Main.bot.getBotUsername();
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

    private void decline(long userId) {
        try {
            log.info("Откланен запрос {} в группу {}", GroupUtils.getUserName(userId, DataUtils.getMainGroupID()),
                    GroupUtils.getGroupName(DataUtils.getMainGroupID()));
            ChatUtils.sendMessage(userId, "Ваша заявка была отклонена, \n" +
                    "вы можете создать еще одну заявку или обратиться к администратору @Tulasikl");
        } catch (TelegramApiException e) {
            log.error("Error decline user request {} to group {}", userId, DataUtils.getMainGroupID());
        }
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
            PaymentBot.log.error("Ошибка при добавлении пользователя в группу \n {}", e.getMessage());
        }
    }
}
