package bot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class DataSincroniser {
    private static final Logger log = LoggerFactory.getLogger(DataSincroniser.class);

    private static final List<String> dataFilesNames = Arrays.asList(
            "catalog.txt",
            "config.properties",
            "help.txt"
    );
    public static void sincronise() {
         for (String fileName : dataFilesNames) {
             File input = Path.of("data", fileName).toFile();
             File output = Path.of("src" ,"test", "testData", fileName).toFile();
             try (FileInputStream inputStream = new FileInputStream(input);
                  FileOutputStream outputStream = new FileOutputStream(output)) {
                outputStream.write(inputStream.readAllBytes());
             } catch (IOException e) {
                 log.error(e.getMessage());
                 e.printStackTrace();
             }
         }
    }
}
