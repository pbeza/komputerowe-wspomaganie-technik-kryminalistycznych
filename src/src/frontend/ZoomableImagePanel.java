package frontend;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ZoomableImagePanel extends JPanel {

	private BufferedImage origImage, scaledImage;

	public void setImage(BufferedImage bufImg) {
		origImage = bufImg;
	}
	
	public void clearImage() {
		origImage = null;
		scaledImage = null;
	}

	protected boolean setScaledImage(double zoomFactor) {

		if (origImage == null) {
			return false;
		}
		final int w = (int) (origImage.getWidth() * zoomFactor);
		final int h = (int) (origImage.getHeight() * zoomFactor);
		scaledImage = new BufferedImage(w, h, origImage.getType());
		final Graphics2D g2 = scaledImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(origImage, 0, 0, w, h, null);
		g2.dispose();
		setPreferredSize(new Dimension(w, h));
		revalidate();
		return true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (scaledImage != null) {
			final int x = (getWidth() - scaledImage.getWidth(this)) / 2;
			final int y = (getHeight() - scaledImage.getHeight(this)) / 2;
			g.drawImage(scaledImage, x, y, this);
		}
	}
}
