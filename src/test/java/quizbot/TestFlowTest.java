package quizbot;

import org.junit.Test;
import quizbot.test.DoshaTestLoader;
import quizbot.test.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class TestFlowTest {
    @Test
    public void testQuestionFlow() throws Exception {
        Test t = DoshaTestLoader.load(Path.of("data", "dosha_test.txt"));
        int count = 0;
        while (t.nextQuestion().isPresent()) {
            t.registerAnswer(1);
            count++;
        }
        assertTrue(t.isFinished());
        assertEquals(9, count);
    }
}
