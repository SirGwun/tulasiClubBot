package quizbot.model;

public enum AnswerScale {
    SCORE_0(0),
    SCORE_1(1),
    SCORE_2(2),
    SCORE_3(3),
    SCORE_4(4),
    SCORE_5(5),
    SCORE_6(6),
    SCORE_7(7);

    private final int value;

    AnswerScale(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AnswerScale fromValue(int v) {
        for (AnswerScale a : values()) {
            if (a.value == v) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + v);
    }
}
