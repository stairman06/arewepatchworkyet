package io.github.stairman06.arewepatchworkyet;

import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import io.github.stairman06.arewepatchworkyet.analyze.Method;
import io.github.stairman06.arewepatchworkyet.ui.ResultListItem;
import io.github.stairman06.arewepatchworkyet.ui.ResultListItemCellRenderer;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                AreWePatchworkYetGui.minecraftVersionBox = new JComboBox<>(MINECRAFT_VERSIONS);
                AreWePatchworkYetGui.minecraftVersionBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                minecraftVersionPanel.add(new JLabel("Minecraft Version:"));
                minecraftVersionPanel.add(AreWePatchworkYetGui.minecraftVersionBox);
                minecraftVersionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(minecraftVersionPanel);
            }

            // Mod Jar Input
            {
                JPanel modJarPanel = new JPanel();
                modJarPanel.setLayout(new BoxLayout(modJarPanel, BoxLayout.X_AXIS));
                AreWePatchworkYetGui.inputModTextField = new JTextField(new File("./mod.jar").getPath(), 30);

                modJarPanel.add(new JLabel("Input mod:"));
                modJarPanel.add(AreWePatchworkYetGui.inputModTextField);
                modJarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(modJarPanel);
            }

            // Patchwork API Jar Input
            {
                JPanel apiJarPanel = new JPanel();
                apiJarPanel.setLayout(new BoxLayout(apiJarPanel, BoxLayout.X_AXIS));
                AreWePatchworkYetGui.apiJarTextField = new JTextField(new File("./api.jar").getPath(), 30);
                apiJarPanel.add(new JLabel("Patchwork API Jar:"));
                apiJarPanel.add(AreWePatchworkYetGui.apiJarTextField);
                apiJarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(apiJarPanel);
            }

            // Mappings Dropdown
            {
                JPanel mappingsPanel = new JPanel();
                mappingsPanel.setLayout(new BoxLayout(mappingsPanel, BoxLayout.X_AXIS));
                AreWePatchworkYetGui.mappingsBox = new JComboBox<>(MAPPINGS_LIST);
                AreWePatchworkYetGui.mappingsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                AreWePatchworkYetGui.mappingsBox.addActionListener(e -> {
                    String selected = (String) AreWePatchworkYetGui.mappingsBox.getSelectedItem();
                    if (selected.equals("yarn")) {
                        try {
                            MappingUtils.downloadYarnIfNeeded((String) AreWePatchworkYetGui.minecraftVersionBox.getSelectedItem());
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }

                    AreWePatchworkYetGui.renderNeededMethods();
                });

                mappingsPanel.add(new JLabel("Mappings to display in:"));
                mappingsPanel.add(AreWePatchworkYetGui.mappingsBox);
                mappingsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                configPanel.add(mappingsPanel);
            }

            // Analyze Button
            {
                JButton analyzeButton = new JButton("Analyze");

                analyzeButton.addActionListener(e -> {
                    try {
                        AreWePatchworkYet.start(
                                (String) AreWePatchworkYetGui.minecraftVersionBox.getSelectedItem(),
                                Paths.get(AreWePatchworkYetGui.inputModTextField.getText()),
                                Paths.get(AreWePatchworkYetGui.apiJarTextField.getText())
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

                AreWePatchworkYetGui.resultSearchTextField = new JTextField("Search classes...", 30);
                AreWePatchworkYetGui.resultSearchTextField.addActionListener(e -> {
                    AreWePatchworkYetGui.renderNeededMethods(AreWePatchworkYetGui.resultSearchTextField.getText());
                });
                infoPanel.add(AreWePatchworkYetGui.resultSearchTextField);

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
                            if (selectedItem.getType() == ResultListItem.Type.METHOD) {
                                inspectMethod((Method) selectedItem.getObject());
                            } else if (selectedItem.getType() == ResultListItem.Type.CLASS) {
                                inspectClass((String) selectedItem.getObject());
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
        AreWePatchworkYetGui.renderNeededMethods("");
    }

    public static void renderNeededMethods(String searchTerm) {
        int methodCount = 0;

        listModel.clear();
        for (Map.Entry<String, TreeSet<Method>> entry : Analyzer.neededMethods.entrySet()) {
            ResultListItem item = new ResultListItem(ResultListItem.Type.CLASS, entry.getKey());

            String keyName = MappingUtils.getClassName(entry.getKey());
            if (keyName.toLowerCase().contains(searchTerm.trim().toLowerCase()) || searchTerm.trim().isEmpty()) {
                listModel.addElement(item);

                for (Method method : entry.getValue()) {
                    methodCount++;
                    ResultListItem methodItem = new ResultListItem(ResultListItem.Type.METHOD, method);
                    listModel.addElement(methodItem);
                }
            }
        }

        System.out.println("Result method count: " + methodCount);
    }

    public static void inspectClass(String owner) {
        AreWePatchworkYetGui.inspectionPanel.removeAll();

        updateClass(owner);

        AreWePatchworkYetGui.inspectionPanel.updateUI();
    }

    private static int addClassHierarchy(String className, int indentationLevel) {
        int ret = 0;
        for (String superclass : Analyzer.superCache.getOrDefault(className, new HashSet<>())) {
            if (!superclass.equals("java/lang/Object")) {
                JLabel label = new JLabel(new String(new char[indentationLevel]).replace("\0", "    ") + "- " + MappingUtils.getClassName(superclass));
                AreWePatchworkYetGui.inspectionPanel.add(label);
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
            AreWePatchworkYetGui.inspectionPanel.add(label);
        }

        {
            JLabel label = new JLabel(MappingUtils.getClassName(owner));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            AreWePatchworkYetGui.inspectionPanel.add(label);
        }

        {
            JLabel label = new JLabel("implements or extends:");
            AreWePatchworkYetGui.inspectionPanel.add(label);
        }

        {
            int hierarchyAmount = addClassHierarchy(owner, 1);

            if (hierarchyAmount == 0) {
                JLabel label = new JLabel("Unknown or nothing");
                AreWePatchworkYetGui.inspectionPanel.add(label);
            }
        }
    }

    public static void inspectMethod(Method newMethod) {
        AreWePatchworkYetGui.inspectionPanel.removeAll();

        updateClass(newMethod.ownerClass);
        {
            JLabel label = new JLabel("Selected Method");
            label.setBorder(new EmptyBorder(20, 0, 0, 0));
            label.setFont(label.getFont().deriveFont(13f));
            label.setAlignmentY(Component.TOP_ALIGNMENT);

            AreWePatchworkYetGui.inspectionPanel.add(label);
        }
        JLabel label = new JLabel(newMethod.name + MappingUtils.getDescriptor(newMethod.descriptor));
        AreWePatchworkYetGui.inspectionPanel.add(label);
        AreWePatchworkYetGui.inspectionPanel.updateUI();
    }

    public static String getCurrentMappings() {
        return (String) AreWePatchworkYetGui.mappingsBox.getSelectedItem();
    }
}
