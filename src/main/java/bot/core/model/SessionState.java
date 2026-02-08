package bot.core.model;

import java.io.Serial;
import java.io.Serializable;

import static bot.core.model.EditingActions.*;

public class SessionState implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private EditingActions action = NONE;

    public void setAction(EditingActions action) {
        this.action = action;
    }

    public boolean isCommonState() {
        return action == NONE;
    }

    public EditingActions getAction() {
        return action;
    }
}
