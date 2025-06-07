package bot.core.utilTest;

import bot.core.DataSincroniser;
import bot.core.util.DataUtils;
import org.junit.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class DataUtilsTest {
    private static final Path DATA_DIR = Paths.get("src","test", "testData");

    @BeforeClass
    public static void setUpClass() throws Exception {
        DataSincroniser.sincronise();
        Files.createDirectories(DATA_DIR);

        Properties config = new Properties();
        config.setProperty("adminChatID", "10");
        config.setProperty("groupID", "20");
        config.setProperty("history", "hist");
        try (OutputStream out = Files.newOutputStream(DATA_DIR.resolve("config.properties"))) {
            config.store(out, null);
        }

        Properties groups = new Properties();
        groups.setProperty("default-group", "1000");
        try (OutputStream out = Files.newOutputStream(DATA_DIR.resolve("groupList.properties"))) {
            groups.store(out, null);
        }

        Files.writeString(DATA_DIR.resolve("help.txt"), "Initial help", StandardCharsets.UTF_8);
        Files.writeString(DATA_DIR.resolve("info.txt"), "Initial info", StandardCharsets.UTF_8);
        Files.writeString(DATA_DIR.resolve("catalog.txt"), "Catalog content", StandardCharsets.UTF_8);
    }

    private void setStaticField(String name, Object value) throws Exception {
        Field f = DataUtils.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    @Test
    public void testGetBotToken() {
        assertEquals("prodToken", DataUtils.getBotToken());
    }

    @Test
    public void testTestMode() throws Exception {
        DataUtils.testMode();
        assertEquals("harmoniousNutritionBot", DataUtils.getBotName());
        assertEquals("testToken", DataUtils.getBotToken());
        // restore
        setStaticField("botName", "tulasiClubBot");
        setStaticField("botToken", "prodToken");
    }

    @Test
    public void testUpdateConfig() {
        assertTrue(DataUtils.updateConfig("adminChatID", "33"));
        assertEquals(33L, DataUtils.getAdminID());
        assertFalse(DataUtils.updateConfig("unknown", "1"));
    }

    @Test
    public void testAddNewGroupAndRemove() {
        assertTrue(DataUtils.addNewGroup("new-group", 555));
        assertEquals("new group", DataUtils.getGroupName(555));
        DataUtils.removeGroup(String.valueOf(555));
        assertNull(DataUtils.getGroupName(555));
    }

    @Test
    public void testGetGroupList() {
        assertNotNull(DataUtils.getGroupList());
    }

    @Test
    public void testBotNameSet() {
        DataUtils.setBotName("anotherBot");
        assertEquals("anotherBot", DataUtils.getBotName());
    }

    @Test
    public void testHelp() throws IOException {
        DataUtils.setHelp("new help");
        assertEquals("new help", DataUtils.getHelp());
    }

    @Test
    public void testInfo() throws IOException {
        DataUtils.setInfo("information");
        assertEquals("information", DataUtils.getInfo());
    }

    @Test
    public void testHistory() {
        assertEquals("hist", DataUtils.getHistroyID());
    }

    @Test
    public void testCatalog() {
        assertEquals("Catalog content", DataUtils.getCatalog());
    }
}
