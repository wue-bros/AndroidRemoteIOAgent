package net.wuebros.android.remoteioagent;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenPollingThread {

    private final ADB adb;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(true);

    private final JComboBox<String> deviceSelector;
    private final JLabel infoLabel;
    private final JLabel fpsLabel;
    private final ScreenPanel screenPanel;

    public ScreenPollingThread(ADB adb, JComboBox<String> deviceSelector, JLabel infoLabel, JLabel fpsLabel, ScreenPanel screenPanel) {
        this.adb = adb;
        this.deviceSelector = deviceSelector;
        this.infoLabel = infoLabel;
        this.screenPanel = screenPanel;
        this.fpsLabel = fpsLabel;
    }

    public void ensureStarted() {
        if (!shutdownRequested.getAndSet(false)) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean wasConnected = false;

                while (true) {
                    if (shutdownRequested.get()) return;

                    long startTime = System.currentTimeMillis();

                    String deviceID = (String) deviceSelector.getSelectedItem();

                    if (deviceID == null) {
                        try {
                            infoLabel.setText("no device detected");
                            fpsLabel.setText("");
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            return;
                        }

                        continue;
                    }

                    BufferedImage image = adb.screencap(deviceID);
                    screenPanel.setImage(image);

                    if (image == null) {
                        infoLabel.setText("device not found: " + deviceID);
                        fpsLabel.setText("");
                        wasConnected = false;
                        continue;
                    } else if (!wasConnected) {
                        wasConnected = true;
                        infoLabel.setText("device connected: " + deviceID);
                    }

                    float fps = 1f / ((System.currentTimeMillis() - startTime) / 1000f);
                    fpsLabel.setText(String.format(Locale.US, "%2.1f FPS", fps));
                }
            }
        }).start();
    }

    public void requestShutdown() {
        shutdownRequested.set(true);
    }
}