package quizbot;

import org.junit.Test;
import quizbot.test.DoshaTestLoader;
import quizbot.test.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class DoshaTestLoaderTest {
    @Test
    public void testLoad() throws Exception {
        Test t = DoshaTestLoader.load(Path.of("data", "dosha_test.txt"));
        assertNotNull(t);
        assertTrue(t.nextQuestion().isPresent());
    }
}
