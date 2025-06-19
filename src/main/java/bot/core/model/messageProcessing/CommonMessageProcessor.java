package bot.core.model.messageProcessing;

import bot.core.control.Session;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.Main;
import bot.core.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(CommonMessageProcessor.class);
    Validator validator;

    @Override
    public boolean canProcess(MessageContext ctx, Session session) {
        //todo проверить
        return !ctx.isCommand() && !ctx.isFromGroup() && session.getState().isCommonState();
    }

    @Override
    public void process(MessageContext ctx, Session session) {
        long userId = ctx.getChatId();

        if (ctx.hasPayment()) {
            handlePayment(ctx, session);
        } else {
            ChatUtils.sendMessage(userId, "Пожалуйста приложите документ или фото платежа");
        }
    }

    private void handlePayment(MessageContext ctx, Session session) {
        long userId = ctx.getFromId();
        log.info("New payment from {}", userId);

        if (validator == null) validator = new Validator();

        boolean valid = validator.isValidPayment(ctx.getMessage());

        //todo иногда валидатор отрабатывает долго, мб какой-то прогресс бар

        if (valid) {
            ChatUtils.addInGroup(userId, session.getGroupId(), "Автоматическая проверка");
            log.info("Автоматическая проверка подтвердила оплату");
        } else {
            validator.sendOuHumanValidation(ctx);
            ChatUtils.sendMessage(userId, "Ваше подтверждение отправлено на проверку. Пожалуйста, подождите.\n \n" +
                    "Как только проверка завершится, бот пришлет вам ссылку для вступления в группу.");
        }
    }
}
