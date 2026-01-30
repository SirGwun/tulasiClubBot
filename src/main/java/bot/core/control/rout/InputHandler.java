package bot.core.control.rout;

import bot.core.model.input.Input;

public interface InputHandler<I extends Input> {
    void handle(I input);
}