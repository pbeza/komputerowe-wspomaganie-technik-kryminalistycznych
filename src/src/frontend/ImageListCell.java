package frontend;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

class ImageListCell extends JLabel {

	private static int id = 0;
	final private static double IMAGE_RATIO = 4.0 / 3.0;
	final private static int ICON_WIDTH = 200, ICON_HEIGHT = (int) (ICON_WIDTH * IMAGE_RATIO);
	final private static Dimension ICON_SIZE = new Dimension(ICON_WIDTH, ICON_HEIGHT);
	final private BufferedImage image;
	final private String filename, fullPath;
	// final private ImageIcon icon;
	// final BufferedImage resizedIcon = new BufferedImage(ICON_WIDTH,
	// ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);

	public ImageListCell(BufferedImage img, String fname, String path) {
		super(Integer.toString(id++) + ". " + fname,
				new ImageIcon(img.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT)), LEFT);
		image = img;
		filename = fname;
		fullPath = path;
		setPreferredSize(ICON_SIZE);
		// Graphics2D g2 = resizedIcon.createGraphics();
		// g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		// RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// g2.drawImage(img, 0, 0, ICON_WIDTH, ICON_HEIGHT, null);
		// g2.dispose();
	}

	public String getFilename() {
		return filename;
	}

	public String getFullPath() {
		return fullPath;
	}

	public int getImageWidth() {
		return image.getWidth();
	}

	public int getImageHeight() {
		return image.getHeight();
	}
}
