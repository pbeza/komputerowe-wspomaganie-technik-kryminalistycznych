package backend.db;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
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

import backend.Log;

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

    private FaceEntity(BufferedImage bufferedImage, String filename, String filetype) {
        this(UNKNOWN_LABEL_ID, bufferedImage, filename, filetype);
    }

    public FaceEntity(int label, BufferedImage bufferedImage, String filename, String filetype) {
        this(label, (DataBufferByte) bufferedImage.getData().getDataBuffer(), filename, filetype);
    }

    private FaceEntity(int label, DataBufferByte imageDataBufferByte, String filename, String filetype) {
        this(label, imageDataBufferByte.getData(), filename, filetype);
    }

    private FaceEntity(byte[] image, String filename, String filetype) {
        this(UNKNOWN_LABEL_ID, image, filename, filetype);
    }

    private FaceEntity(int label, byte[] image, String filename, String filetype) {
        this(UNKNOWN_ID, label, image, filename, filetype, UNKNOWN_TIMESTAMP);
    }

    private FaceEntity(int id, int label, byte[] image, String filename, String filetype, Timestamp timestamp) {
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

    public void saveFaceImageToFile(String outputPath) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage() + e.getCause());
            return;
        }
        try {
            fos.write(getImage());
            fos.close();
        } catch (IOException e) {
            System.err.println("Failed to write " + outputPath);
            return;
        }
    }

    public BufferedImage convertToBufferedImage() {
        ByteArrayInputStream bais = new ByteArrayInputStream(image);
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
}
