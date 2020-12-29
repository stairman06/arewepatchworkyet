package io.github.stairman06.arewepatchworkyet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MinecraftUtils {
    public static void downloadAndRemapMinecraft(String version) throws Exception {
        if (!Files.exists(Paths.get("./data/minecraft-" + version + "-unmapped.jar"))) {
            MinecraftUtils.downloadMinecraft(version);
        }

        if (!Files.exists(Paths.get("./data/intermediary-" + version + ".tiny"))) {
            MappingUtils.downloadIntermediary(version);
        }

        if (!Files.exists(Paths.get("./data/minecraft-" + version + "-intermediary.jar"))) {
            MinecraftUtils.remapMinecraft(version);
        }
    }

    public static void downloadMinecraft(String version) {
        try {
            String launcherMetaString = IOUtils.toString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"), StandardCharsets.UTF_8);

            Gson gson = new Gson();

            JsonArray versions = gson.fromJson(launcherMetaString, JsonObject.class).get("versions").getAsJsonArray();
            for (JsonElement element : versions) {
                if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    String id = object.get("id").getAsString();

                    if (id.equals(version)) {
                        String versionJsonUrl = object.get("url").getAsString();

                        JsonObject downloads = gson.fromJson(IOUtils.toString(new URL(versionJsonUrl), StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("downloads");

                        String jarUrl = downloads.getAsJsonObject("client").get("url").getAsString();
                        FileUtils.copyURLToFile(new URL(jarUrl), new File("data/minecraft-" + version + "-unmapped.jar"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void remapMinecraft(String version) throws Exception {
        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(new BufferedReader(new FileReader("./data/intermediary-" + version + ".tiny")), "official", "intermediary"))
                .rebuildSourceFilenames(true)
                .build();

        Path original = Paths.get("./data/minecraft-" + version + "-unmapped.jar");
        Path destination = Paths.get("./data/minecraft-" + version + "-intermediary.jar");

        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(destination).build()) {
            outputConsumer.addNonClassFiles(original, NonClassCopyMode.SKIP_META_INF, remapper);
            remapper.readInputs(Paths.get("./data/minecraft-" + version + "-unmapped.jar"));
            remapper.apply(outputConsumer);
        } finally {
            remapper.finish();
        }
    }
}
