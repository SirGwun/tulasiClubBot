package bot.core.control.rout;

import bot.core.model.input.Input;

public interface RoutedHandler<K, I extends Input> extends InputHandler<I> {
    K routeKey();
}
