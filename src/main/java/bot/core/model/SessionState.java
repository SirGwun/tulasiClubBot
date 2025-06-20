package bot.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;

import static bot.core.model.EditingActions.*;

public class SessionState implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private EditingActions action = NONE;

    public boolean isEditingInfo() {
        return action == EDIT_INFO;
    }
    public void editInfo() {
        action = EDIT_INFO;
    }

    public boolean isEditingHelp() {
        return action == EDIT_HELP;
    }
    public void editHelp() {
        action = EDIT_HELP;
    }

    public EditingActions cansel() {
        EditingActions currentAction = action;
        action = NONE;
        return currentAction;
    }

    public boolean isEditPaymentInfo() {
        return action == EDIT_PAYMENT_INFO;
    }
    public void editPaymentInfo() {
        action = EDIT_PAYMENT_INFO;
    }

    public boolean isCommonState() {
        return action == NONE;
    }
}
