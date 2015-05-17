package net.wuebros.android.remoteioagent;

import javax.swing.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SendTextThread extends Thread {

    private static class Key {
        public final Integer keyCode;
        public final Character keyChar;

        public Key(int keyCode) {
            this.keyCode = keyCode;
            keyChar = null;
        }

        public Key(char keyChar) {
            this.keyChar = keyChar;
            keyCode = null;
        }
    }

    private final ADB adb;
    private final JComboBox<String> deviceSelector;

    private final Queue<Key> keys = new ConcurrentLinkedQueue<>();
    private final Object lock = new Object();

    public SendTextThread(ADB adb, JComboBox<String> deviceSelector) {
        this.adb = adb;
        this.deviceSelector = deviceSelector;
    }

    public void queueKey(int keyCode) {
        synchronized (lock) {
            keys.add(new Key(keyCode));
            lock.notify();
        }
    }

    public void queueKey(char keyChar) {
        synchronized (lock) {
            keys.add(new Key(keyChar));
            lock.notify();
        }
    }

    @Override
    public void run() {
        while (true) {
            final Key firstKey = keys.poll();

            // Handle empty queue.
            if (firstKey == null) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }

                    continue;
                } catch (InterruptedException e) {
                    return;
                }
            }

            // Handle actual keys.
            if (firstKey.keyCode != null) {
                adb.sendKey((String) deviceSelector.getSelectedItem(), firstKey.keyCode);
                continue;
            }

            // Handle text. Combining key chars to a string.
            if (firstKey.keyChar != null) {
                final StringBuilder builder = new StringBuilder();
                builder.append(firstKey.keyChar);

                while (true) {
                    final Key key = keys.peek();

                    if (key != null && key.keyChar != null) {
                        keys.remove();
                        builder.append(key.keyChar);
                    } else {
                        break;
                    }
                }

                adb.sendText((String) deviceSelector.getSelectedItem(), builder.toString());
            }
        }
    }
}
