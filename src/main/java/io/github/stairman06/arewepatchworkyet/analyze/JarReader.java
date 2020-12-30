package io.github.stairman06.arewepatchworkyet.analyze;

import io.github.stairman06.arewepatchworkyet.analyze.asm.BaseClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarReader {
    public static void readDefinedMethods(JarFile jarFile, boolean isForge) {
        for (JarEntry entry : Collections.list(jarFile.entries())) {
            if (entry.getName().endsWith(".class")) {
                // class files
                try {
                    InputStream inputStream = jarFile.getInputStream(entry);
                    ClassReader classReader = new ClassReader(inputStream);
                    ClassVisitor cv = new BaseClassVisitor(isForge);

                    classReader.accept(cv, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (entry.getName().endsWith(".jar")) {
                // handling of JiJ
                try {
                    File tempFile = File.createTempFile("jij", "jij", new File("./temp"));
                    tempFile.delete();
                    Files.copy(jarFile.getInputStream(entry), tempFile.toPath());
                    readDefinedMethods(new JarFile(tempFile), isForge);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
