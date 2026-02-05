package bot.core.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    private Long userId;

    private Long groupId;

    @Enumerated(EnumType.STRING)
    EditingActionStates actionState;

    public SessionEntity() {}

    public SessionEntity(Session session) {
        userId = session.getUserId();
        groupId = session.getGroupId();
        actionState = session.getAction();
    }

    public Session toDomain() {
        Session s = new Session(userId);
        s.setGroupId(groupId);
        s.setAction(actionState);
        return s;
    }
}
