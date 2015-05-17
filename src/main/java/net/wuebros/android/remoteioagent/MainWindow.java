package net.wuebros.android.remoteioagent;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainWindow extends JFrame {

    private final ADB adb;

    private final ScreenPanel screenPanel;
    private final JComboBox<String> deviceSelector = new JComboBox<>();
    private final JLabel infoLabel;

    private final OptionsWindow optionsWindow;

    public MainWindow(Config config) throws HeadlessException {
        super("AndroidRemoteIOAgent");

        adb = new ADB(config);

        setMinimumSize(new Dimension(300, 200));
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());

        infoLabel = new JLabel(" "); // force height
        infoLabel.setBorder(new EmptyBorder(2, 4, 2, 2));

        JLabel fpsLabel = new JLabel();
        fpsLabel.setBorder(new EmptyBorder(2, 2, 2, 4));

        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.add(fpsLabel, BorderLayout.EAST);

        final JPanel devicePanel = new JPanel();
        devicePanel.setLayout(new BorderLayout());

        deviceSelector.setEditable(false);

        for (String deviceID : adb.listDevices()) {
            deviceSelector.addItem(deviceID);
        }

        deviceSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                screenPanel.requestFocus();
            }
        });

        screenPanel = new ScreenPanel(adb, config, deviceSelector, infoLabel);

        final JPanel screenPanelBorder = new JPanel();
        screenPanelBorder.setLayout(new BorderLayout());
        screenPanelBorder.add(screenPanel);

        screenPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                screenPanelBorder.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.RED));
            }

            @Override
            public void focusLost(FocusEvent e) {
                screenPanelBorder.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.LIGHT_GRAY));
            }
        });

        JButton refreshDevices = new JButton();
        refreshDevices.setLayout(new BorderLayout());

        refreshDevices.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoLabel.setText("refreshing connected devices...");
                deviceSelector.removeAllItems();

                for (String deviceID : adb.listDevices()) {
                    deviceSelector.addItem(deviceID);
                }

                infoLabel.setText(" ");
                screenPanel.requestFocus();
            }
        });

        ImageIcon refreshIcon = new ImageIcon(getClass().getClassLoader().getResource("refresh.png"));
        JLabel refreshLabel = new JLabel(refreshIcon);
        refreshDevices.add(refreshLabel);
        refreshDevices.setPreferredSize(new Dimension(deviceSelector.getPreferredSize().height, deviceSelector.getPreferredSize().height));
        refreshDevices.setMargin(new Insets(1, 1, 1, 1));
        refreshDevices.setToolTipText("refresh list of connected devices");

        int iconSize = (int) (refreshDevices.getPreferredSize().height * 0.75);

        refreshLabel.setIcon(new ImageIcon(refreshIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH)));

        JButton optionsButton = new JButton("Options");
        optionsButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionsWindow.setVisible(true);
            }
        });

        devicePanel.add(optionsButton, BorderLayout.WEST);
        devicePanel.add(deviceSelector, BorderLayout.CENTER);
        devicePanel.add(refreshDevices, BorderLayout.EAST);

        JPanel extraPanel = new JPanel();
        extraPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        JPanel softKeys = new JPanel();
        softKeys.setLayout(new GridLayout());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 4);
                screenPanel.requestFocus();
            }
        });

        JButton homeButton = new JButton("Home");
        homeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 3);
                screenPanel.requestFocus();
            }
        });

        JButton tasksButton = new JButton("Tasks");
        tasksButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 187);
                screenPanel.requestFocus();
            }
        });

        softKeys.add(backButton);
        softKeys.add(homeButton);
        softKeys.add(tasksButton);

        JButton powerButton = new JButton("Power");
        powerButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 26);
                screenPanel.requestFocus();
            }
        });

        JButton menuButton = new JButton("Menu");
        menuButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 82);
                screenPanel.requestFocus();
            }
        });

        JButton volumeUpButton = new JButton("Volume Up");
        volumeUpButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 24);
                screenPanel.requestFocus();
            }
        });

        JButton volumeDownButton = new JButton("Volume Down");
        volumeDownButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), 25);
                screenPanel.requestFocus();
            }
        });

        JPanel systemKeys = new JPanel();
        systemKeys.setLayout(new GridLayout());

        systemKeys.add(powerButton);
        systemKeys.add(menuButton);
        systemKeys.add(volumeUpButton);
        systemKeys.add(volumeDownButton);

        buttonPanel.add(softKeys, BorderLayout.NORTH);
        buttonPanel.add(systemKeys, BorderLayout.CENTER);

        extraPanel.add(buttonPanel, BorderLayout.NORTH);
        extraPanel.add(infoPanel, BorderLayout.SOUTH);

        add(devicePanel, BorderLayout.NORTH);
        add(screenPanelBorder, BorderLayout.CENTER);
        add(extraPanel, BorderLayout.SOUTH);

        pack();

        final ScreenPollingThread pollingThread = new ScreenPollingThread(adb, deviceSelector, infoLabel, fpsLabel, screenPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pollingThread.requestShutdown();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                pollingThread.ensureStarted();
            }
        });

        optionsWindow = new OptionsWindow(config);

        screenPanel.requestFocus();
    }
}
