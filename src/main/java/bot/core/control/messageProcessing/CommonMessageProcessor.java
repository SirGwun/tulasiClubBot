package bot.core.control.messageProcessing;

import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.model.Session;
import bot.core.model.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Update;
import bot.core.util.ChatUtils;
import bot.core.control.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(CommonMessageProcessor.class);
    Validator validator;

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;
        MessageContext ctx = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .openSessionIfNeeded(update.getMessage().getFrom());
        //todo проверить
        return !ctx.isCommand() && !ctx.isFromGroup() && session.getState().isCommonState();
    }

    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        if (validator == null) validator = new Validator();

        MessageContext ctx = new MessageContext(update.getMessage());
        Session session = SessionController.getInstance()
                .getUserSession(ctx.getFromId());

        long chatId = ctx.getChatId();

        if (ctx.hasPayment()) {
            handlePayment(ctx, session);
        } else {
            ChatUtils.sendMainMenu(chatId);
        }
    }

    private void handlePayment(MessageContext ctx, Session session) {
        long userId = ctx.getFromId();
        log.info("New payment from {}", userId);

        if (session.getGroupId() == null) {
            ChatUtils.sendMessage(ctx.getChatId(), "Группа не выбрана, пожалуйста, выберете группу при помощи \n" +
                    "Меню -> /set_group " +
                    "После чего отправьте подтверждение оплаты повторно");
            return;
        }

        //todo иногда валидатор отрабатывает долго, мб какой-то прогресс бар

        if (validator.isValidPayment(ctx.getMessage())) {
            ChatUtils.addInGroup(userId, session.getGroupId(), "Автоматическая проверка");
            log.info("Автоматическая проверка подтвердила оплату");
        } else {
            validator.sendOuHumanValidation(ctx);
            ChatUtils.sendMessage(userId, "Ваше подтверждение отправлено на проверку. Пожалуйста, подождите.\n \n" +
                    "Как только проверка завершится, бот пришлет вам ссылку для вступления в группу.");
        }
    }
}
