package bot.core.quizbot.model;

public enum AnswerScale {
    SCORE_0(0, "Совсем не относится ко мне"),
    SCORE_1(1, "Скорее не относится ко мне"),
    SCORE_2(2, "Немного относится ко мне"),
    SCORE_3(3, "Затрудняюсь ответить"),
    SCORE_4(4, "Скорее относится ко мне"),
    SCORE_5(5, "Почти всегда относится ко мне"),
    SCORE_6(6, "Определенно относится ко мне");

    private final int value;
    private final String description;

    AnswerScale(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }
    public String getDescription() { return description; }

    public static AnswerScale fromValue(int v) {
        for (AnswerScale answer : AnswerScale.values()) {
            if (answer.getValue() == v) {
                return answer;
            }
        }
        throw new IllegalArgumentException("Неизвестная оценка: " + v);
    }
}
