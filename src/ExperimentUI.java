import java.awt.*;
import java.io.File;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ExperimentUI extends JFrame implements ExperimentModeController.StateObserver {

    private final ExperimentModeController ctrl = new ExperimentModeController();
    private final LogBuffer logBuffer = new LogBuffer(20);

    private JLabel statusLabel, descLabel;
    private JPanel behaviorsPanel;
    private DefaultListModel<String> historyModel;
    private DefaultListModel<String> trafficModel;
    private JButton undoBtn, redoBtn;

    public ExperimentUI() {
        setTitle("Real-Time Software Mode & Policy Engine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 620);
        setLocationRelativeTo(null);

        ctrl.addObserver(this);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(new Color(20, 20, 26));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildSidePanel(), BorderLayout.EAST);

        setContentPane(root);
        onUpdate();
        setVisible(true);

        TrafficSimulator simulator = new TrafficSimulator(ctrl, logBuffer);
        simulator.start();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setBackground(new Color(20, 20, 26));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleRow.setBackground(new Color(20, 20, 26));

        statusLabel = new JLabel();
        descLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        titleRow.add(statusLabel);
        titleRow.add(descLabel);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(new Color(20, 20, 26));

        for (Mode m : Mode.values()) {
            JButton btn = new JButton(m.getLabel());
            btn.addActionListener(e -> ctrl.switchMode(m));
            toolbar.add(btn);
        }

        JButton exportBtn = new JButton("Export Config");
        exportBtn.addActionListener(e -> handleExport());
        toolbar.add(exportBtn);

        JButton importBtn = new JButton("Import Config");
        importBtn.addActionListener(e -> handleImport());
        toolbar.add(importBtn);

        header.add(titleRow, BorderLayout.NORTH);
        header.add(toolbar, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildCenter() {
        behaviorsPanel = new JPanel();
        behaviorsPanel.setLayout(new BoxLayout(behaviorsPanel, BoxLayout.Y_AXIS));
        behaviorsPanel.setBackground(new Color(30, 30, 40));
        behaviorsPanel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 65)));

        JScrollPane scroll = new JScrollPane(behaviorsPanel);
        scroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(20, 20, 26));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildSidePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
        panel.setPreferredSize(new Dimension(360, 0));
        panel.setBackground(new Color(20, 20, 26));

        historyModel = new DefaultListModel<>();
        JList<String> histList = new JList<>(historyModel);
        JPanel histBox = new JPanel(new BorderLayout(0, 4));
        histBox.setBackground(new Color(20, 20, 26));
        JLabel histTitle = new JLabel("Audit Log (Command Stack)");
        histTitle.setForeground(Color.LIGHT_GRAY);
        histBox.add(histTitle, BorderLayout.NORTH);
        histBox.add(new JScrollPane(histList), BorderLayout.CENTER);

        undoBtn = new JButton("Undo");
        redoBtn = new JButton("Redo");
        undoBtn.addActionListener(e -> ctrl.undo());
        redoBtn.addActionListener(e -> ctrl.redo());

        JPanel actions = new JPanel(new GridLayout(1, 2, 6, 0));
        actions.add(undoBtn);
        actions.add(redoBtn);
        histBox.add(actions, BorderLayout.SOUTH);

        trafficModel = new DefaultListModel<>();
        JList<String> logList = new JList<>(trafficModel);
        logList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JPanel logBox = new JPanel(new BorderLayout(0, 4));
        logBox.setBackground(new Color(20, 20, 26));
        JLabel logTitle = new JLabel("Simulated Runtime Traffic (Worker Thread)");
        logTitle.setForeground(Color.LIGHT_GRAY);
        logBox.add(logTitle, BorderLayout.NORTH);
        logBox.add(new JScrollPane(logList), BorderLayout.CENTER);

        panel.add(histBox);
        panel.add(logBox);
        return panel;
    }

    private void handleExport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ctrl.exportProfile(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Configuration successfully exported!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleImport() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ctrl.importProfile(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Configuration successfully loaded!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onUpdate() {
        SwingUtilities.invokeLater(() -> {
            Mode mode = ctrl.getCurrentMode();
            String safety = ctrl.getCurrentConfig().getSafetyStatus();

            statusLabel.setText(mode.getLabel());
            statusLabel.setForeground(Color.WHITE);
            descLabel.setText(" - " + mode.getDescription() + " | Safety: " + safety);

            if (safety.startsWith("UNSAFE")) {
                descLabel.setForeground(new Color(255, 90, 90));
            } else if (safety.startsWith("RISK")) {
                descLabel.setForeground(new Color(255, 180, 70));
            } else {
                descLabel.setForeground(new Color(100, 220, 140));
            }

            renderBehaviors();

            historyModel.clear();
            for (Command cmd : ctrl.getUndoStack()) {
                historyModel.addElement(cmd.getDescription());
            }

            trafficModel.clear();
            for (String log : logBuffer.getAll()) {
                trafficModel.addElement(log);
            }

            undoBtn.setEnabled(ctrl.canUndo());
            redoBtn.setEnabled(ctrl.canRedo());
        });
    }

    private void renderBehaviors() {
        behaviorsPanel.removeAll();
        for (Map.Entry<String, Behavior> entry : ctrl.getCurrentConfig().getBehaviors().entrySet()) {
            String name = entry.getKey();
            Behavior b = entry.getValue();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(new Color(30, 30, 40));
            row.setBorder(new EmptyBorder(8, 10, 8, 10));

            JLabel label = new JLabel(name + " (" + b.getValueDescription() + ")");
            label.setForeground(Color.WHITE);

            JToggleButton toggle = new JToggleButton(b.isEnabled() ? "ON" : "OFF");
            toggle.setSelected(b.isEnabled());
            toggle.setBackground(b.isEnabled() ? new Color(35, 145, 95) : new Color(170, 45, 45));
            toggle.setForeground(Color.WHITE);
            toggle.setFocusPainted(false);

            toggle.addActionListener(e -> {
                boolean ok = ctrl.toggleBehavior(name);
                if (!ok) {
                    JOptionPane.showMessageDialog(this,
                            "Operation blocked under STABLE safety constraints.",
                            "Security Boundary",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

            row.add(label, BorderLayout.CENTER);
            row.add(toggle, BorderLayout.EAST);
            behaviorsPanel.add(row);
        }
        behaviorsPanel.revalidate();
        behaviorsPanel.repaint();
    }
}