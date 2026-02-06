package bot.core.control.callbackHandlers;

import bot.core.Main;
import bot.core.util.ChatUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GetInstructionHandler extends AbstractCallbackHandler {
    public GetInstructionHandler() {
        super(Action.getInstruction);
    }

    @Override
    public void handle(Update update) {
        ChatUtils.sendMessage(update.getCallbackQuery().getFrom().getId(),
                Main.dataUtils.getHelp());
    }
}
