package bot.core.model;

import java.io.Serial;
import java.io.Serializable;

import static bot.core.model.EditingActionStates.*;

public class SessionState implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private EditingActionStates action = NONE;

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

    public EditingActionStates cansel() {
        EditingActionStates currentAction = action;
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
