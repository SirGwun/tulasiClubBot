package bot.core.control.callbackHandlers;

import bot.core.Legacy;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GetInstructionHandler extends AbstractCallbackHandler {
    public GetInstructionHandler() {
        super(Action.getInstruction, 2);
    }

    @Override
    public void handle(Update update) {
        ChatUtils.sendMessage(update.getCallbackQuery().getFrom().getId(),
                Legacy.dataUtils.getHelp());
    }
}
