package bot.core.model.messageProcessing;

import bot.core.control.SessionState;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import bot.core.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CommonMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(CommonMessageProcessor.class);
    Validator validator;

    @Override
    public boolean canProcess(MessageContext ctx, SessionState state) {
        return !ctx.isFromGroup();
    }

    @Override
    public void process(MessageContext ctx, SessionState state) {
        long userId = ctx.getChatId();
        log.info("New message from {}", userId);

        if (ctx.hasPayment()) {
            handlePayment(ctx);
        } else {
            ChatUtils.sendMessage(userId, "Пожалуйста приложите документ или фото платежа");
        }
    }

    private void handlePayment(MessageContext message, SessionState state) {
        long userId = ctx.getChatId();
        log.info("New payment from {}", userId);

        if (validator == null) validator = new Validator();

        boolean valid = validator.isValidPayment(message.getMessage());

        if (valid) {
            addInGroup(userId);
            ChatUtils.sendMessage(Long.parseLong(DataUtils.getHistroyID()), "Добавлен в группу автопроверкой");
            log.info("Автоматическая проверка подтвердила оплату");
        } else {
            validator.sendOuHumanValidation(message);
            ChatUtils.sendMessage(userId, "Ваше подтверждение отправлено на проверку. Пожалуйста, подождите.\n \n" +
                    "Как только проверка завершится, бот пришлет вам ссылку для вступления в группу.");
        }
    }
}
