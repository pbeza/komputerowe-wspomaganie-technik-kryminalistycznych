package backend;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;

public class Eigenfaces {

	private final static String DEFAULT_FACES_LEARNING_SET_CSV_PATH = "../../faces/faces.csv";
	private final static int UNKNOWN_LABEL = -1, LABELS_MARTIX_TYPE = CvType.CV_32SC1;

	private List<Mat> learningSetFaces;
	private List<Integer> learningSetFacesLabels;
	private Mat matLearningSetFacesLabels;
	private int predictedLabel;
	private Mat faceToIdentify;
	private int faceToIdentifyLabel;
	private CsvParser csvParser;
	private FaceRecognizer eigenfacesRecognizer;
	private boolean isModelTrained;

	public Eigenfaces() {
		this(DEFAULT_FACES_LEARNING_SET_CSV_PATH);
	}

	public Eigenfaces(String facesCsvPath) {
		learningSetFaces = new ArrayList<Mat>();
		learningSetFacesLabels = new ArrayList<Integer>();
		csvParser = new CsvParser(facesCsvPath);
		isModelTrained = false;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public int predictFaces(String faceToIdentifyPath) throws IOException, URISyntaxException {
		return predictFaces(faceToIdentifyPath, UNKNOWN_LABEL);
	}

	public int predictFaces(String faceToIdentifyPath, int faceToIdentifyLabel) throws IOException, URISyntaxException {

		File tmpPngFile = TempPngFileCreator.createTmpPngCopy(faceToIdentifyPath);
		String tmpFaceCanonicalPath = tmpPngFile.getCanonicalPath();
		Mat faceToIdentify = Imgcodecs.imread(tmpFaceCanonicalPath, CsvParser.IMREAD_FLAGS);
		if (!tmpPngFile.delete()) {
			System.out.println("Warning! Temporary file was't deleted! " + tmpFaceCanonicalPath);
		}
		return predictFaces(faceToIdentify, faceToIdentifyLabel);
	}

	public int predictFaces(Mat faceToIdentify) throws IOException, URISyntaxException {
		return predictFaces(faceToIdentify, UNKNOWN_LABEL);
	}

	public int predictFaces(Mat faceToIdentify, int faceToIdentifyLabel) throws IOException, URISyntaxException {

		this.faceToIdentify = faceToIdentify;
		this.faceToIdentifyLabel = faceToIdentifyLabel;

		if (!isModelTrained) {
			train();
		}

		// int height = images.get(0).rows();
		// Mat testSample = learningSetFaces.remove(learningSetFaces.size() -
		// 1);
		// int testLabel =
		// learningSetFacesLabels.remove(learningSetFacesLabels.size() - 1);

		this.predictedLabel = eigenfacesRecognizer.predict(faceToIdentify);

		System.out.println(String.format("Predicted class = %d", predictedLabel));
		System.out.print("Actual class = ");
		System.out.println(faceToIdentifyLabel == UNKNOWN_LABEL ? "unknown" : faceToIdentifyLabel);

		return predictedLabel;
	}

	public void train() throws IOException, URISyntaxException {
		try {
			loadImagesAndLabelsFromCsv();
		} catch (IOException | URISyntaxException e) {
			System.out.println("Error during loading images from database. Details: " + e.getMessage());
			throw e;
		}
		eigenfacesRecognizer = Face.createEigenFaceRecognizer();
		eigenfacesRecognizer.train(learningSetFaces, matLearningSetFacesLabels);
		isModelTrained = true;
	}

	private void loadImagesAndLabelsFromCsv() throws IOException, URISyntaxException {

		csvParser.readCsv(learningSetFaces, learningSetFacesLabels);

		if (learningSetFaces.size() <= 1) {
			throw new RuntimeException(
					"At least 2 images are required to work. Please add more images to your data set!");
		}

		matLearningSetFacesLabels = labelsToMat(learningSetFacesLabels);
	}

	/**
	 * Converts list of faces' labels to labels matrix required by OpenCV Java
	 * API.
	 * 
	 * @see http://stackoverflow.com/questions/25748096/opencv-in-java-use-mat-
	 *      instead-of-vector-for-labels
	 * @param labels
	 * @return
	 */
	private Mat labelsToMat(List<Integer> labels) {
		final int rows = labels.size(), cols = 1, type = LABELS_MARTIX_TYPE;
		Mat resLabels = new Mat(rows, cols, type);
		for (int i = 0; i < labels.size(); i++) {
			resLabels.put(i, 0, labels.get(i));
		}
		return resLabels;
	}

	/**
	 * Simple test interface for stand-alone tests.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String faceToIdentifyPath = null;
		String learningSetCsvPath = DEFAULT_FACES_LEARNING_SET_CSV_PATH;

		if (args.length == 2) {
			learningSetCsvPath = args[1];
		} else if (args.length != 1) {
			System.out.println("USAGE: eigenfaces <face_to_identify_path> [learning_set_CSV_path]");
			return;
		}

		faceToIdentifyPath = args[0];
		Eigenfaces e = new Eigenfaces(learningSetCsvPath);

		try {
			e.predictFaces(faceToIdentifyPath);
		} catch (IOException | URISyntaxException e1) {
			System.out.println("Application has failed. Details: " + e1.getMessage());
			System.out.println("Stack trace:");
			e1.printStackTrace();
		}
	}
}