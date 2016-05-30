package frontend;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import backend.Log;

/**
 * Represents panel which allows to resize by clicking
 */
public class ZoomableImagePanelWrapper extends JPanel {

    private final static Logger log = Log.getLogger();
    private final static double ZOOM_DELTA = 0.05, INITIAL_ZOOM_FACTOR = 1.0,
            MIN_ZOOM = 0.01, MAX_ZOOM = 100.0;
    private final static int TIMER_DELAY = 50;
    private final Timer zoomTimer;
    private final JButton zoomInButton, zoomOutButton, restoreOriginalButton;
    private final JScrollPane scrollPaneImageZoom;
    private final JPanel panelForCenteringInScrollPane;
    private final ImageDetailsPanel detailsPanel;
    private final ZoomableImagePanel zoomableImagePanel;
    private double zoomFactor = INITIAL_ZOOM_FACTOR;
    private final ButtonModel zoomInButtonModel, zoomOutButtonModel;

    public ZoomableImagePanelWrapper(ImageDetailsPanel detailsPanel) {
        zoomTimer = new Timer(TIMER_DELAY, new ZoomButtonTimerActionListener());
        this.detailsPanel = detailsPanel;

        setLayout(new BorderLayout());

        zoomableImagePanel = new ZoomableImagePanel();
        zoomableImagePanel.setToolTipText("Preview");

        panelForCenteringInScrollPane = new JPanel(new GridBagLayout());
        panelForCenteringInScrollPane.add(zoomableImagePanel);
        scrollPaneImageZoom = new JScrollPane(panelForCenteringInScrollPane);
        add(scrollPaneImageZoom, BorderLayout.CENTER);

        final ZoomButtonChangeListener zoomChangeListener = new ZoomButtonChangeListener();
        zoomInButton = new JButton("+");
        zoomInButton.setToolTipText("Zoom in");
        zoomInButtonModel = zoomInButton.getModel();
        zoomInButtonModel.addChangeListener(zoomChangeListener);

        zoomOutButton = new JButton("-");
        zoomOutButton.setToolTipText("Zoom out");
        zoomOutButtonModel = zoomOutButton.getModel();
        zoomOutButtonModel.addChangeListener(zoomChangeListener);

        final ZoomButtonActionListener zoomActionListener = new ZoomButtonActionListener();
        restoreOriginalButton = new JButton("Original");
        restoreOriginalButton.addActionListener(zoomActionListener);
        restoreOriginalButton.setToolTipText("Restore original image size");

        final JPanel bottomZoomInOutPanel = new JPanel(new GridLayout());
        bottomZoomInOutPanel.add(zoomInButton);
        bottomZoomInOutPanel.add(restoreOriginalButton);
        bottomZoomInOutPanel.add(zoomOutButton);

        add(bottomZoomInOutPanel, BorderLayout.SOUTH);
    }

    public void setImage(BufferedImage bufImg) {
        zoomableImagePanel.setImage(bufImg);
        setZoom(INITIAL_ZOOM_FACTOR);
        scrollPaneImageZoom.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPaneImageZoom.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        repaint();
    }

    public void clearImage() {
        zoomableImagePanel.clearImage();
        scrollPaneImageZoom.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneImageZoom.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    }

    private void setZoom(double zoom) {
        zoomFactor = zoom;
        if (zoomableImagePanel.setScaledImage(zoomFactor)
                && detailsPanel != null) {
            detailsPanel.setZoom(zoomFactor);
        }
    }

    private class ZoomButtonTimerActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            double d;
            if (e.getSource() != zoomTimer) {
                return;
            }
            if (zoomInButtonModel.isPressed()) {
                d = ZOOM_DELTA;
            } else if (zoomOutButtonModel.isPressed()) {
                d = -ZOOM_DELTA;
            } else {
                log.warning("Unexpected timer event");
                return;
            }
            final double newZoomFactor = zoomFactor + d;
            if (newZoomFactor <= MIN_ZOOM) {
                log.finer("Cannot zoom out more");
                return;
            } else if (newZoomFactor > MAX_ZOOM) {
                log.finer("Cannot zoom in more");
                return;
            }
            log.finer("Setting zoom factor equals " + newZoomFactor);
            setZoom(newZoomFactor);
        }

    }

    private class ZoomButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == restoreOriginalButton) {
                log.finer("Restoring original image size");
                setZoom(INITIAL_ZOOM_FACTOR);
            }
        }
    }

    private class ZoomButtonChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {

            if (!zoomTimer.isRunning() && (zoomInButtonModel.isPressed()
                    || zoomOutButtonModel.isPressed())) {
                zoomTimer.start();
            } else if (zoomTimer.isRunning() && !zoomInButtonModel.isPressed()
                    && !zoomOutButtonModel.isPressed()) {
                zoomTimer.stop();
            }
        }
    }
}
