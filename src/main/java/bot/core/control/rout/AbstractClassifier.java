package bot.core.control.rout;

import bot.core.model.input.Input;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractClassifier<I extends Input, K extends Enum<K>> implements InputHandler<I>, Classifier<I, K> {
    Map<K, InputHandler<I>> routes;

    public AbstractClassifier(Class<K> keyClass, List<RoutedHandler<K, I>> children) {
        this.routes = new EnumMap<>(keyClass);
        for (var child : children) {
            routes.put(child.routeKey(), child);
        }
        System.out.println("Classifier has been built for " + keyClass.getName());
    }

    @Override
    public abstract K classify(I input);

    @Override
    public void handle(I input) {
        K key = classify(input);
        InputHandler<I> next = routes.get(key);

        if (next == null) {
            throw new IllegalStateException(
                    "No handlers for key: " + key
            );
        }

        next.handle(input);
    }
}
