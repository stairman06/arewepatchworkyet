package io.github.stairman06.arewepatchworkyet;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MappingUtils {
    public static void downloadIntermediary(String version) throws IOException {
        Path jarDest = Paths.get("./data/intermediary-" + version + ".jar");
        Path intermediaryDest = Paths.get("./data/intermediary-" + version + ".tiny");
        FileUtils.copyURLToFile(new URL("https://maven.fabricmc.net/net/fabricmc/intermediary/" + version + "/intermediary-" + version + ".jar"), jarDest.toFile());

        try(FileSystem fs = FileSystems.newFileSystem(jarDest, null)) {
            Files.copy(fs.getPath("/mappings/mappings.tiny"), intermediaryDest);
        }
    }
}
