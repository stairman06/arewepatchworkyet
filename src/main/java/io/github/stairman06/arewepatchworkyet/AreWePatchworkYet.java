package io.github.stairman06.arewepatchworkyet;

import io.github.stairman06.arewepatchworkyet.analyze.JarReader;
import io.github.stairman06.arewepatchworkyet.analyze.asm.ModClassVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AreWePatchworkYet {
    public static final Logger LOGGER = LogManager.getFormatterLogger("AWPY");

    public static void start(String minecraftVersion, Path modJarPath, Path apiJarPath) throws Exception {
        LOGGER.info("Starting analyzing mod jar...");

        LOGGER.info("Downloading and remapping Minecraft...");
        MinecraftUtils.downloadAndRemapMinecraft(minecraftVersion);

        LOGGER.info("Processing Minecraft and Patchwork API jars");
        AreWePatchworkYet.processLibs(Paths.get("./data/minecraft-" + minecraftVersion + "-intermediary.jar"), apiJarPath);

        AreWePatchworkYet.processModJar(new JarFile(modJarPath.toFile()));

        AreWePatchworkYetGui.renderNeededMethods();
    }

    public static void processLibs(Path minecraftJarPath, Path apiJarPath) throws Exception {
        JarFile mcFile = new JarFile(minecraftJarPath.toFile());
        JarFile patchworkFile = new JarFile(apiJarPath.toFile());
        JarReader.readDefinedMethods(mcFile);
        JarReader.readDefinedMethods(patchworkFile);
    }


    public static void processModJar(JarFile jarFile) throws Exception {
        JarReader.readDefinedMethods(jarFile); // add defined classes
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
                ClassNode classNode = new ClassNode();


                InputStream classFileInputStream = jarFile.getInputStream(entry);
                try {
                    ClassReader classReader = new ClassReader(classFileInputStream);

                    ModClassVisitor cv = new ModClassVisitor();

                    classReader.accept(cv, 0);
                } finally {
                    classFileInputStream.close();
                }
            }
        }
    }

}
