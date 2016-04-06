package backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;

public class Eigenfaces {

	static int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = list.get(i);
		return ret;
	}

	/**
	 * @see http://stackoverflow.com/questions/25748096/opencv-in-java-use-mat-
	 *      instead-of-vector-for-labels
	 * @param labels
	 * @return
	 */
	static Mat labelsToMat(List<Integer> labels) {
		final int rows = labels.size(), cols = 1, type = CvType.CV_32SC1;
		Mat resLabels = new Mat(rows, cols, type);
		for (int i = 0; i < labels.size(); i++) {
			resLabels.put(i, 0, labels.get(i));
		}
		return resLabels;
	}

	public static void readCsv(String csvfile, List<Mat> mats, List<Integer> labels)
			throws IOException, URISyntaxException {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		final String csvSeparator = ";", relativeFacesPath = "../../faces";
		if (!Files.exists(Paths.get(csvfile))) {
			throw new FileNotFoundException(csvfile + " CSV file with faces' images wasn't found");
		}
		String line;
		Mat imgMat = null;
		try (BufferedReader br = new BufferedReader(new FileReader(csvfile))) {
			while ((line = br.readLine()) != null) {
				String[] pathAndGroup = line.split(csvSeparator);
				Path path = Paths.get(relativeFacesPath, pathAndGroup[0]);
				int label = Integer.parseInt(pathAndGroup[1]);
				String canonicalPath = new URI(path.toString()).normalize().getPath();
				// GIF format is not supported. Create PNG temporary copy.
				File tmpPngFile = createTmpPngCopy(canonicalPath);
				canonicalPath = tmpPngFile.getCanonicalPath();
				try {
					imgMat = Imgcodecs.imread(canonicalPath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				// Remove temporary PNG.
				if (!tmpPngFile.delete()) {
					System.out.println("Warning! Temporary file was't deleted! " + canonicalPath);
				}
				mats.add(imgMat);
				labels.add(label);
			}
		}
	}

	private static File createTmpPngCopy(String canonicalPath) throws IOException {
		final String prefix = "opencv", suffix = ".png";
		File tmpPngFile = File.createTempFile(prefix, suffix);
		File input = new File(canonicalPath);
		ImageIO.write(ImageIO.read(input), "png", tmpPngFile);
		return tmpPngFile;
	}

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		/*
		 * if (args.length < 2) { System.out.println(
		 * "usage: java -jar 'the jar' <csv.ext> <output_folder> ");
		 * System.exit(1); } String outputFolder = args[1]; String csv =
		 * args[0];
		 */
		String csv = "faces.csv";
		csv = "../../faces/" + csv;

		List<Mat> images = new ArrayList<Mat>();
		List<Integer> labels = new ArrayList<Integer>();

		try {
			Eigenfaces.readCsv(csv, images, labels);
		} catch (Exception e) {
			System.out.println("Failed to load images");
			return;
		}

		if (images.size() <= 1) {
			throw new RuntimeException(
					"At least 2 images are required to work. Please add more images to your data set!");
		}

		// int height = images.get(0).rows();

		Mat testSample = images.remove(images.size() - 1);
		int testLabel = labels.remove(labels.size() - 1);

		FaceRecognizer model = Face.createEigenFaceRecognizer();

		Mat matlabels = labelsToMat(labels);
		model.train(images, matlabels);

		int predictedLabel = model.predict(testSample);

		System.out.println(String.format("Predicted class = %d / Actual class = %d.", predictedLabel, testLabel));
	}
}