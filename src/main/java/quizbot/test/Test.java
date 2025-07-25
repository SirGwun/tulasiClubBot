package quizbot.test;

import quizbot.model.DoshaResult;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class Test implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public final String name;
    private final List<List<String>> blocks;
    private int currentBlock = 0;
    private int currentQuestion = 0;
    private final int[] scores = new int[3];
    private final int[] piks = new int[3];

    public Test(String name, List<List<String>> blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    public Optional<String> nextQuestion() {
        System.out.println(scores[0] + " " + scores[1] + " " + scores[2]);
        if (currentBlock >= blocks.size()) {
            return Optional.empty();
        }
        List<String> currentList = blocks.get(currentBlock);
        if (currentQuestion >= currentList.size()) {
            currentBlock++;
            currentQuestion = 0;
            if (currentBlock >= blocks.size()) {
                return Optional.empty();
            }
            currentList = blocks.get(currentBlock);
            return Optional.of("Вы закончили блок " + getBlockName(currentBlock - 1) + "\n\n" +
                    currentList.get(currentQuestion++));
        }
        return Optional.of(currentList.get(currentQuestion++));
    }

    public void registerAnswer(int score) {
        if (currentBlock < scores.length) {
            scores[currentBlock] += score;
            if (score == 6) {
                piks[currentBlock] += 1;
            }
        }

    }

    private String getBlockName(int number) {
        if (number == 0) {
            return "Вата";
        } else if (number == 1) {
            return "Питта";
        } else if (number == 2) {
            return "Капха";
        } else {
            return "Неизвестное значение";
        }
    }

    public boolean isFinished() {
        return currentBlock >= blocks.size();
    }

    public DoshaResult result() {
        return new DoshaResult(scores[0], scores[1], scores[2], piks[0], piks[1], piks[2]);
    }
}
