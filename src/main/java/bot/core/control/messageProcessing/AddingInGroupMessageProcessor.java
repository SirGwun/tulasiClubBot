package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.SessionService;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AddingInGroupMessageProcessor implements MessageProcessor {
    SessionService sessionService = SessionService.getInstance();
    Logger log = LoggerFactory.getLogger(AddingInGroupMessageProcessor.class);
    GroupRepository groupRepository = new GroupRepository();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(4);

    private final Map<Long, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> attempts = new ConcurrentHashMap<>();

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
        ChatMemberUpdated myChatMember = update.getMyChatMember();
        String status = myChatMember.getNewChatMember().getStatus();

        Long fromId = myChatMember.getFrom().getId();
        Long newChatId = myChatMember.getChat().getId();

        if (status.equalsIgnoreCase("left")
                || status.equalsIgnoreCase("kicked"))
            processChatLeft(
                    newChatId,
                    myChatMember.getChat().getTitle(),
                    fromId,
                    myChatMember.getChat().getType()
            );
        else if (status.equalsIgnoreCase("administrator")
                || status.equalsIgnoreCase("creator")) {
            log.info("Session action in enteringToGroupProcess{}", sessionService.getAction(fromId));
            if (sessionService.getAction(fromId) == EditingActions.WAIT_FOR_SPECIAL_GROUP) {
                processSpecialGroupAdding(
                        newChatId,
                        myChatMember.getChat().getTitle(),
                        fromId
                );
                sessionService.setSessionAction(fromId, EditingActions.NONE);
            } else {
                processChatAddition(
                        newChatId,
                        myChatMember.getChat().getTitle(),
                        fromId,
                        myChatMember.getChat().getType()
                );
            }
        }
    }

    private void processSpecialGroupAdding(Long chatId, String chatName, Long fromId) {
        log.info("In process special group adding {} ", chatId);
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

        Group byId = Main.dataUtils.getGroupById(chatId);
        Group byName = Main.dataUtils.getGroupByName(chatName);

        if (byId == null && byName == null) {                        // полностью новый чат
            log.info("Bot added to {}", chatName);
            groupRepository.saveGroup(new Group(
                    chatName,
                    chatId,
                    Main.dataUtils.getActualGroupTag(),
                    ChatUtils.isBotAdminInGroup(chatId)));
            ChatUtils.sendMessage(fromId, "Вы успешно добавили бота в " + chatType + " " + chatName);
            return;
        }

        if (byId != null && !Objects.equals(byId.getName(), chatName)) {  // id тот же, имя изменилось
            byId.setName(chatName);
            groupRepository.updateGroupName(chatId, chatName);
            return;
        }

        if (byId == null) {                        // имя занято другим id
            ChatUtils.sendMessage(fromId,
                    "Группа с именем «" + chatName + "» уже зарегистрирована.\n" +
                            "Переименуйте её или удалите добавленную (/del) и попробуйте ещё раз.");
            return;
        }

        ChatUtils.sendMessage(fromId,
                "Добавление уже записанной группы, данные обновлены " + chatName);
        log.warn("Добавление уже записанной группы, данные обновлены {}", byId);     // id и имя совпали
        Main.dataUtils.saveOrUpdateGroup(chatId, chatName);

        checkAdminRightsLater(chatId, 3);
    }

    public void checkAdminRightsLater(long groupId, int tries) {

        Group group = Main.dataUtils.getGroupById(groupId);
        if (group == null) return;

        if (group.isBotAdmin()) return;

        attempts.putIfAbsent(groupId, new AtomicInteger(tries));

        tasks.compute(groupId, (id, existingTask) -> {

            if (existingTask != null && !existingTask.isDone()) {
                return existingTask;
            }

            return scheduler.schedule(
                    () -> executeCheck(groupId),
                    5,
                    TimeUnit.SECONDS
            );
        });
    }

    private void executeCheck(Long groupId) {
        try {

            Group group = Main.dataUtils.getGroupById(groupId);
            if (group == null) {
                cleanup(groupId);
                return;
            }

            boolean realAdmin = ChatUtils.isBotAdminInGroup(groupId);

            if (realAdmin != group.isBotAdmin()) {
                group.setIsBotAdmin(realAdmin);
                groupRepository.updateGroupAdminRights(groupId, realAdmin);
            }

            if (realAdmin) {
                cleanup(groupId);
                return;
            }

            AtomicInteger counter = attempts.get(groupId);
            if (counter == null) {
                cleanup(groupId);
                return;
            }

            if (counter.decrementAndGet() <= 0) {
                notifyAdmin(group);
                cleanup(groupId);
                return;
            }

            ScheduledFuture<?> newTask = scheduler.schedule(
                    () -> executeCheck(groupId),
                    10,
                    TimeUnit.SECONDS
            );

            tasks.put(groupId, newTask);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void cleanup(Long groupId) {
        ScheduledFuture<?> future = tasks.remove(groupId);
        if (future != null) {
            future.cancel(false);
        }
        attempts.remove(groupId);
    }


    private void notifyAdmin(Group group) {

        ChatUtils.sendMessage(
                Main.dataUtils.getAdminId(),
                "Бот не является админом в группе:\n" +
                        group.getTag() + " - " +
                        group.getName()
        );
    }
}
