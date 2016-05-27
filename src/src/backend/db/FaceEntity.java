package backend.db;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import backend.Log;
import backend.TempPngFileCreator;

@Entity
@Table(name = "faces")
public class FaceEntity {

    public final static int UNKNOWN_ID = -1, UNKNOWN_LABEL_ID = -1;
    private final static Timestamp UNKNOWN_TIMESTAMP = null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "label", nullable = false, unique = true)
    private int label;

    @Column(name = "image", nullable = false)
    private byte[] image;

    @Column(name = "filename", nullable = false, unique = true)
    private String filename;

    @Column(name = "filetype", nullable = false)
    private String filetype;

    @Column(name = "timestamp", nullable = false, insertable = false, columnDefinition = "DEFAULT CURRENT_TIMESTAMP NOT NULL")
    private Timestamp timestamp;

    public FaceEntity() {
    }

    private FaceEntity(BufferedImage bufferedImage, String filename,
            String filetype) {
        this(UNKNOWN_LABEL_ID, bufferedImage, filename, filetype);
    }

    public FaceEntity(int label, BufferedImage bufferedImage, String filename,
            String filetype) {
        this(label, (DataBufferByte) bufferedImage.getData().getDataBuffer(),
                filename, filetype);
    }

    private FaceEntity(int label, DataBufferByte imageDataBufferByte,
            String filename, String filetype) {
        this(label, imageDataBufferByte.getData(), filename, filetype);
    }

    private FaceEntity(byte[] image, String filename, String filetype) {
        this(UNKNOWN_LABEL_ID, image, filename, filetype);
    }

    private FaceEntity(int label, byte[] image, String filename,
            String filetype) {
        this(UNKNOWN_ID, label, image, filename, filetype, UNKNOWN_TIMESTAMP);
    }

    private FaceEntity(int id, int label, byte[] image, String filename,
            String filetype, Timestamp timestamp) {
        this.id = id;
        this.label = label;
        this.image = image;
        this.filename = filename;
        this.filetype = filetype;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Timestamp setTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public static void saveFaceImageToFile(byte[] imageArray,
            String outputPath) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage() + e.getCause());
            return;
        }
        try {
            fos.write(imageArray);
            fos.close();
        } catch (IOException e) {
            System.err.println("Failed to write " + outputPath);
            return;
        }
    }

    public void saveFaceImageToFile(String outputPath) {
        FaceEntity.saveFaceImageToFile(image, outputPath);
    }

    public BufferedImage convertToBufferedImage() {
        return FaceEntity.convertToBufferedImage(image);
    }

    public static BufferedImage convertToBufferedImage(byte[] imageArray) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageArray);
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(bais);
        } catch (IOException e) {
            Logger log = Log.getLogger();
            log.severe("Cannot convert byte[] to BufferedImage");
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

    public static Mat convertGifToMat(String faceGifImagePath)
            throws IOException {
        final int IMREAD_FLAGS = Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
        File tmpPngFile = TempPngFileCreator.createTmpPngCopy(faceGifImagePath);
        String tmpFaceCanonicalPath = tmpPngFile.getCanonicalPath();
        Mat imgMat = Imgcodecs.imread(tmpFaceCanonicalPath, IMREAD_FLAGS); // 243x320,
                                                                           // CV_8UC1

        // Remove temporary PNG.

        Log log = Log.getLogger();
        if (!tmpPngFile.delete()) {
            log.warning("Warning! Temporary file " + tmpFaceCanonicalPath
                    + " was't deleted!");
        }
        if (imgMat.empty()) {
            log.warning("Matrix m is empty!");
        }
        return imgMat;
    }
}
