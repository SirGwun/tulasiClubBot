package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.control.callbackHandlers.Action;
import bot.core.control.callbackHandlers.administation.oneTime.ChoseTagForSpecialGroup;
import bot.core.model.*;
import bot.core.repos.GroupRepository;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> scheduledTask;
    private final AtomicInteger attemptsLeft = new AtomicInteger(0);

    private final Object lock = new Object();

    @Override
    public boolean canProcess(Update update) {
        return isBotAddedToGroup(update) && update.getMyChatMember().getFrom().getId() == Main.dataUtils.getAdminId();
    }

    private boolean isBotAddedToGroup(Update update) {
        if (!update.hasMyChatMember()) return false;
        User user = update.getMyChatMember().getNewChatMember().getUser();
        return user.getUserName().equalsIgnoreCase(Main.paymentBot.getBotUsername());
    }

    @Override
    public void process(Update update) {
        SessionState session = SessionController.getInstance().openSessionIfNeeded(update.getMyChatMember().getFrom()).getState();

        ChatMemberUpdated myChatMember = update.getMyChatMember();
        String status = myChatMember.getNewChatMember().getStatus();

        if (status.equalsIgnoreCase("left")
                || status.equalsIgnoreCase("kicked"))
            processChatLeft(
                    myChatMember.getChat().getId(),
                    myChatMember.getChat().getTitle(),
                    myChatMember.getFrom().getId(),
                    myChatMember.getChat().getType()
            );
        else if (status.equalsIgnoreCase("administrator")
                || status.equalsIgnoreCase("creator")) {
            if (session.getAction() == EditingActions.WAIT_FOR_SPECIAL_GROUP) {
                processSpecialGroupAdding(
                        myChatMember.getChat().getId(),
                        myChatMember.getChat().getTitle(),
                        myChatMember.getFrom().getId()
                );
                session.setAction(EditingActions.NONE);
            } else {
                processChatAddition(
                        myChatMember.getChat().getId(),
                        myChatMember.getChat().getTitle(),
                        myChatMember.getFrom().getId(),
                        myChatMember.getChat().getType()
                );
            }
        }
    }

    private void processSpecialGroupAdding(Long chatId, String chatName, Long fromId) {
        GroupRepository repository = new GroupRepository();

        Main.paymentBot.registerOneTimeHandler(new ChoseTagForSpecialGroup(chatId, chatName));

        List<Tag> tagList = repository.findAllTag();
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (Tag tag : tagList) {
            InlineKeyboardButton button = new InlineKeyboardButton(tag.getName());
            button.setCallbackData(Action.specialGroupTagChoose + "_" + tag.getId());
            buttons.add(Collections.singletonList(button));
        }

        inlineKeyboard.setKeyboard(buttons);

        ChatUtils.sendInlineKeyboard(
                fromId,
                "Выберете для какого тега вы добавили эту избранную группу",
                inlineKeyboard);
    }

    //todo доделать чтобы выходил из специальных групп
    private void processChatLeft(long chatId, String chatName, Long fromId, String type) {
        String chatType = ("group".equals(type) || "supergroup".equals(type)) ? "группы" : "канала";

        if (Main.dataUtils.getGroupById(chatId) != null) {           // чат известен → удаляем
            log.info("Bot left from {}", chatName);
            Main.dataUtils.removeGroup(chatId);
            ChatUtils.sendMessage(fromId, "Бот был удален из " + chatType + " " + chatName);
        } else {                                                     // чат не найден
            log.info("Удален из не записанной группы: {}", chatName);
        }
    }

    private void processChatAddition(long chatId, String chatName, Long fromId, String type) {
        String chatType = ("group".equals(type) || "supergroup".equals(type)) ? "группу" : "канал";

        Group byId   = Main.dataUtils.getGroupById(chatId);
        Group byName = Main.dataUtils.getGroupByName(chatName);

        if (byId == null && byName == null) {                        // полностью новый чат
            log.info("Bot added to {}", chatName);
            Main.dataUtils.addNewGroup(chatName, chatId);
            ChatUtils.sendMessage(fromId, "Вы успешно добавили бота в " + chatType + " " + chatName);
            checkAdminRightsLater(3);
            return;
        }

        if (byId != null && !Objects.equals(byId.getName(), chatName)) {  // id тот же, имя изменилось
            byId.setName(chatName);
            Main.dataUtils.saveGroupList();
            return;
        }

        if (byId == null) {                        // имя занято другим id
            ChatUtils.sendMessage(fromId,
                    "Группа с именем «" + chatName + "» уже зарегистрирована.\n" +
                            "Переименуйте её или удалите добавленную (/del) и попробуйте ещё раз.");
            return;
        }

        log.warn("Добавление уже существующей группы {}", byId);     // id и имя совпали
    }

    public void checkAdminRightsLater(int tries) {
        if (isAdminRightsOK()) {
            return;
        }

        synchronized (lock) {
            if (scheduledTask != null && !scheduledTask.isDone()) {
                attemptsLeft.set(Math.max(attemptsLeft.get(), tries));
                return;
            }

            attemptsLeft.set(tries);
            scheduledTask = scheduler.schedule(this::executeCheck, 5, TimeUnit.SECONDS);
        }
    }

    private void executeCheck() {
        try {
            Main.dataUtils.checkAndFixAdminRights();

            if (isAdminRightsOK()) {
                return;
            }

            int remaining = attemptsLeft.decrementAndGet();

            if (remaining <= 0) {
                notifyAdmin();
                return;
            }

            synchronized (lock) {
                scheduledTask = scheduler.schedule(this::executeCheck, 10, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void notifyAdmin() {
        StringBuilder builder = new StringBuilder();
        List<Group> groupList = Main.dataUtils.getGroupList();

        for (Group group : groupList) {
            if (!group.isBotAdmin()) {
                builder.append(group.getTag())
                        .append(" - ")
                        .append(group.getName())
                        .append("\n");
            }
        }

        ChatUtils.sendMessage(
                Main.dataUtils.getAdminId(),
                "В некоторых добавленных группах бот не является админом. " +
                        "Они не будут отображаться пользователям. " +
                        "Удалите из них бота совсем или удалите и добавьте заново.\n\n" +
                        "Список таких групп:\n" +
                        builder.toString()
        );
    }

    public boolean isAdminRightsOK() {
        List<Group> groupList = Main.dataUtils.getGroupList();
        for (Group group : groupList) {
            if (!group.isBotAdmin()) return false;
        }
        return true;
    }
}
