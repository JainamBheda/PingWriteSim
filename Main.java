import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Main extends JFrame {

    private JTextField hostInput;
    private JButton pingButton;
    private JTextArea pingResultArea;

    private JTextArea codeEditorArea;
    private JButton compileRunButton;
    private JTextArea codeOutputArea;

    public Main() {
        setTitle("Multi-Tool: Ping + Java Editor + ARP");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Ping Tester", createPingPanel());
        tabbedPane.addTab("Java Code Editor", createCodeEditorPanel());
        tabbedPane.addTab("ARP Simulator", createARPPanel());

        applyDarkTheme(tabbedPane);
        add(tabbedPane);
    }

    // 🛰️ Ping Panel
    private JPanel createPingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new FlowLayout());

        hostInput = new JTextField(20);
        pingButton = new JButton("Ping");

        topPanel.add(new JLabel("Enter Host/IP:"));
        topPanel.add(hostInput);
        topPanel.add(pingButton);

        pingResultArea = new JTextArea();
        pingResultArea.setEditable(false);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(pingResultArea), BorderLayout.CENTER);

        pingButton.addActionListener(e -> {
            String host = hostInput.getText().trim();
            if (host.isEmpty()) {
                pingResultArea.setText("Please enter a host name or IP address.");
                return;
            }

            try {
                pingResultArea.setText("Pinging " + host + "...\n");
                InetAddress inet = InetAddress.getByName(host);
                long start = System.currentTimeMillis();
                boolean reachable = inet.isReachable(3000);
                long end = System.currentTimeMillis();

                if (reachable) {
                    pingResultArea.append("Host is reachable.\nResponse time: " + (end - start) + " ms\n");
                } else {
                    pingResultArea.append("Host is NOT reachable.\n");
                }
            } catch (IOException ex) {
                pingResultArea.setText("Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    // 💻 Java Code Editor Panel
    private JPanel createCodeEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        codeEditorArea = new JTextArea(
                "import java.util.*;\n\npublic class Main {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Hello from editor!\");\n" +
                        "    }\n" +
                        "}"
        );
        compileRunButton = new JButton("Compile & Run");
        codeOutputArea = new JTextArea();
        codeOutputArea.setEditable(false);

        panel.add(compileRunButton, BorderLayout.NORTH);
        panel.add(new JScrollPane(codeEditorArea), BorderLayout.CENTER);
        panel.add(new JScrollPane(codeOutputArea), BorderLayout.SOUTH);

        compileRunButton.addActionListener(e -> compileAndRunJava());

        return panel;
    }

    private void compileAndRunJava() {
        try {
            String code = codeEditorArea.getText();
            String fileName = "Main.java";

            FileWriter writer = new FileWriter(fileName);
            writer.write(code);
            writer.close();

            Process compile = Runtime.getRuntime().exec("javac " + fileName);
            compile.waitFor();

            if (compile.exitValue() != 0) {
                codeOutputArea.setText("Compilation failed:\n");
                try (BufferedReader error = new BufferedReader(new InputStreamReader(compile.getErrorStream()))) {
                    error.lines().forEach(line -> codeOutputArea.append(line + "\n"));
                }
                return;
            }

            Process run = Runtime.getRuntime().exec("java Main");
            BufferedReader output = new BufferedReader(new InputStreamReader(run.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(run.getErrorStream()));

            StringBuilder result = new StringBuilder("Output:\n");
            output.lines().forEach(line -> result.append(line).append("\n"));
            error.lines().forEach(line -> result.append("Error: ").append(line).append("\n"));

            codeOutputArea.setText(result.toString());

        } catch (Exception ex) {
            codeOutputArea.setText("Error: " + ex.getMessage());
        }
    }

    // 🔁 ARP Simulator Panel
    private JPanel createARPPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        java.util.List<String[]> arpTable = new java.util.ArrayList<>();
        arpTable.add(new String[]{"192.168.1.1", "00-AA-BB-CC-DD-01"});
        arpTable.add(new String[]{"192.168.1.2", "00-AA-BB-CC-DD-02"});
        arpTable.add(new String[]{"192.168.1.3", "00-AA-BB-CC-DD-03"});
        arpTable.add(new String[]{"192.168.1.4", "00-AA-BB-CC-DD-04"});

        JComboBox<String> sourceCombo = new JComboBox<>();
        JComboBox<String> targetCombo = new JComboBox<>();
        JTextArea arpResultArea = new JTextArea();
        arpResultArea.setEditable(false);

        Runnable refreshDropdowns = () -> {
            sourceCombo.removeAllItems();
            targetCombo.removeAllItems();
            for (String[] row : arpTable) {
                sourceCombo.addItem(row[0]);
                targetCombo.addItem(row[0]);
            }
        };
        refreshDropdowns.run();

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Source IP:"));
        topPanel.add(sourceCombo);
        topPanel.add(new JLabel("Destination IP:"));
        topPanel.add(targetCombo);

        JButton simulateButton = new JButton("Simulate ARP Request");
        topPanel.add(simulateButton);

        JPanel addPanel = new JPanel(new FlowLayout());
        JTextField ipInput = new JTextField(10);
        JTextField macInput = new JTextField(15);
        JButton addDeviceButton = new JButton("Add Device");

        addPanel.add(new JLabel("New IP:"));
        addPanel.add(ipInput);
        addPanel.add(new JLabel("MAC:"));
        addPanel.add(macInput);
        addPanel.add(addDeviceButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(addPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(arpResultArea), BorderLayout.CENTER);

        simulateButton.addActionListener(e -> {
            String srcIP = (String) sourceCombo.getSelectedItem();
            String dstIP = (String) targetCombo.getSelectedItem();

            if (srcIP == null || dstIP == null) {
                arpResultArea.setText("Please select valid IPs.");
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append("Source [" + srcIP + "] sends ARP Request: Who has " + dstIP + "?\n");

            boolean found = false;
            for (String[] row : arpTable) {
                if (row[0].equals(dstIP)) {
                    result.append("Device [" + dstIP + "] replies: My MAC is " + row[1] + "\n\n");
                    result.append("=== ARP Table at Source [" + srcIP + "] ===\n");
                    result.append("IP Address\tMAC Address\n");
                    result.append(dstIP + "\t" + row[1] + "\n");
                    found = true;
                    break;
                }
            }

            if (!found) {
                result.append("No device responded to the ARP request.\n");
            }

            arpResultArea.setText(result.toString());
        });

        addDeviceButton.addActionListener(e -> {
            String newIP = ipInput.getText().trim();
            String newMAC = macInput.getText().trim();

            if (newIP.isEmpty() || newMAC.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter both IP and MAC address.");
                return;
            }

            for (String[] row : arpTable) {
                if (row[0].equals(newIP)) {
                    JOptionPane.showMessageDialog(panel, "IP already exists.");
                    return;
                }
            }

            arpTable.add(new String[]{newIP, newMAC});
            refreshDropdowns.run();
            ipInput.setText("");
            macInput.setText("");
            arpResultArea.setText("Device [" + newIP + "] with MAC [" + newMAC + "] added.\n");
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    // 🎨 Apply Dark Theme
    private void applyDarkTheme(Component comp) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(new Color(40, 44, 52));
        }

        if (comp instanceof JLabel || comp instanceof JComboBox) {
            comp.setForeground(Color.WHITE);
        }

        if (comp instanceof JTextArea || comp instanceof JTextField) {
            comp.setBackground(new Color(30, 30, 30));
            comp.setForeground(Color.GREEN);
            comp.setFont(new Font("Consolas", Font.PLAIN, 14));
        }

        if (comp instanceof JButton) {
            comp.setBackground(new Color(60, 63, 65));
            comp.setForeground(Color.WHITE);
        }

        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyDarkTheme(child);
            }
        }
    }

    // 🚀 Main Method
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
