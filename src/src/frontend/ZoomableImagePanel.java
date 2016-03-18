package frontend;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ZoomableImagePanel extends JPanel implements MouseWheelListener {

	private final JButton zoomInButton, zoomOutButton;
	private final JScrollPane scrollPaneImageZoom;
	private final JPanel panelForCenteringInScrollPane;
	private final static double ZOOM_FACTOR = 0.25;
	private double zoom = 1;
	private BufferedImage image;
	private Icon icon;
	private final JLabel imageLabel;

	public ZoomableImagePanel() {
		setLayout(new BorderLayout());

		imageLabel = new JLabel();

		panelForCenteringInScrollPane = new JPanel(new GridBagLayout());
		panelForCenteringInScrollPane.add(imageLabel);

		scrollPaneImageZoom = new JScrollPane(panelForCenteringInScrollPane);
		add(scrollPaneImageZoom, BorderLayout.CENTER);

		final ZoomButtonActionListener zoomListener = new ZoomButtonActionListener();
		zoomInButton = new JButton("+");
		zoomInButton.addActionListener(zoomListener);
		zoomInButton.setToolTipText("Zoom in");

		zoomOutButton = new JButton("-");
		zoomOutButton.addActionListener(zoomListener);
		zoomOutButton.setToolTipText("Zoom out");

		final JPanel bottomZoomInOutPanel = new JPanel(new GridLayout());
		bottomZoomInOutPanel.add(zoomInButton);
		bottomZoomInOutPanel.add(zoomOutButton);

		add(bottomZoomInOutPanel, BorderLayout.SOUTH);
	}

	public void setImage(BufferedImage bufImg) {
		image = bufImg;
		icon = new ImageIcon(bufImg);
		imageLabel.setIcon(icon);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown()) {
			final int r = e.getWheelRotation();
			System.out.println(r);
		} else {
			getParent().dispatchEvent(e);
		}
	}

	private class ZoomButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final double factor = e.getSource() == zoomInButton ? ZOOM_FACTOR : -ZOOM_FACTOR;
			System.out.print("Zoom before: " + factor);
			if (zoom <= 0) {
				System.out.println("Cannot zoom out more");
				return;
			} else if (zoom > 1) {
				System.out.println("Cannot zoom in more");
				return;
			} else if (image == null) {
				System.out.println("Cannot zoom in / out because image not set");
				return;
			}
			zoom += factor;
			final int w = (int) (image.getWidth() * zoom); // TODO
															// image.getWidth()
															// -> original width
			final int h = (int) (image.getHeight() * zoom); // TODO
															// image.getWidth()
															// -> original
															// height
			final Graphics2D g2 = image.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(image, 0, 0, w, h, null);
			g2.dispose();
			scrollPaneImageZoom.repaint();
		}
	}
}
