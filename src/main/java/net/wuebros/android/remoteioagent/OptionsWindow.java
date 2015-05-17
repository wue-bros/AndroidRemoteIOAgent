package net.wuebros.android.remoteioagent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;

public class OptionsWindow extends JFrame {

    public OptionsWindow(final Config config) {
        super("Android Remote I/O Agent - Options");

        final JFileChooser chooser = new JFileChooser();

        JPanel container = new JPanel();
        container.setPreferredSize(new Dimension(450, 250));

        container.setBorder(new EmptyBorder(5, 5, 5, 5));
        container.setLayout(new GridLayout(10, 1));

        JLabel adbPathLabel = new JLabel("ADB path");
        final JTextField adbPathInput = new JTextField(config.getADBPath());

        adbPathInput.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setADBPath(adbPathInput.getText());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        JLabel deviceTempFileLabel = new JLabel("Temporary file (device)");
        final JTextField deviceTempFileInput = new JTextField(config.getDeviceTempFile());

        deviceTempFileInput.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setDeviceTempFile(deviceTempFileInput.getText());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        JLabel clientTempFileLabel = new JLabel("Temporary file (client)");
        final JTextField clientTempFileInput = new JTextField(config.getClientTempFile());

        clientTempFileInput.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setClientTempFile(clientTempFileInput.getText());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        SpinnerModel model = new SpinnerNumberModel(config.getLongPressDuration(), 1, null, 1);
        JLabel longPressDurationLabel = new JLabel("Long press duration (milliseconds)");
        final JSpinner longPressDurationInput = new JSpinner(model);

        longPressDurationInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                config.setLongPressDuration((int) longPressDurationInput.getValue());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        SpinnerModel scrollModel = new SpinnerNumberModel(config.getScrollSpeed(), 0, null, 1);
        JLabel scrollSpeedLabel = new JLabel("Scroll speed");
        final JSpinner scrollSpeedInput = new JSpinner(scrollModel);

        scrollSpeedInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                config.setScrollSpeed((int) scrollSpeedInput.getValue());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        JPanel adbPathPanel = new JPanel();
        adbPathPanel.setLayout(new BorderLayout());

        JButton adbPathBrowser = new JButton("...");
        adbPathBrowser.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setDialogTitle("Navigate to adb.exe");
                chooser.setFileFilter(new FileNameExtensionFilter("Executable (.exe)", "exe"));

                int result = chooser.showOpenDialog(OptionsWindow.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    adbPathInput.setText(chooser.getSelectedFile().getPath());
                }

                config.setADBPath(adbPathInput.getText());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        JPanel clientTempFilePanel = new JPanel();
        clientTempFilePanel.setLayout(new BorderLayout());

        JButton clientTempFileBrowser = new JButton("...");
        clientTempFileBrowser.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setDialogTitle("Select a folder to save the temporary image in.");
                chooser.setFileFilter(new FileNameExtensionFilter("PNG (.png)", "png"));

                int result = chooser.showSaveDialog(OptionsWindow.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    clientTempFileInput.setText(chooser.getSelectedFile().getPath());
                }

                config.setClientTempFile(clientTempFileInput.getText());
                try {
                    Config.save(config);
                } catch (IOException e1) {
                    throw new RuntimeException("Unable to write to properties file.");
                }
            }
        });

        clientTempFilePanel.add(clientTempFileInput, BorderLayout.CENTER);
        clientTempFilePanel.add(clientTempFileBrowser, BorderLayout.EAST);

        adbPathPanel.add(adbPathInput, BorderLayout.CENTER);
        adbPathPanel.add(adbPathBrowser, BorderLayout.EAST);

        container.add(adbPathLabel);
        container.add(adbPathPanel);

        container.add(deviceTempFileLabel);
        container.add(deviceTempFileInput);

        container.add(clientTempFileLabel);
        container.add(clientTempFilePanel);

        container.add(longPressDurationLabel);
        container.add(longPressDurationInput);

        container.add(scrollSpeedLabel);
        container.add(scrollSpeedInput);

        add(container);

        pack();
    }
}
