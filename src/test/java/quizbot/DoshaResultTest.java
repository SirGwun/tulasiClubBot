package quizbot;

import org.junit.Test;
import quizbot.model.DoshaResult;

import static org.junit.Assert.*;

public class DoshaResultTest {
    @Test
    public void testDominant() {
        DoshaResult r = new DoshaResult(30, 20, 10);
        assertEquals("Vata", r.dominant());
        assertFalse(r.isDoubleType());
    }

    @Test
    public void testDoubleType() {
        DoshaResult r = new DoshaResult(30, 28, 5);
        assertTrue(r.isDoubleType());
    }
}
