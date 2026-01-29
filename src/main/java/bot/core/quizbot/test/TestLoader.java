package bot.core.quizbot.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import bot.core.quizbot.core.QuizBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestLoader {
    private static final Logger log = LoggerFactory.getLogger(TestLoader.class);
    private static final List<String> tests = List.of(
            "doshaTest",
            "bodyParametrs"
    );

    public static Test load(Path path) throws IOException {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        String name = "";
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("#TEST_NAME")) {
                    name = line.split(" ")[1];
                    continue;
                }
                if (line.startsWith("#BLOCK")) {
                    if (!current.isEmpty()) {
                        blocks.add(new ArrayList<>(current));
                        current.clear();
                    }
                    continue;
                }
                current.add(line);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            log.info("В файле теста не найдено имя");
        }
        if (!current.isEmpty()) {
            blocks.add(current);
        }
        if (name.isEmpty()) name = "doshaTest";
        return new Test(name, blocks);
    }

    public static Test loadDefault() throws IOException {
        return load(Path.of(QuizBot.data, "doshaTest.txt"));
    }
}
