package backend;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;

import frontend.ImageListCell;

public class Eigenfaces {
    private final static Logger log = Log.getLogger();
    private final static int UNKNOWN_LABEL = -1, LABELS_MARTIX_TYPE = CvType.CV_8UC3;// CvCV_32SC1
    private final FaceRecognizer eigenfacesRecognizer;
    private int predictedLabel;
    private double predictedConfidence;
    private boolean isModelTrained = false;

    public Eigenfaces() {
        log.info("Loading OpenCV libraries...");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        log.info("Loading OpenCV libraries successfully loaded");
        // TODO see and use more flexible constructors for FaceRecognizer
        eigenfacesRecognizer = Face.createEigenFaceRecognizer();
    }

    public void train(List<ImageListCell> learningSetFaceEntities) {
        int n = learningSetFaceEntities.size();
        List<Integer> labels = new ArrayList<>(n);
        List<byte[]> images = new ArrayList<>(n);
        for (ImageListCell f : learningSetFaceEntities) {
            int label = f.getLabelId();
            BufferedImage faceImage = f.getImage();
            byte[] faceImageArray = convertBufferedImageToByteArray(faceImage);
            labels.add(label);
            images.add(faceImageArray);
        }
        train(images, labels);
    }

    private void train(List<byte[]> learningSetFaceEntities, List<Integer> learningSetFacesLabels) {
        int n = learningSetFaceEntities.size();
        if (n != learningSetFacesLabels.size() || n < 2) {
            throw new AssertionError("Number of faces images must be equal to number of images' labels (at least 2)");
        }
        if (n <= 5) {
            log.warning(n + " images in learning set is not enough. Add more images to your data set!");
        }
        List<Mat> learningSetFaces = new ArrayList<>(n);
        for (byte[] b : learningSetFaceEntities) {
            Mat m = new Mat();
            m.put(0, 0, b);
        }
        Mat matLearningSetFacesLabels = labelsToMat(learningSetFacesLabels);
        eigenfacesRecognizer.train(learningSetFaces, matLearningSetFacesLabels);
        isModelTrained = true;
    }

    public int predictFaces(BufferedImage faceToIdentify) {
        byte[] face = convertBufferedImageToByteArray(faceToIdentify);
        return predictFaces(face);
    }

    public int predictFaces(byte[] faceToIdentify) {
        return predictFaces(faceToIdentify, UNKNOWN_LABEL);
    }

    public int predictFaces(byte[] faceToIdentify, int faceToIdentifyLabel) {
        Mat face = new Mat();
        face.put(0, 0, faceToIdentify);
        return predictFaces(face, faceToIdentifyLabel);
    }

    public int predictFaces(Mat faceToIdentify) throws IOException, URISyntaxException {
        return predictFaces(faceToIdentify, UNKNOWN_LABEL);
    }

    public int predictFaces(Mat faceToIdentify, int faceToIdentifyLabel) {
        if (!isModelTrained) {
            throw new AssertionError("You must train model before starting identifying face");
        }
        int[] label_out = new int[1];
        double[] confidence_out = new double[1];
        eigenfacesRecognizer.predict(faceToIdentify, label_out, confidence_out);
        predictedLabel = label_out[0];
        predictedConfidence = confidence_out[0];
        log.info(String.format("Predicted class = %d with confidence %f", predictedLabel, predictedConfidence));
        log.info("Actual class = " + (faceToIdentifyLabel == UNKNOWN_LABEL ? "unknown" : faceToIdentifyLabel));
        return predictedLabel;
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
        for (int i = 0; i < rows; i++) {
            resLabels.put(i, 0, labels.get(i));
        }
        return resLabels;
    }

    private byte[] convertBufferedImageToByteArray(BufferedImage faceToIdentify) {
        WritableRaster faceRaster = faceToIdentify.getRaster();
        DataBuffer faceDataBuffer = faceRaster.getDataBuffer();
        DataBufferByte faceDataBufferByte = (DataBufferByte) faceDataBuffer;
        byte[] face = faceDataBufferByte.getData();
        return face;
    }
}