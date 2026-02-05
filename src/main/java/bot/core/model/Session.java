package bot.core.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;

import static bot.core.model.EditingActionStates.*;


public class Session implements Serializable {
    @Serial
    private static final long serialVersionUID = 4L;

    private Long userId;

    private Long groupId;
    private EditingActionStates action = NONE;

    public Session() {}

    public Session(long userId) {
        this.userId = userId;
    }

    public EditingActionStates getAction() {
        return action;
    }

    public void setAction(EditingActionStates action) {
        this.action = action;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
