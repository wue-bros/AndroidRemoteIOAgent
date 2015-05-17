package net.wuebros.android.remoteioagent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.apache.commons.exec.*;

public class ADB {

    private interface CommandExecutionListener<T> {
        public T onCommand(InputStream stderr, InputStream stdout);
    }

    private Config config;

    public ADB(Config config) {
        this.config = config;
    }

    public List<String> listDevices() {
        try {
            return exec(new String[]{"devices"}, new CommandExecutionListener<List<String>>() {
                @Override
                public List<String> onCommand(InputStream stderr, InputStream stdout) {
                    List<String> devices = new ArrayList<>();

                    BufferedReader out = new BufferedReader(new InputStreamReader(stdout));

                    try {
                        boolean headerFound = false;
                        String line;

                        while ((line = out.readLine()) != null) {
                            if (line.trim().equals("List of devices attached")) {
                                headerFound = true;
                                continue;
                            }

                            if (!headerFound) continue;
                            if (line.trim().isEmpty()) continue;

                            devices.add(line.split("\\s+")[0]);
                        }
                    } catch (IOException e) {
                        return null;
                    }

                    return devices;
                }
            });
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public BufferedImage screencap(String deviceID) {
        try {
            exec(new String[]{"-s", deviceID, "shell", "screencap", "-p", config.getDeviceTempFile()}, null);
            exec(new String[]{"-s", deviceID, "pull", config.getDeviceTempFile(), config.getClientTempFile()}, null);
            BufferedImage image = ImageIO.read(new File(config.getClientTempFile()));
            new File(config.getClientTempFile()).delete();
            return image;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public void sendText(String deviceID, String text) {
        try {
            exec(new String[]{"-s", deviceID, "shell", "input", "text", text}, null);
        } catch (IOException | InterruptedException e) {
            return;
        }
    }

    public void sendKey(String deviceID, int key) {
        try {
            exec(new String[]{"-s", deviceID, "shell", "input", "keyevent", String.valueOf(key)}, null);
        } catch (IOException | InterruptedException e) {
            return;
        }
    }

    public void sendTap(String deviceID, Point position) {
        try {
            exec(new String[]{"-s", deviceID, "shell", "input", "tap", String.valueOf(position.x), String.valueOf(position.y)}, null);
            System.out.println("Sent tap at " + position.x + "x" + position.y);
        } catch (IOException | InterruptedException e) {
            return;
        }
    }

    public void sendSwipe(String deviceID, Point start, Point target, long duration) {
        try {
            exec(new String[]{"-s", deviceID, "shell", "input", "touchscreen", "swipe", start.x + "", start.y + "", target.x + "", target.y + "", duration + ""}, null);
            System.out.println("Sent swipe from " + start.x + "x" + start.y + " to " + target.x + "x" + target.y);
        } catch (IOException | InterruptedException e) {
            return;
        }
    }

    public void sendLongPress(String deviceID, Point position) {
        sendSwipe(deviceID, position, position, config.getLongPressDuration());
    }

    private <T> T exec(String[] command, final CommandExecutionListener<T> listener) throws IOException, InterruptedException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cmdLine = new CommandLine(config.getADBPath());
        cmdLine.addArguments(command);

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        PumpStreamHandler streamHandler = new PumpStreamHandler(stdout, stderr);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(streamHandler);
        executor.execute(cmdLine, resultHandler);
        streamHandler.start();
        resultHandler.waitFor();

        if (listener == null) return null;
        return listener.onCommand(new ByteArrayInputStream(stderr.toByteArray()), new ByteArrayInputStream(stdout.toByteArray()));
    }
}
