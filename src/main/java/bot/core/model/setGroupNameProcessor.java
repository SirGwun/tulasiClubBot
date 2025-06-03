package bot.core.model;

import bot.core.control.EditingSessionState;
import bot.core.util.ChatUtils;
import bot.core.util.GroupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class setGroupNameProcessor implements MessageProcessor {
    private final EditingSessionState state;
    private static final Logger log = LoggerFactory.getLogger(setGroupNameProcessor.class);

    public static final String GROUP_NAME_SETUP_INSTRUCTION = """
    Группа успешно назначена: "%s".
    
    Теперь добавьте бота в нужную группу и назначьте его администратором.
    После этого заданное имя будет использоваться ботом для этой группы.
    
    Учтите, что название группы в Telegram останется без изменений — имя используется только во внутренней логике бота.
    """;

    public setGroupNameProcessor(EditingSessionState state) {
        this.state = state;
    }

    @Override
    public boolean canProcess(MessageContext ctx) {
        return state.isWaitingGroupName() && ctx.isFromAdmin() && !ctx.getText().equals("/cancel");
    }

    @Override
    public void process(MessageContext ctx) {
        String name = ctx.getText();
        long chatId = ctx.getChatID();

        log.info("New group name input received: '{}'", name);

        if (name.length() > 128) {
            ChatUtils.sendMessage(chatId, "Слишком длинное имя группы, пожалуйста, используйте не более 128 символов");
            return;
        }

        if (!GroupUtils.isValidGroupName(name)) {
            ChatUtils.sendMessage(chatId, "Некорректное имя группы");
            return;
        }

        state.pendingGroupName = name.replace(" ", "-").replace("_", "-");
        state.setWaitingGroupName(false);

        ChatUtils.sendMessage(ctx.getChatID(), String.format(GROUP_NAME_SETUP_INSTRUCTION, state.pendingGroupName));
    }
}
