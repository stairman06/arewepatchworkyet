package io.github.stairman06.arewepatchworkyet;

import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import io.github.stairman06.arewepatchworkyet.analyze.ClassMember;
import io.github.stairman06.arewepatchworkyet.forge.ForgeInstaller;
import io.github.stairman06.arewepatchworkyet.mappings.MappingUtils;
import io.github.stairman06.arewepatchworkyet.ui.GitHubUtils;
import io.github.stairman06.arewepatchworkyet.ui.ResultListItem;
import io.github.stairman06.arewepatchworkyet.ui.ResultListItemCellRenderer;
import jdk.nashorn.internal.scripts.JO;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

public class AreWePatchworkYetGui {
    private static final String[] MINECRAFT_VERSIONS = new String[]{"1.16.4"};
    private static final String[] MAPPINGS_LIST = new String[]{"intermediary", "yarn"};

    private static JComboBox<String> minecraftVersionBox;
    private static JComboBox<String> mappingsBox;
    private static JTextField inputModTextField;
    private static JTextField apiJarTextField;
    private static JTextField resultSearchTextField;
    private static JPanel inspectionPanel;
    private static JLabel resultAmount;
    private static JPanel statsPanel;
    private static DefaultListModel<ResultListItem> listModel = new DefaultListModel<>();


    private static String getMappingClass(TinyTree tree, String classname) {
        for (ClassDef def : tree.getClasses()) {
            if (def.getName("intermediary").equals(classname)) {
                return def.getName("named");
            }
        }

        return "ERROR";
    }

