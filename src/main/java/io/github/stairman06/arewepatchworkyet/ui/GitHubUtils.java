package io.github.stairman06.arewepatchworkyet.ui;

import io.github.stairman06.arewepatchworkyet.AreWePatchworkYetGui;
import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import io.github.stairman06.arewepatchworkyet.analyze.ClassMember;
import io.github.stairman06.arewepatchworkyet.mappings.MappingUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeSet;

public class GitHubUtils {
    public static void createRequiredFeaturesIssue() {
        StringBuilder body = new StringBuilder("Missing methods and/or fields:\n");
        try {
            MappingUtils.downloadYarnIfNeeded("1.16.4");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, TreeSet<ClassMember>> entry : Analyzer.neededClassMembers.entrySet()) {
            // only show vanilla and forge classes
            if (entry.getKey().startsWith("net/minecraft")) {
                if (entry.getKey().startsWith("net/minecraft/")) {
                    // vanilla patch
                    String yarnClass = MappingUtils.getYarnClassName(entry.getKey());
                    body.append("- [")
                            .append(yarnClass)
                            .append("](")
                            .append("https://github.com/PatchworkMC/YarnForge/blob/target-applied/patches/minecraft/")
                            .append(MappingUtils.removeSubclassIfNeeded(yarnClass))
                            .append(".java.patch)\n");
                } else {
                    // forge class
                    String clazz = entry.getKey();
                    body.append("- [")
                            .append(clazz)
                            .append("](")
                            .append("https://github.com/PatchworkMC/YarnForge/blob/target-applied/src/main/java/")
                            .append(MappingUtils.removeSubclassIfNeeded(clazz))
                            .append(".java)\n");
                }

                // constructor deduper
                if (!(entry.getValue().size() == 1 && entry.getValue().iterator().next().name.equals("<init>"))) {
                    for (ClassMember classMember : entry.getValue()) {
                        body.append("\t- [ ] ").append(escapeChars(classMember.name)).append("\n");
                    }
                }
            }
        }

        createApiIssue("Required features for [EDIT ME]", body.toString());
    }

    private static String escapeChars(String input) {
        return input.replace("<", "\\<").replace(">", "\\>");
    }

    private static void createApiIssue(String title, String body) {
        try {
            AreWePatchworkYetGui.openURI("https://github.com/PatchworkMC/patchwork-api/issues/new?labels=features+needed+for+mod&title=" + URLEncoder.encode(title, StandardCharsets.UTF_8.toString()) + "&body=" + URLEncoder.encode(body, StandardCharsets.UTF_8.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
