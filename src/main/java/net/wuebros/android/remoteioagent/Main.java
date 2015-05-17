package net.wuebros.android.remoteioagent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {

    private static final File CONFIG_FILE = new File("config.properties");

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
        }

        try {
            Config config = Config.createFromFile(CONFIG_FILE);
            Config.save(config);
            new MainWindow(config).setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read config file.");
        }
    }
}
