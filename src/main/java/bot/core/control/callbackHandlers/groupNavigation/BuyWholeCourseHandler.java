package bot.core.control.callbackHandlers.groupNavigation;

import bot.core.Main;
import bot.core.control.SessionService;
import bot.core.control.callbackHandlers.AbstractCallbackHandler;
import bot.core.control.callbackHandlers.Action;
import bot.core.model.SpecialGroup;
import bot.core.model.Tag;
import bot.core.repos.GroupRepository;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

//мб перенести в ChooseGroupHandler upated - НАДО СОЕДЕНИТЬ В 1 СУЩНОСТЬ! ЛОГИКА ДУБЛИРУЕТСЯ
public class BuyWholeCourseHandler extends AbstractCallbackHandler {
    public BuyWholeCourseHandler() {
        super(Action.buyWholeCourse);
    }

    @Override
    public void handle(Update update) {
        if (update == null || update.getCallbackQuery() == null) {
            return;
        }

        var callbackQuery = update.getCallbackQuery();
        Long fromId = callbackQuery.getFrom().getId();

        String data = callbackQuery.getData();
        if (data == null || !data.contains("_")) {
            ChatUtils.sendMessage(fromId,
                    "Произошла ошибка. Пожалуйста, напишите в поддержку или попробуйте другой способ.");
            return;
        }

        String[] queryData = data.split("_");
        if (queryData.length < 2) {
            ChatUtils.sendMessage(fromId,
                    "Произошла ошибка. Пожалуйста, напишите в поддержку или попробуйте другой способ.");
            return;
        }


        String tagName = queryData[1];

        GroupRepository repository = new GroupRepository();
        String requisites = Main.dataUtils.getPaymentInfo();

        SpecialGroup specialGroup = repository.findSpecialGroupByTag(tagName)
                .orElseGet(() ->
                        repository.findSpecialGroupByTag("default").orElse(null)
                );

        if (specialGroup == null) {
            ChatUtils.sendMessage(fromId,
                    "Произошла ошибка. Пожалуйста, напишите в поддержку или попробуйте другой способ.");
            return;
        }

        SessionService.getInstance().setUserGroupId(fromId, specialGroup.getId());
        Tag tag = repository.findTagByName(tagName).orElse(null);
        int courseSize = tag != null ? repository.getAllGroupForTag(tag).size() : 0;

        final int PRICE_PER_LESSON = 700;
        int totalPrice = courseSize * PRICE_PER_LESSON;

        String text = String.format("""
                        Спасибо за выбор курса "%s".

                        Рекомендуемая сумма за весь курс — %d руб.

                        ━━━━━━━━━━━━━━━━━━
                        ❗️ ЧТО НУЖНО СДЕЛАТЬ:
                        ━━━━━━━━━━━━━━━━━━

                        1️⃣ Оплатить курс по реквизитам выше.
                        2️⃣ ОБЯЗАТЕЛЬНО отправить чек об оплате 
                           (документ, скриншот или фото).
                        3️⃣ Дождаться проверки чека — после этого
                           вас добавят в группу: %s

                        ━━━━━━━━━━━━━━━━━━

                        %s

                        ℹ️ Обратите внимание:
                        Сейчас используется временный способ оплаты.
                        Мы понимаем, что он не самый удобный, и уже работаем
                        над подключением более удобной онлайн-оплаты.

                        После добавления в группу вы получите доступ ко всем лекциям курса.

                        📚 Для доступа к лекциям:
                        Меню → Выбрать курс → "%s" → нужная лекция.
                        """,
                tagName,
                totalPrice,
                requisites,
                specialGroup.getName(),
                tagName
        );


        SessionService sessionService = SessionService.getInstance();
        sessionService.setUserGroupId(fromId, specialGroup.getId());

        ChatUtils.deleteMessage(update.getCallbackQuery().getFrom().getId(),
                update.getCallbackQuery().getMessage().getMessageId());

        ChatUtils.sendMessage(fromId, text);
    }

}
