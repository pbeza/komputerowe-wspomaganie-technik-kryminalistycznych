package backend.db;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
    @Column(name = "id")
    private int id;

    @Column(name = "label", unique = true)
    private int label;

    @Column(name = "image")
    private byte[] image;

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
}
