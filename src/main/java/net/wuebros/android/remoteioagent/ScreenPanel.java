package net.wuebros.android.remoteioagent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Created by Benedikt on 16.05.2015.
 */
public class ScreenPanel extends JPanel {

    private boolean wasNull = false;

    private BufferedImage originalDeviceImage;
    private Image scaledDeviceImage;

    private int viewportWidth = 0;
    private int viewportHeight = 0;

    private final SendTextThread textThread;

    public ScreenPanel(final ADB adb, final Config config, final JComboBox<String> deviceSelector, final JLabel infoLabel) {
        setFocusable(true);
        setDoubleBuffered(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                update();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!hasFocus()) return;

                char c = e.getKeyChar();
                if (c > 32 && c < 128) textThread.queueKey(c);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        textThread.queueKey(66);
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        textThread.queueKey(67);
                        break;
                    case KeyEvent.VK_UP:
                        textThread.queueKey(19);
                        break;
                    case KeyEvent.VK_DOWN:
                        textThread.queueKey(20);
                        break;
                    case KeyEvent.VK_LEFT:
                        textThread.queueKey(21);
                        break;
                    case KeyEvent.VK_RIGHT:
                        textThread.queueKey(22);
                        break;
                    case KeyEvent.VK_SPACE:
                        textThread.queueKey(62);
                        break;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            private long startPressTime = 0;
            private Point startPressPosition;

            @Override
            public void mouseReleased(MouseEvent e) {
                long timePressed = System.currentTimeMillis() - startPressTime;
                String deviceID = (String) deviceSelector.getSelectedItem();
                Point position = unproject(e.getPoint());

                // Check for swipe.
                if (!startPressPosition.equals(unproject(e.getPoint()))) {
                    adb.sendSwipe(deviceID, startPressPosition, position, timePressed);
                    infoLabel.setText("sent " + timePressed + "ms swipe from " + startPressPosition.x + "x" + startPressPosition.y + " to " + position.x + "x" + position.y);
                    return;
                }

                // Check if single tap or long press.
                if (timePressed < 300) {
                    adb.sendTap(deviceID, position);
                    infoLabel.setText("sent tap at " + position.x + "x" + position.y);
                    return;
                }

                adb.sendLongPress(deviceID, position);
                infoLabel.setText("sent " + config.getLongPressDuration() + "ms long press at " + position.x + "x" + position.y);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                startPressTime = System.currentTimeMillis();
                startPressPosition = unproject(e.getPoint());
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point startPoint = unproject(e.getPoint());

                // When the scrolled distance is too small android will interpret it as a tap instead, so we'll just
                // need to ensure, that we are not tapping by accident.
                final int MIN_SCROLL_DISTANCE = 25;
                int scrollDistance = e.getUnitsToScroll() * config.getScrollSpeed();

                if (scrollDistance > 0) {
                    scrollDistance = Math.max(MIN_SCROLL_DISTANCE, scrollDistance);
                } else if (scrollDistance < 0) {
                    scrollDistance = Math.min(-MIN_SCROLL_DISTANCE, scrollDistance);
                } else {
                    return;
                }

                adb.sendSwipe((String) deviceSelector.getSelectedItem(),
                        startPoint,
                        new Point(startPoint.x, startPoint.y - scrollDistance),
                        50);
            }
        });

        Timer updateTimer = new Timer(16, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });

        updateTimer.setRepeats(true);
        updateTimer.setInitialDelay(0);
        updateTimer.start();

        textThread = new SendTextThread(adb, deviceSelector);
        textThread.start();
    }

    private Point unproject(Point point) {
        if (originalDeviceImage == null) return null;
        return new Point((int) (originalDeviceImage.getWidth() / (float) viewportWidth * point.x), (int) (originalDeviceImage.getHeight() / (float) viewportHeight * point.y));
    }

    public synchronized void setImage(BufferedImage image) {
        if (originalDeviceImage == null) wasNull = true;
        this.originalDeviceImage = image;
    }

    private void update() {
        if (originalDeviceImage == null) return;
        float ratio = originalDeviceImage.getWidth() / (float) originalDeviceImage.getHeight();

        if (wasNull) {
            wasNull = false;
            setPreferredSize(new Dimension(getWidth(), (int) (getWidth() / ratio)));
            SwingUtilities.getWindowAncestor(this).pack();
        }

        int maxWidth = getWidth();
        int maxHeight = getHeight();

        if (maxHeight * ratio > maxWidth) {
            viewportHeight = (int) (maxWidth / ratio);
            viewportWidth = maxWidth;
        } else {
            viewportHeight = maxHeight;
            viewportWidth = (int) (maxHeight * ratio);
        }

        scaledDeviceImage = originalDeviceImage.getScaledInstance(viewportWidth, viewportHeight, BufferedImage.SCALE_SMOOTH);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (scaledDeviceImage != null) g.drawImage(scaledDeviceImage, 0, 0, null);
    }
}
