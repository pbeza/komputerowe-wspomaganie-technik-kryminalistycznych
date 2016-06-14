package frontend;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.time.Instant;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import backend.Log;
import backend.db.FaceEntity;

/**
 * Represents full image and icon in GUI.
 */
class ImageListCell extends JLabel {

    private final static Log log = Log.getLogger();
    private final static double IMAGE_RATIO = 243.0 / 320.0;
    private final static int ICON_WIDTH = 180, ICON_HEIGHT = (int) (ICON_WIDTH * IMAGE_RATIO), MIN_IMAGE_ID = 1;
    private final static Dimension ICON_SIZE = new Dimension(ICON_WIDTH, ICON_HEIGHT);
    private static int latelyAssignedDisplayedId = MIN_IMAGE_ID;
    private final int displayedId;
    private final BufferedImage image;
    private final FaceEntity faceEntity;
    private String filepath; // TODO remove it! Use BufferedImage instead

    // This constructor is dedicated for images added to local database
    // personId must NOT collide with persons' IDs loaded from DB
    public ImageListCell(File img, int personId) throws IOException {
        this(img);
        faceEntity.setPersonId(personId);
        // HOTFIX
        byte[] imgArr;
        try {
            imgArr = Files.readAllBytes(Paths.get(img.getCanonicalPath()));
            faceEntity.setImage(imgArr);
        } catch (IOException e) {
            log.severe("Failed to readAllBytes from image which was tried to add to local DB " + img.getCanonicalPath()
                    + ". Details: " + e.getMessage());
        }
    }

    public ImageListCell(File img) throws IOException {
        this(ImageIO.read(img), img.getName(), Files.probeContentType(img.toPath()));
        filepath = img.getCanonicalPath();
    }

    public ImageListCell(FaceEntity faceEntity) {
        super();
        displayedId = ImageListCell.latelyAssignedDisplayedId++;
        this.faceEntity = faceEntity;
        image = faceEntity.convertToBufferedImage();
        setGUI();
    }

    private ImageListCell(BufferedImage image, String filename, String mimetype) {
        // TODO this(new FaceEntity(labelId, image, filename, mimetype));
        super();
        this.image = image;
        displayedId = ImageListCell.latelyAssignedDisplayedId++;
        faceEntity = new FaceEntity(image, filename, mimetype);
        setGUI();
    }

    private void setGUI() {
        String text = displayedId + ". " + faceEntity.getFilename();
        setText(text);
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

    public int getCellId() {
        return faceEntity.getId();
    }

    public FaceEntity getFaceEntity() {
        return faceEntity;
    }

    public String getFilepath() {
        return filepath;
    }

    public int getPersonId() {
        return faceEntity.getPersonId();
    }

    public int getImageId() {
        return faceEntity.getImageId();
    }

    public Timestamp getTimestamp() {
        return faceEntity.getTimestamp();
    }

    public void setTimestamp(File f) {
        try {
            BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            Instant instant = fileTime.toInstant();
            Timestamp timestamp = Timestamp.from(instant);
            faceEntity.setTimestamp(timestamp);
        } catch (IOException e) {
            // Just don't change timestamp
        }
    }
}
