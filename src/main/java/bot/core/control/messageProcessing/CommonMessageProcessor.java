package bot.core.control.messageProcessing;

import bot.core.control.SessionService;
import bot.core.model.EditingActions;
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
    SessionService sessionService = SessionService.getInstance();

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) return false;
        MessageContext ctx = new MessageContext(update.getMessage());
        long userId = update.getMessage().getFrom().getId();

        return !ctx.isCommand() && ctx.notFromGroup() && sessionService.getAction(userId) == EditingActions.NONE;
    }

    @Override
    public void process(Update update) {
        if (!update.hasMessage()) return;
        if (validator == null) validator = new Validator();

        MessageContext ctx = new MessageContext(update.getMessage());

        long chatId = ctx.getChatId();

        if (ctx.hasPayment()) {
            handlePayment(ctx);
        } else {
            ChatUtils.sendMainMenu(chatId);
        }
    }

    private void handlePayment(MessageContext ctx) {
        long userId = ctx.getFromId();
        log.info("New payment from {}", userId);

        if (sessionService.getUserGroupId(userId) == null) {
            ChatUtils.sendMessage(ctx.getChatId(), "Группа не выбрана, пожалуйста, выберете группу при помощи \n" +
                    "Меню -> Выбрать курс -> Выберете курс -> Выберете лекцию (группу)  " +
                    "После чего отправьте подтверждение оплаты повторно");
            return;
        }


        if (validator.isValidPayment(ctx.message())) {
            ChatUtils.addInGroup(userId, sessionService.getUserGroupId(userId), "Автоматическая проверка");
            log.info("Автоматическая проверка подтвердила оплату");
        } else {
            validator.sendOuHumanValidation(ctx);
            ChatUtils.sendMessage(userId, """
                    Ваше подтверждение отправлено на проверку. Пожалуйста, подождите.
                    \s
                    Как только проверка завершится, бот пришлет вам ссылку для вступления в группу.""");
        }
    }
}
