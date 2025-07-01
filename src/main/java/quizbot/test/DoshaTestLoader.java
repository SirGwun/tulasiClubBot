package quizbot.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DoshaTestLoader {
    public static Test load(Path path) throws IOException {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("#BLOCK")) {
                    if (!current.isEmpty()) {
                        blocks.add(new ArrayList<>(current));
                        current.clear();
                    }
                    continue;
                }
                current.add(line);
            }
        }
        if (!current.isEmpty()) {
            blocks.add(current);
        }
        return Test.fromBlocks(blocks);
    }

    public static Test loadDefault() throws IOException {
        return load(Path.of("data", "dosha_test.txt"));
    }
}
