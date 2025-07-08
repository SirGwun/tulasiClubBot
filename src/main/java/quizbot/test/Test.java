package quizbot.test;

import quizbot.model.DoshaResult;

import java.util.List;
import java.util.Optional;

public class Test {
    private final List<List<String>> blocks;
    private int currentBlock = 0;
    private int currentQuestion = 0;
    private final int[] scores = new int[3];

    public Test(List<List<String>> blocks) {
        this.blocks = blocks;
    }

    public Optional<String> nextQuestion() {
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
        }
        return Optional.of(currentList.get(currentQuestion++));
    }

    public void registerAnswer(int score) {
        if (currentBlock < scores.length) {
            scores[currentBlock] += score;
        }
    }

    public boolean isFinished() {
        return currentBlock >= blocks.size();
    }

    public DoshaResult result() {
        return new DoshaResult(scores[0], scores[1], scores[2]);
    }

    public static Test fromBlocks(List<List<String>> blocks) {
        return new Test(blocks);
    }
}
