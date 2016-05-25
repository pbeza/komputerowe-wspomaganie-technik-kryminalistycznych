package frontend;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import backend.Log;
import backend.db.FaceEntity;

/**
 * Represents full image and icon.
 */
public class ImageListCell extends JLabel {

    private final static Log log = Log.getLogger();
    private final static double IMAGE_RATIO = 4.0 / 3.0;
    private final static int ICON_WIDTH = 180, ICON_HEIGHT = (int) (ICON_WIDTH * IMAGE_RATIO), MIN_IMAGE_ID = 1,
            DEFAULT_LABEL_ID = -1;
    private final static Dimension ICON_SIZE = new Dimension(ICON_WIDTH, ICON_HEIGHT);
    private static int latelyAssignedId = MIN_IMAGE_ID;
    private final int id, labelId;
    private final BufferedImage image;
    private final String filename;
    private final String mimeType;

    public ImageListCell(File img) throws IOException {
        this(ImageIO.read(img), img.getName(), Files.probeContentType(img.toPath()));
    }

    public ImageListCell(FaceEntity faceEntity) {
        this(faceEntity.convertToBufferedImage(), faceEntity.getFilename(), faceEntity.getFiletype());
    }

    public ImageListCell(BufferedImage image, String filename, String mimetype) {
        this(image, filename, mimetype, DEFAULT_LABEL_ID);
    }

    public ImageListCell(BufferedImage image, String filename, String mimetype, int labelId) {
        super(Integer.toString(ImageListCell.latelyAssignedId) + ". " + filename,
                new ImageIcon(image.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT)), LEFT);
        id = ImageListCell.latelyAssignedId++;
        this.image = image;
        this.filename = filename;
        mimeType = mimetype;
        this.labelId = labelId;
        setPreferredSize(ICON_SIZE);
    }

    public static void resetImagesNumbering() {
        ImageListCell.latelyAssignedId = MIN_IMAGE_ID;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
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

    public int getLabelId() {
        return labelId;
    }
}
