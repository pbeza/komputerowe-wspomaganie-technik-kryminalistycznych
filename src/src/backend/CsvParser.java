package backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class CsvParser {

    public final static int IMREAD_FLAGS = Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
    public final static String CSV_SEPARATOR = ";", RELATIVE_FACES_PATH = "../../faces",
            FACES_LEARNING_SET_CSV_PATH = "../../faces/faces.csv";
    private final static Logger log = Log.getLogger();

    private final String facesCsvPath;
    private List<Mat> mats = new ArrayList<>();
    private List<Integer> labels = new ArrayList<>();
    private List<String> canonicalPaths = new ArrayList<>();

    public CsvParser(String facesCsvPath) {
        this.facesCsvPath = facesCsvPath;
    }

    public void readCsv(List<Mat> mats, List<Integer> labels, List<String> canonicalPaths)
            throws IOException, URISyntaxException {
        this.mats = mats;
        this.labels = labels;
        this.canonicalPaths = canonicalPaths;

        if (!Files.exists(Paths.get(facesCsvPath))) {
            throw new FileNotFoundException(facesCsvPath + " CSV file with faces' images wasn't found");
        }

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(facesCsvPath))) {
            while ((line = br.readLine()) != null) {
                processFaceImage(line);
            }
        }
    }

    public void readCsv(List<Mat> mats, List<Integer> labels) throws IOException, URISyntaxException {
        List<String> p = new ArrayList<>();
        readCsv(mats, labels, p);
    }

    private void processFaceImage(String line) throws URISyntaxException, IOException {

        String[] pathAndLabel = line.split(CSV_SEPARATOR);
        if (pathAndLabel.length != 2) {
            throw new RuntimeException("Unexpected line format in " + facesCsvPath + ". Line: " + line);
        }
        Path facePath = Paths.get(RELATIVE_FACES_PATH, pathAndLabel[0]);
        int faceLabel = Integer.parseInt(pathAndLabel[1]);
        String canonicalFacePath = facePath.toFile().getCanonicalPath();
        processFaceImage(faceLabel, canonicalFacePath);
    }

    private void processFaceImage(int label, String faceCanonicalPath) throws IOException {

        // GIF format is not supported. Create PNG temporary copy.

        File tmpPngFile = TempPngFileCreator.createTmpPngCopy(faceCanonicalPath);
        String tmpFaceCanonicalPath = tmpPngFile.getCanonicalPath();

        Mat imgMat = Imgcodecs.imread(tmpFaceCanonicalPath, IMREAD_FLAGS);

        // Remove temporary PNG.

        if (!tmpPngFile.delete()) {
            log.warning("Warning! Temporary file " + tmpFaceCanonicalPath + " was't deleted!");
        }
        mats.add(imgMat);
        labels.add(label);
        canonicalPaths.add(faceCanonicalPath);
    }
}
