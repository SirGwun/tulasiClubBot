package bot.core.control.rout;

import bot.core.model.input.Input;

public interface Classifier<I extends Input, K> extends InputHandler<I> {
    K classify(I input);
}
