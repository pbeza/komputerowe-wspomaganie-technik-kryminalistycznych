package backend.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "faces")
public class FaceEntity {

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
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
