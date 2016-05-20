package frontend;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/***
 * Represents image: full image and UI icon
 */
class ImageListCell extends JLabel {

    private final static double IMAGE_RATIO = 4.0 / 3.0;
    private final static int ICON_WIDTH = 180, ICON_HEIGHT = (int) (ICON_WIDTH * IMAGE_RATIO);
    private final static Dimension ICON_SIZE = new Dimension(ICON_WIDTH, ICON_HEIGHT);
    private final int id;
    /***
     * Image with buffering utilities.
     */
    private final BufferedImage image;
    private final String filename, fullPath;
    private String mimeType;

    public ImageListCell(int id, BufferedImage image, String filename, String fullPath) {
        super(Integer.toString(id) + ". " + filename,
                new ImageIcon(image.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT)), LEFT);
        this.id = id;
        this.image = image;
        this.filename = filename;
        this.fullPath = fullPath;
        try {
            this.mimeType = Files.probeContentType(Paths.get(fullPath));
        } catch (IOException e) {
            this.mimeType = "unknown";
        }
        setPreferredSize(ICON_SIZE);
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getMimeType() {
        return mimeType;
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
