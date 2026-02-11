package bot.core.control.callbackHandlers.groupNavigation;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.control.callbackHandlers.Action;
import bot.core.control.callbackHandlers.CallbackHandler;
import bot.core.model.Group;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        long chatId = cq.getMessage().getChatId();
        log.debug("User {} set group {}", chatId, groupId);

        Group group = Main.dataUtils.getGroupById(groupId);

        if (group == null) {
            ChatUtils.sendMessage(chatId, "Группа не найдена");
            log.warn("Группа не найдена {}", groupId);
            return;
        }

        if (group.isBotAdmin()) {
            if (isItFavoriteUser(chatId)) {
                ChatUtils.addInGroup(chatId, groupId, "Член избранной группы");
            } else {
                SessionController.getInstance().setUserGroupId(chatId, groupId);
                ChatUtils.sendInlineKeyboard(chatId
                        ,"Выбрана группа: " + group.getName() + "\nВыберете способ оплаты"
                        ,buildPaymentKeyboard(chatId));
            }
        } else {
            ChatUtils.sendMessage(chatId, "Бот не входит в группу или не является в ней администратором");
        }
    }

    private InlineKeyboardMarkup buildPaymentKeyboard(Long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton russianCard = new InlineKeyboardButton("С карты российского банка");
        russianCard.setCallbackData(Action.payWithRussianCard + "_" + chatId);

        InlineKeyboardButton foreignCard = new InlineKeyboardButton("С карты иностранного банка");
        foreignCard.setCallbackData(Action.payWithForeignCard + "_" + chatId);

        InlineKeyboardButton alreadyPaid = new InlineKeyboardButton("Я уже оплатил(а) (прислать чек)");
        alreadyPaid.setCallbackData(Action.alreadyPaid + "_" + chatId);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Collections.singletonList(russianCard));
        rows.add(Collections.singletonList(foreignCard));
        rows.add(Collections.singletonList(alreadyPaid));

        keyboardMarkup.setKeyboard(rows);

        return keyboardMarkup;
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
