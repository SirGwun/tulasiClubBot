package bot.core.control.rout;

import bot.core.control.rout.classify.enums.InputSources;
import bot.core.model.input.Input;
import org.springframework.stereotype.Service;

@Service
public class RoutingTreeRoot {
    Classifier<Input, InputSources> root;

    public RoutingTreeRoot(Classifier<Input, InputSources> root) {
        this.root = root;
    }

    public InputHandler<Input> getRoutingRoot() { // check illegal argument ex
        return root;
    }
}
