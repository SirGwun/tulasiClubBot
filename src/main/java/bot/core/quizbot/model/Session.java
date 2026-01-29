package bot.core.quizbot.model;

import bot.core.quizbot.test.Test;

import java.io.Serial;
import java.io.Serializable;

public class Session implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long userId;
    private final Test test;

    public Session(long userId, Test test) {
        this.userId = userId;
        this.test = test;
    }

    public long getUserId() {
        return userId;
    }

    public Test getTest() {
        return test;
    }
}
