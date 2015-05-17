package net.wuebros.android.remoteioagent;

import java.io.*;
import java.util.Properties;

public class Config {

    private File file;
    private Properties properties = new Properties();

    public static Config createFromFile(File file) throws IOException {
        Config config = new Config();
        config.file = file;

        try {
            config.properties.load(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            // ignore
        }

        return config;
    }

    public static void save(Config config) throws IOException {
        save(config, null);
    }

    public static void save(Config config, File file) throws IOException {
        File actualFile = file;
        if (actualFile == null) actualFile = config.file;
        if (actualFile == null) throw new IllegalArgumentException("missing file");
        config.properties.store(new FileOutputStream(actualFile), null);
    }

    public Config() {
        properties.setProperty("adb_path", "adb");
        properties.setProperty("device_temp_file", "/sdcard/AndroidRemoteIOAgent.png");
        properties.setProperty("client_temp_file", "AndroidRemoteIOAgent.png");
        properties.setProperty("long_press_duration", "750");
        properties.setProperty("scroll_speed", "30");
    }

    public int getScrollSpeed() {
        return Integer.parseInt(properties.getProperty("scroll_speed"));
    }

    public String getADBPath() {
        return properties.getProperty("adb_path");
    }

    public String getDeviceTempFile() {
        return properties.getProperty("device_temp_file");
    }

    public String getClientTempFile() {
        return properties.getProperty("client_temp_file");
    }

    public int getLongPressDuration() {
        return Integer.parseInt(properties.getProperty("long_press_duration"));
    }

    public void setADBPath(String path) {
        properties.setProperty("adb_path", path);
    }

    public void setScrollSpeed(int speed) {
        properties.setProperty("scroll_speed", String.valueOf(speed));
    }

    public void setDeviceTempFile(String path) {
        properties.setProperty("device_temp_file", path);
    }

    public void setClientTempFile(String path) {
        properties.setProperty("client_temp_file", path);
    }

    public void setLongPressDuration(int duration) {
        properties.setProperty("long_press_duration", String.valueOf(duration));
    }
}
