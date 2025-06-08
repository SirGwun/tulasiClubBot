package bot.core.util;

import org.junit.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;


import static org.junit.Assert.*;

public class DataUtilsTest {
    private static final Path DATA_DIR = Paths.get("data");
    private DataUtils dataUtils;

    @BeforeClass
    public static void setUpClass() throws Exception {
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


    @Before
    public void setUp() {
        dataUtils = new DataUtils();
    }

    @Test
    public void testGetBotToken() {
        assertEquals("prodToken", dataUtils.getBotToken());
    }

    @Test
    public void testTestMode() throws Exception {
        dataUtils.testMode();
        assertEquals("harmoniousNutritionBot", dataUtils.getBotName());
        assertEquals("testToken", dataUtils.getBotToken());
        dataUtils = new DataUtils();
    }

    @Test
    public void testUpdateConfig() {
        assertTrue(dataUtils.updateConfig("adminChatID", "33"));
        assertEquals(33L, dataUtils.getAdminID());
        assertFalse(dataUtils.updateConfig("unknown", "1"));
    }

    @Test
    public void testAddNewGroupAndRemove() {
        assertTrue(dataUtils.addNewGroup("new-group", 555));
        assertEquals("new group", dataUtils.getGroupName(555));
        dataUtils.removeGroup(String.valueOf(555));
        assertNull(dataUtils.getGroupName(555));
    }

    @Test
    public void testGetGroupList() {
        assertNotNull(dataUtils.getGroupList());
    }

    @Test
    public void testBotNameSet() {
        dataUtils.setBotName("anotherBot");
        assertEquals("anotherBot", dataUtils.getBotName());
    }

    @Test
    public void testHelp() throws IOException {
        dataUtils.setHelp("new help");
        assertEquals("new help", dataUtils.getHelp());
    }

    @Test
    public void testInfo() throws IOException {
        dataUtils.setInfo("information");
        assertEquals("information", dataUtils.getInfo());
    }

    @Test
    public void testHistory() {
        assertEquals("hist", dataUtils.getHistroyID());
    }

    @Test
    public void testCatalog() {
        assertEquals("Catalog content", dataUtils.getCatalog());
    }
}
