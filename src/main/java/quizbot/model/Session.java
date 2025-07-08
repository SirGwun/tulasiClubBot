package quizbot.model;

import quizbot.test.Test;

public class Session {
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
