package bot.core.control.rout.classify;

import bot.core.control.rout.AbstractClassifier;
import bot.core.control.rout.RoutedHandler;
import bot.core.control.rout.classify.enums.InputSources;
import bot.core.model.input.Input;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class SourceClassifier extends AbstractClassifier<Input, InputSources> {

    public SourceClassifier(List<RoutedHandler<InputSources, Input>> children) {
        super(InputSources.class, children);
    }

    @Override
    public InputSources classify(Input input) {
        if (input instanceof Update) return InputSources.TELEGRAM;
        throw new IllegalArgumentException("There is no classification rule for " + input);
    }
}
