package frontend;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

class ImageListCell extends JLabel {

	private static int id = 0;
	final private static double IMAGE_RATIO = 4.0 / 3.0;
	final private static int ICON_WIDTH = 180, ICON_HEIGHT = (int) (ICON_WIDTH * IMAGE_RATIO);
	final private static Dimension ICON_SIZE = new Dimension(ICON_WIDTH, ICON_HEIGHT);
	final private BufferedImage image;
	final private String filename, fullPath;

	public ImageListCell(BufferedImage img, String fname, String path) {
		super(Integer.toString(id++) + ". " + fname,
				new ImageIcon(img.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT)), LEFT);
		image = img;
		filename = fname;
		fullPath = path;
		setPreferredSize(ICON_SIZE);
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

	public BufferedImage getImage() {
		return image;
	}
}
