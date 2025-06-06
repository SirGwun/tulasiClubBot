package bot.core.model.messageProcessing;

import bot.core.control.SessionState;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CommonMessageProcessor implements MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(CommonMessageProcessor.class);

    @Override
    public boolean canProcess(MessageContext ctx, SessionState state) {
        return !ctx.isFromGroup();
    }

    @Override
    public void process(MessageContext ctx, SessionState state) {
        long userId = ctx.getChatId();
        log.info("New message from {}", ctx.getChatId());

        if (ctx.hasPayment()) {
            handlePayment(ctx, userId);
        } else {
            ChatUtils.sendMessage(userId, "Пожалуйста приложите документ или фото платежа");
        }
    }

    private void handlePayment(MessageContext message, long userId) {
        log.info("New payment from {}", userId);
        boolean valid = validator.isValidPayment(message);

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
