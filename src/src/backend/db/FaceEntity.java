package backend.db;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
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

    public final static int UNKNOWN_ID = -1, UNKNOWN_PERSON_ID = -1, UNKNOWN_IMAGE_ID = -1;
    private final static Timestamp UNKNOWN_TIMESTAMP = null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "person_id", nullable = false)
    private int personId;

    @Column(name = "image_id", nullable = false)
    private int imageId;

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

    public FaceEntity(BufferedImage bufferedImage, String filename, String filetype) {
        this((DataBufferByte) bufferedImage.getData().getDataBuffer(), filename, filetype);
    }

    private FaceEntity(DataBufferByte imageDataBufferByte, String filename, String filetype) {
        this(imageDataBufferByte.getData(), filename, filetype);
    }

    private FaceEntity(byte[] image, String filename, String filetype) {
        this(UNKNOWN_ID, UNKNOWN_PERSON_ID, UNKNOWN_IMAGE_ID, image, filename, filetype, UNKNOWN_TIMESTAMP);
    }

    private FaceEntity(int id, int personId, int imageId, byte[] image, String filename, String filetype,
            Timestamp timestamp) {
        this.id = id;
        this.personId = personId;
        this.imageId = imageId;
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

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public static void saveFaceImageToFile(byte[] imageArray, String outputPath) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputPath);
            fos.write(imageArray);
            fos.close();
        } catch (IOException e) {
            Log log = Log.getLogger();
            log.severe("Error during converting image from byte array to file " + outputPath + ". Details: "
                    + e.getMessage() + e.getCause());
        }
    }

    void saveFaceImageToFile(String outputPath) {
        FaceEntity.saveFaceImageToFile(image, outputPath);
    }

    public BufferedImage convertToBufferedImage() {
        return FaceEntity.convertToBufferedImage(image);
    }

    private static BufferedImage convertToBufferedImage(byte[] imageArray) {
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

    public static Mat convertGifToMat(String faceGifImagePath) throws IOException {
        final Log log = Log.getLogger();
        final int IMREAD_FLAGS = Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
        File tmpPngFile = TempPngFileCreator.createTmpPngCopy(faceGifImagePath);
        String tmpFaceCanonicalPath = tmpPngFile.getCanonicalPath();
        Mat imgMat = Imgcodecs.imread(tmpFaceCanonicalPath, IMREAD_FLAGS); // 243x320,
                                                                           // CV_8UC1
        if (!tmpPngFile.delete()) {
            log.warning("Warning! Temporary file " + tmpFaceCanonicalPath + " was't deleted!");
        }
        if (imgMat.empty()) {
            log.warning("Matrix m is empty!");
        }
        return imgMat;
    }
}
