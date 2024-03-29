package backend;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.face.BasicFaceRecognizer;
import org.opencv.face.Face;

import backend.db.FaceEntity;

public class Eigenfaces {
    private final static Logger log = Log.getLogger();
    private BasicFaceRecognizer eigenfacesRecognizer;
    private boolean isModelTrained = false;
    private double threshold = Double.MAX_VALUE;
    private int eigenfacesNumber = 0;

    public class PredictionPoint extends Point implements Comparable<Point> {

        private final int indexInJList;

        public PredictionPoint(Point p, int indexInJList) {
            super(p.x, p.y);
            this.indexInJList = indexInJList;
        }

        public int getLabel() {
            return (int) x;
        }

        public double getConfidence() {
            return y;
        }

        public int getIndexInJList() {
            return indexInJList;
        }

        @Override
        public int compareTo(Point o) {
            return Double.compare(y, o.y);
        }
    }

    public Eigenfaces() {
        log.info("Loading OpenCV libraries...");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        log.info("Loading OpenCV libraries successfully loaded");
    }

    public int getEigenfacesNumber() {
        return eigenfacesNumber;
    }

    public void setEigenfacesNumber(int eigenfacesNumber) {
        this.eigenfacesNumber = eigenfacesNumber;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean getIsModelTrained() {
        return isModelTrained;
    }

    public void setModelTrained(boolean isModelTrained) {
        this.isModelTrained = isModelTrained;
    }

    public void train(List<FaceEntity> learningSetFaceEntities) throws IOException {
        int n = learningSetFaceEntities.size();
        List<Integer> labels = new ArrayList<>(n);
        List<byte[]> images = new ArrayList<>(n);
        for (FaceEntity f : learningSetFaceEntities) {
            int label = f.getPersonId();
            byte[] faceImageArray = f.getImage();
            labels.add(label);
            images.add(faceImageArray);
        }
        n = images.size();
        if (n != labels.size() || n < 2) {
            throw new AssertionError("Number of faces images must be equal to number of images' labels (at least 2)");
        } else if (n <= 5) {
            log.warning(n + " images in learning set is not enough. Add more images to your learning data set!");
        }
        train(images, labels);
    }

    private void train(List<byte[]> learningSetImages, List<Integer> learningSetFacesLabels) throws IOException {
        int n = learningSetImages.size();
        assert n == learningSetFacesLabels.size();
        List<Mat> learningSetFaces = new ArrayList<>(n);
        // TODO this can be probably done without temporary file
        File tmpGif = File.createTempFile("tmp_eigenface", ".gif");
        String tmpGifFilepath = tmpGif.getCanonicalPath();
        for (byte[] b : learningSetImages) {
            FaceEntity.saveFaceImageToFile(b, tmpGifFilepath);
            Mat m = FaceEntity.convertGifToMat(tmpGifFilepath);
            learningSetFaces.add(m);
        }
        if (!tmpGif.delete()) {
            log.warning("Failed to delete " + tmpGifFilepath + " temporary file");
        }
        Mat matLearningSetFacesLabels = labelsToMat(learningSetFacesLabels);
        eigenfacesRecognizer = Face.createEigenFaceRecognizer(eigenfacesNumber, threshold);
        eigenfacesRecognizer.train(learningSetFaces, matLearningSetFacesLabels);
        isModelTrained = true;
        // TODO add to DB default learned model
    }

    public void saveLearnedModelInXML(String filename) {
        if (isModelTrained) {
            eigenfacesRecognizer.save(filename);
        } else {
            JOptionPane.showMessageDialog(null, "Train your model first!");
        }
    }

    public List<PredictionPoint> predictFaces(BufferedImage faceToIdentify) throws IOException, URISyntaxException {
        byte[] face = convertBufferedImageToByteArray(faceToIdentify);
        return predictFaces(face);
    }

    private List<PredictionPoint> predictFaces(byte[] faceToIdentify) throws IOException, URISyntaxException {
        Mat face = new Mat();
        face.put(0, 0, faceToIdentify);
        return predictFaces(face);
    }

    public List<PredictionPoint> predictFaces(Mat faceToIdentify) {
        if (!isModelTrained) {
            throw new AssertionError("You must train model before starting identifying face");
        }
        MatOfPoint2f prediction = eigenfacesRecognizer.predict_dummy(faceToIdentify);
        List<Point> points = prediction.toList();
        List<PredictionPoint> predictionPoints = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            predictionPoints.add(new PredictionPoint(p, i));
        }
        Collections.sort(predictionPoints);
        return predictionPoints;
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
        final int rows = labels.size(), cols = 1, type = CvType.CV_32SC1;
        Mat resLabels = new Mat(rows, cols, type);
        for (int i = 0; i < rows; i++) {
            int l = labels.get(i);
            resLabels.put(i, 0, l);
        }
        return resLabels;
    }

    private byte[] convertBufferedImageToByteArray(BufferedImage faceToIdentify) {
        WritableRaster faceRaster = faceToIdentify.getRaster();
        DataBuffer faceDataBuffer = faceRaster.getDataBuffer();
        DataBufferByte faceDataBufferByte = (DataBufferByte) faceDataBuffer;
        return faceDataBufferByte.getData();
    }
}