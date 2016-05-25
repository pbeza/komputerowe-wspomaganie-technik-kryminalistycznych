package frontend;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.Icon;
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
    private final static int ICON_WIDTH = 180, ICON_HEIGHT = (int) (ICON_WIDTH * IMAGE_RATIO), MIN_IMAGE_ID = 1;
    private final static Dimension ICON_SIZE = new Dimension(ICON_WIDTH, ICON_HEIGHT);
    private static int latelyAssignedDisplayedId = MIN_IMAGE_ID;
    private final int displayedId;
    private final BufferedImage image;
    private final FaceEntity faceEntity;

    public ImageListCell(File img) throws IOException {
        this(img, FaceEntity.UNKNOWN_LABEL_ID);
    }

    private ImageListCell(File img, int labelId) throws IOException {
        this(ImageIO.read(img), img.getName(), Files.probeContentType(img.toPath()), labelId);
    }

    // TODO too much copy-paste
    private ImageListCell(BufferedImage image, String filename, String mimetype, int labelId) {
        // this(new FaceEntity(labelId, image, filename, mimetype));
        super();
        this.image = image;
        displayedId = ImageListCell.latelyAssignedDisplayedId++;
        faceEntity = new FaceEntity(labelId, image, filename, mimetype);
        String text = displayedId + ". " + faceEntity.getFilename();
        setText(text);
        Image scaledImage = image.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT);
        Icon icon = new ImageIcon(scaledImage);
        setIcon(icon);
        setHorizontalAlignment(LEFT);
        setPreferredSize(ICON_SIZE);
    }

    public ImageListCell(FaceEntity faceEntity) {
        super();
        displayedId = ImageListCell.latelyAssignedDisplayedId++;
        this.faceEntity = faceEntity;
        String text = displayedId + ". " + faceEntity.getFilename();
        setText(text);
        image = faceEntity.convertToBufferedImage();
        Image scaledImage = image.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_DEFAULT);
        Icon icon = new ImageIcon(scaledImage);
        setIcon(icon);
        setHorizontalAlignment(LEFT);
        setPreferredSize(ICON_SIZE);
    }

    public static void resetImagesNumbering() {
        ImageListCell.latelyAssignedDisplayedId = MIN_IMAGE_ID;
    }

    public int getDisplayedId() {
        return displayedId;
    }

    public String getFilename() {
        return faceEntity.getFilename();
    }

    public String getMimeType() {
        return faceEntity.getFiletype();
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
        return faceEntity.getLabel();
    }

    public FaceEntity getFaceEntity() {
        return faceEntity;
    }
}
