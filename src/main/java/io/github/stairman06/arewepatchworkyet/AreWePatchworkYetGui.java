package io.github.stairman06.arewepatchworkyet;

import io.github.stairman06.arewepatchworkyet.analyze.Analyzer;
import io.github.stairman06.arewepatchworkyet.analyze.Method;
import io.github.stairman06.arewepatchworkyet.ui.ResultListItem;
import io.github.stairman06.arewepatchworkyet.ui.ResultListItemCellRenderer;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeSet;

public class AreWePatchworkYetGui {
    private static final String[] MINECRAFT_VERSIONS = new String[] { "1.16.4" };

    private static JComboBox<String> minecraftVersionBox;
    private static JTextField inputModTextField;
    private static JTextField apiJarTextField;
    private static JPanel methodInfoPanel;
    private static Method selectedMethod;
    private static DefaultListModel<ResultListItem> listModel = new DefaultListModel<>();


    private static String getMappingClass(TinyTree tree, String classname) {
        for(ClassDef def : tree.getClasses()) {
            if(def.getName("intermediary").equals(classname)) {
                return def.getName("named");
            }
        }

        return "ERROR";
    }

    public static void main(String[] args) throws Exception {
        try {
            if(Files.exists(Paths.get("./temp"))) {
                FileUtils.deleteDirectory(new File("./temp"));
            }

            new File("./temp").mkdirs();
            if(!Files.exists(Paths.get("./data"))) {
                new File("./data").mkdirs();
            }
        }catch(Exception e) {
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
                    } catch(Exception ex) {
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

                resultsPanel.add(infoPanel);
            }

            // Result List
            {
                JList<ResultListItem> list = new JList<>(listModel);
                list.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(list.getSelectedValue() != null) {
                            ResultListItem selectedItem = list.getSelectedValue();
                            if(selectedItem.getType() == ResultListItem.Type.METHOD) {
                                updateMethod((Method) selectedItem.getObject());
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

            JPanel methodInfoPanel = new JPanel();
            methodInfoPanel.setLayout(new BoxLayout(methodInfoPanel, BoxLayout.Y_AXIS));
            methodInfoPanel.setMinimumSize(new Dimension(200, 0));

            JLabel selectALabel = new JLabel("Select a method or class");
            methodInfoPanel.add(selectALabel);

            containerPanel.add(methodInfoPanel, c);

            AreWePatchworkYetGui.methodInfoPanel = methodInfoPanel;
        }

        frame.add(containerPanel);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void renderNeededMethods() {
        for(Map.Entry<String, TreeSet<Method>> entry : Analyzer.neededMethods.entrySet()) {
            ResultListItem item = new ResultListItem(ResultListItem.Type.CLASS, entry.getKey());

            listModel.addElement(item);
            for(Method method : entry.getValue()) {
                ResultListItem methodItem = new ResultListItem(ResultListItem.Type.METHOD, method);
                listModel.addElement(methodItem);
            }
        }
    }

    public static void updateMethod(Method newMethod) {
        AreWePatchworkYetGui.methodInfoPanel.removeAll();

        {
            JLabel label = new JLabel("Selected Method");
            label.setFont(label.getFont().deriveFont(13f));
            label.setAlignmentY(Component.TOP_ALIGNMENT);

            AreWePatchworkYetGui.methodInfoPanel.add(label);
        }
        JLabel label = new JLabel(newMethod.name);
        AreWePatchworkYetGui.methodInfoPanel.add(label);
        AreWePatchworkYetGui.methodInfoPanel.updateUI();
    }
}