    public static void main(String[] args) throws Exception {
        try {
            if (Files.exists(Paths.get("./temp"))) {
                FileUtils.deleteDirectory(new File("./temp"));
            }

            new File("./temp").mkdirs();
            if (!Files.exists(Paths.get("./data"))) {
                new File("./data").mkdirs();
            }


            if (!Files.exists(Paths.get("./data/mcpatched-intermediary.jar")) || !Files.exists(Paths.get("./data/forge-intermediary.jar"))) {
                ForgeInstaller.installForge();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JFrame frame = new JFrame("AreWePatchworkYet?");

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();


        // These constants are used by each panel
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;


        // ============
        // CONFIG PANEL
        // ============
        {
            c.gridx = 0;
            c.weightx = 0.3;

            JPanel configPanel = new JPanel();
            configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
            // Title
            {
                JLabel title = new JLabel("AreWePatchworkYet?");
                title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
                configPanel.add(title);
            }

            // Minecraft Version Dropdown
            {
                JPanel minecraftVersionPanel = new JPanel();
                minecraftVersionPanel.setLayout(new BoxLayout(minecraftVersionPanel, BoxLayout.X_AXIS));
                minecraftVersionBox = new JComboBox<>(MINECRAFT_VERSIONS);
                minecraftVersionBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                minecraftVersionPanel.add(new JLabel("Minecraft Version:"));
                minecraftVersionPanel.add(minecraftVersionBox);
                minecraftVersionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(minecraftVersionPanel);
            }

            // Mod Jar Input
            {
                JPanel modJarPanel = new JPanel();
                modJarPanel.setLayout(new BoxLayout(modJarPanel, BoxLayout.X_AXIS));
                File inputPath = new File("./input");
                if (!inputPath.exists()) {
                    inputPath.mkdirs();
                }

                inputModTextField = new JTextField(inputPath.getPath(), 30);

                modJarPanel.add(new JLabel("Input mod directory:"));
                modJarPanel.add(inputModTextField);
                modJarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(modJarPanel);
            }

            // Patchwork API Jar Input
            {
                JPanel apiJarPanel = new JPanel();
                apiJarPanel.setLayout(new BoxLayout(apiJarPanel, BoxLayout.X_AXIS));
                apiJarTextField = new JTextField(new File("./api.jar").getPath(), 30);
                apiJarPanel.add(new JLabel("Patchwork API Jar:"));
                apiJarPanel.add(apiJarTextField);
                apiJarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(apiJarPanel);
            }

            // Mappings Dropdown
            {
                JPanel mappingsPanel = new JPanel();
                mappingsPanel.setLayout(new BoxLayout(mappingsPanel, BoxLayout.X_AXIS));
                mappingsBox = new JComboBox<>(MAPPINGS_LIST);
                mappingsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                mappingsBox.addActionListener(e -> {
                    String selected = (String) mappingsBox.getSelectedItem();
                    if (selected.equals("yarn")) {
                        try {
                            MappingUtils.downloadYarnIfNeeded((String) minecraftVersionBox.getSelectedItem());
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }

                    if (resultSearchTextField.getText().equals("Search classes...")) {
                        renderNeededMethods();
                    } else {
                        renderNeededMethods(resultSearchTextField.getText());
                    }
                });

                mappingsPanel.add(new JLabel("Mappings to display in:"));
                mappingsPanel.add(mappingsBox);
                mappingsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(mappingsPanel);
            }

            // Analyze Button
            {
                JButton analyzeButton = new JButton("Analyze");

                analyzeButton.addActionListener(e -> {
                    try {
                        Path inputPath = Paths.get(inputModTextField.getText());
                        Path apiPath = Paths.get(apiJarTextField.getText());
                        if (!inputPath.toFile().exists()) {
                            JOptionPane.showMessageDialog(null, "You need to supply an input mod jar!", "Unable to analyze", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if (!apiPath.toFile().exists()) {
                            JOptionPane.showMessageDialog(null, "You need to supply a Patchwork API jar!", "Unable to analyze", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        AreWePatchworkYet.start(
                                (String) minecraftVersionBox.getSelectedItem(),
                                inputPath,
                                Paths.get(apiJarTextField.getText())
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                configPanel.add(analyzeButton, BorderLayout.CENTER);
            }

            containerPanel.add(configPanel, c);
        }

        // =============
        // RESULTS PANEL
        // =============
        {
            c.gridx = 1;
            c.weightx = 0.75;

            JPanel resultsPanel = new JPanel();
            resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
            // Result information
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(new JLabel("These results may not be representative of the actual support."));

                resultAmount = new JLabel("Click analyze to view results");
                infoPanel.add(resultAmount);

                JPanel statsPanel = new JPanel();
                statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
                infoPanel.add(statsPanel);
                AreWePatchworkYetGui.statsPanel = statsPanel;

                resultSearchTextField = new JTextField("Search classes...", 30);
                resultSearchTextField.addActionListener(e -> {
                    renderNeededMethods(resultSearchTextField.getText());
                });
                infoPanel.add(resultSearchTextField);

                JButton exportButton = new JButton("Export to GitHub Issue");
                exportButton.addActionListener(e -> {
                    if (Analyzer.neededClassMembers.size() == 0) {
                        JOptionPane.showMessageDialog(null, "You need to run analyze first!", "Nothing to export!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    GitHubUtils.createRequiredFeaturesIssue();
                });
                infoPanel.add(exportButton);


                resultsPanel.add(infoPanel);
            }

            // Result List
            {
                JList<ResultListItem> list = new JList<>(listModel);
                list.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (list.getSelectedValue() != null) {
                            ResultListItem selectedItem = list.getSelectedValue();
                            if (selectedItem.getType() == ResultListItem.Type.CLASS) {
                                inspectClass((String) selectedItem.getObject());
                            } else {
                                inspectClassMember((ClassMember) selectedItem.getObject());
                            }
                        }
                    }
                });

                list.setCellRenderer(new ResultListItemCellRenderer());
                resultsPanel.add(new JScrollPane(list));
            }

            containerPanel.add(resultsPanel, c);
        }

        // ================
        // INSPECTION PANEL
        // ================
        {
            c.gridx = 2;
            c.weightx = 0.4;

            JPanel inspectionPanel = new JPanel();
            inspectionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            inspectionPanel.setLayout(new BoxLayout(inspectionPanel, BoxLayout.Y_AXIS));
            inspectionPanel.setMinimumSize(new Dimension(200, 0));

            JLabel selectALabel = new JLabel("Select a method or class");
            inspectionPanel.add(selectALabel);

            containerPanel.add(inspectionPanel, c);

            AreWePatchworkYetGui.inspectionPanel = inspectionPanel;
        }

        frame.add(containerPanel);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void renderNeededMethods() {
        renderNeededMethods("");
    }

    public static void renderNeededMethods(String searchTerm) {
        int neededCount = 0;

        listModel.clear();
        for (Map.Entry<String, TreeSet<ClassMember>> entry : Analyzer.neededClassMembers.entrySet()) {
            ResultListItem item = new ResultListItem(ResultListItem.Type.CLASS, entry.getKey());

            String keyName = MappingUtils.getClassName(entry.getKey());
            if (keyName.toLowerCase().contains(searchTerm.trim().toLowerCase()) || searchTerm.trim().isEmpty()) {
                listModel.addElement(item);

                for (ClassMember classMember : entry.getValue()) {
                    neededCount++;
                    ResultListItem memberItem = new ResultListItem(ResultListItem.Type.CLASS_MEMBER, classMember);
                    listModel.addElement(memberItem);
                }
            }
        }

        resultAmount.setText("Needed methods/fields: " + neededCount);


        // statistics!
        statsPanel.removeAll();
        {
            JLabel label = new JLabel("Statistics");
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            statsPanel.add(label);
        }

        {
            JLabel label = new JLabel("Mixins: " + Analyzer.statMap.getOrDefault(Analyzer.Stat.MIXIN, 0));
            statsPanel.add(label);
        }

        {
            JLabel label = new JLabel("JS Coremods: " + Analyzer.statMap.getOrDefault(Analyzer.Stat.JS_COREMOD, 0));
            statsPanel.add(label);
        }

        statsPanel.updateUI();;
    }

    public static void inspectClass(String owner) {
        inspectionPanel.removeAll();

        updateClass(owner);

        inspectionPanel.updateUI();
    }

    private static int addClassHierarchy(String className, int indentationLevel) {
        int ret = 0;
        HashSet<String> superCache;

        HashMap<String, HashSet<String>> mergedSuperCache = Analyzer.superCache;
        for (Map.Entry<String, HashSet<String>> entry : Analyzer.forgeSuperCache.entrySet()) {
            if (mergedSuperCache.containsKey(entry.getKey())) {
                mergedSuperCache.get(entry.getKey()).addAll(entry.getValue());
            } else {
                mergedSuperCache.put(entry.getKey(), entry.getValue());
            }
        }
//
//        if (className.startsWith("net/minecraftforge/")) {
//            superCache = Analyzer.forgeSuperCache.getOrDefault(className, new HashSet<>());
//        } else {
//            superCache = Analyzer.superCache.getOrDefault(className, new HashSet<>());
//        }

        for (String superclass : mergedSuperCache.getOrDefault(className, new HashSet<>())) {
            if (!superclass.equals("java/lang/Object")) {
                JLabel label = new JLabel(new String(new char[indentationLevel]).replace("\0", "    ") + "- " + MappingUtils.getClassName(superclass));

                if (!Analyzer.isClassImplemented(superclass) || (Analyzer.superCache.get(superclass) != null && !Analyzer.superCache.get(className).contains(superclass))) {
                    label.setForeground(Color.RED);
                }
                inspectionPanel.add(label);
                ret++;
                addClassHierarchy(superclass, indentationLevel + 1);
            }
        }

        return ret;
    }

    public static void updateClass(String owner) {
        {
            JLabel label = new JLabel("Selected Class");
            label.setFont(label.getFont().deriveFont(13f));
            inspectionPanel.add(label);
        }

        {
            JLabel label = new JLabel(MappingUtils.getClassName(owner));
            if (!Analyzer.isClassImplemented(owner)) {
                label.setForeground(Color.RED);
            }
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            inspectionPanel.add(label);
        }

        {
            JLabel label = new JLabel("implements or extends:");
            inspectionPanel.add(label);
        }

        {
            int hierarchyAmount = addClassHierarchy(owner, 1);

            if (hierarchyAmount == 0) {
                JLabel label = new JLabel("Nothing");
                inspectionPanel.add(label);
            }
        }
    }

    private static void showImplementationTip(String tip) {
        JLabel tipsLabel = new JLabel("Implementation tips");
        tipsLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
        tipsLabel.setFont(tipsLabel.getFont().deriveFont(13f));
        tipsLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel tipLabel = new JLabel("<html>" + tip + "</html>");
        inspectionPanel.add(tipsLabel);
        inspectionPanel.add(tipLabel);
    }

    public static void openURI(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void inspectClassMember(ClassMember newClassMember) {
        inspectionPanel.removeAll();

        updateClass(newClassMember.ownerClass);
        {
            String labelText;
            if (newClassMember.type == ClassMember.Type.FIELD) {
                labelText = "Selected Field";
            } else {
                labelText = "Selected Method";
            }

            JLabel label = new JLabel(labelText);
            label.setBorder(new EmptyBorder(20, 0, 0, 0));
            label.setFont(label.getFont().deriveFont(13f));
            label.setAlignmentY(Component.TOP_ALIGNMENT);

            inspectionPanel.add(label);
        }

        if (newClassMember.type == ClassMember.Type.METHOD) {
            JLabel label = new JLabel(newClassMember.name + MappingUtils.getMethodDescriptor(newClassMember.descriptor));
            inspectionPanel.add(label);
        } else if (newClassMember.type == ClassMember.Type.FIELD) {
            JLabel label = new JLabel(MappingUtils.getFieldDescriptor(newClassMember.descriptor) + " " + newClassMember.name);
            inspectionPanel.add(label);
        }

        {
            if (newClassMember.caller != null) {
                JLabel label = new JLabel((newClassMember.type == ClassMember.Type.METHOD ? "Called" : "Accessed") + " by: ");
                inspectionPanel.add(label);

                JLabel label2 = new JLabel(MappingUtils.getClassName(newClassMember.caller));
                inspectionPanel.add(label2);
            }
        }

        {
            if (newClassMember.ownerClass.startsWith("net/minecraftforge/")) {
                JButton viewYarnForge = new JButton("View source (YarnForge)");
                viewYarnForge.addActionListener(e -> {
                    openURI("https://github.com/PatchworkMC/YarnForge/blob/target-applied/src/main/java/" + MappingUtils.removeSubclassIfNeeded(newClassMember.ownerClass) + ".java");
                });

                inspectionPanel.add(viewYarnForge);

                JButton viewForgeUpstream = new JButton("View source (Forge Upstream)");
                viewForgeUpstream.addActionListener(e -> {
                    // TODO: un-hardcode 1.16.x
                    openURI("https://github.com/MinecraftForge/MinecraftForge/blob/1.16.x/src/main/java/" + MappingUtils.removeSubclassIfNeeded(newClassMember.ownerClass) + ".java");
                });

                inspectionPanel.add(viewForgeUpstream);
            }

            if (newClassMember.ownerClass.startsWith("net/minecraft/")) {
                if (newClassMember.name.equals("<init>")) {
                    showImplementationTip("This looks like a Forge-added constructor. You'll need to redirect this to a custom class in Patcher.");
                } else if (newClassMember.type == ClassMember.Type.METHOD) {
                    showImplementationTip("It looks like Forge is adding a method to this Minecraft class. You'll need to create a duck interface, mixin to the Minecraft class, and implement the interface.");
                } else if (newClassMember.type == ClassMember.Type.FIELD) {
                    // TODO: auto recognize public static fields
                    showImplementationTip("It looks like Forge is adding a field to this Minecraft class. If this is a public static field, you'll need to redirect it with Patcher.");
                }

                {
                    JButton viewYarnForgePatch = new JButton("View Forge's patch (YarnForge)");
                    viewYarnForgePatch.addActionListener(e -> {
                        try {
                            MappingUtils.downloadYarnIfNeeded((String) minecraftVersionBox.getSelectedItem());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        openURI("https://github.com/PatchworkMC/YarnForge/blob/target-applied/patches/minecraft/" + MappingUtils.removeSubclassIfNeeded(MappingUtils.getYarnClassName(newClassMember.ownerClass)) + ".java.patch");
                    });

                    inspectionPanel.add(viewYarnForgePatch);
                }
            }
        }
        inspectionPanel.updateUI();
    }

    public static String getCurrentMappings() {
        return (String) mappingsBox.getSelectedItem();
    }
}
