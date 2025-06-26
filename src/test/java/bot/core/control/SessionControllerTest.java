import bot.core.Main;
import bot.core.control.SessionController;
import bot.core.model.Session;
import bot.core.util.DataUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SessionControllerTest {
    private Field sessionMapField;

    @Before
    public void setUp() throws Exception {
        Main.dataUtils = new DataUtils();
        sessionMapField = SessionController.class.getDeclaredField("sessionMap");
        sessionMapField.setAccessible(true);
    }

    @After
    public void tearDown() throws Exception {
        // restore empty map to avoid side effects
        sessionMapField.set(null, new HashMap<Long, Session>());
    }

    @Test
    public void testGetUserIdByUsernameFound() throws Exception {
        Map<Long, Session> map = new HashMap<>();
        map.put(1L, new Session(1L, "user1"));
        sessionMapField.set(null, map);

        SessionController controller = SessionController.getInstance();
        Long id = controller.getUserIdByUsername("user1");
        assertEquals(Long.valueOf(1L), id);
    }

    @Test
    public void testGetUserIdByUsernameNotFound() throws Exception {
        Map<Long, Session> map = new HashMap<>();
        sessionMapField.set(null, map);

        SessionController controller = SessionController.getInstance();
        Long id = controller.getUserIdByUsername("unknown");
        assertNull(id);
    }
}
