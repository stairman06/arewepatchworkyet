package io.github.stairman06.arewepatchworkyet;

import io.github.stairman06.arewepatchworkyet.mappings.AsmRemapper;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MappingUtils {
    private static TinyTree tinyTree;

    public static void setupYarnTree(String version) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader("./data/yarn-" + version + "-v2.tiny"))) {
            MappingUtils.tinyTree = TinyMappingFactory.load(reader);
        }
    }

    public static void downloadIntermediary(String version) throws IOException {
        Path jarDest = Paths.get("./data/intermediary-" + version + ".jar");
        Path intermediaryDest = Paths.get("./data/intermediary-" + version + ".tiny");
        FileUtils.copyURLToFile(new URL("https://maven.fabricmc.net/net/fabricmc/intermediary/" + version + "/intermediary-" + version + ".jar"), jarDest.toFile());

        try(FileSystem fs = FileSystems.newFileSystem(jarDest, null)) {
            Files.copy(fs.getPath("/mappings/mappings.tiny"), intermediaryDest);
        }
    }

    public static void downloadYarn(String version) throws IOException {
        Path jarDest = Paths.get("./data/yarn-" + version + "-v2.jar");
        Path yarnDest = Paths.get("./data/yarn-" + version + "-v2.tiny");
        FileUtils.copyURLToFile(new URL("https://maven.fabricmc.net/net/fabricmc/yarn/" + version + "+build.1/yarn-" + version + "+build.1-v2.jar"), jarDest.toFile());

        try(FileSystem fs = FileSystems.newFileSystem(jarDest, null)) {
            Files.copy(fs.getPath("/mappings/mappings.tiny"), yarnDest);
        }
    }

    public static void downloadYarnIfNeeded(String version) throws Exception {
        if (!Files.exists(Paths.get("./data/yarn-" + version + "-v2.tiny"))) {
            MappingUtils.downloadYarn(version);
        }

        MappingUtils.setupYarnTree(version);
    }

    public static String getYarnClassName(String intermediary) {
        for (ClassDef each : MappingUtils.tinyTree.getClasses()) {
            if (each.getName("intermediary").equals(intermediary)) {
                return each.getName("named");
            }
        }
        return intermediary;
    }

    public static String remapDescriptorToYarn(String descriptor) {
        AsmRemapper remapper = new AsmRemapper();
        return remapper.mapMethodDesc(descriptor);
    }

    /**
     * Takes a name, and remaps it if necessary
     * @param rawName Raw name, probably in intermediary mappings
     * @return A mapped name if the user has selected to view it
     */
    public static String getClassName(String rawName) {
        if (AreWePatchworkYetGui.getCurrentMappings().equals("yarn")) {
            return MappingUtils.getYarnClassName(rawName);
        }

        return rawName;
    }

    /**
     * Takes a descriptor and remaps it if necessary
     * @param rawDescriptor R descriptor, probably in intermediary mappings
     * @return A mapped descriptor if the user has selected to view it
     */
    public static String getDescriptor(String rawDescriptor) {
        if (AreWePatchworkYetGui.getCurrentMappings().equals("yarn")) {
            return MappingUtils.remapDescriptorToYarn(rawDescriptor);
        }

        return rawDescriptor;
    }
}
