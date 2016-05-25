package backend_tests;

import org.junit.Before;
import org.junit.Test;

public class EigenfacesSimpleTests {
    // private static final String PATH_PREFIX = "././faces/YaleFacedatabaseA/";
    // private Eigenfaces eigenfaces;
    // private HashMap<String, Integer> facesFromTrainingFacesSet;

    @Before
    public void setUp() throws Exception {
        // eigenfaces = new Eigenfaces();
        // facesFromTrainingFacesSet = new HashMap<>();
        // eigenfaces.train();
        // createTests();
    }

    // private void createTests() {
    // String[] fnames = { "subject09.happy.gif", "subject13.rightlight.gif",
    // "subject14.surprised.gif" };
    // int[] expectedLabels = { 9, 13, 14 };
    // assert (fnames.length == expectedLabels.length);
    // for (int i = 0; i < fnames.length; i++) {
    // facesFromTrainingFacesSet.put(PATH_PREFIX + fnames[i],
    // expectedLabels[i]);
    // }
    // }

    @Test
    public void testImageFromTrainingSet() {
        // for (Map.Entry<String, Integer> p :
        // facesFromTrainingFacesSet.entrySet()) {
        // String facePath = p.getKey();
        // int expectedLabel = p.getValue();
        // int predictedLabel = -1;
        // try {
        // predictedLabel = eigenfaces.predictFaces(facePath, expectedLabel);
        // } catch (IOException | URISyntaxException e) {
        // fail("Exception: " + e.getMessage());
        // }
        // if (expectedLabel != predictedLabel) {
        // fail("Expected label = " + expectedLabel + ", predicted label = " +
        // predictedLabel);
        // }
        // System.out.println("Expected label = " + expectedLabel + ", predicted
        // label = " + predictedLabel);
        // }
    }
}
