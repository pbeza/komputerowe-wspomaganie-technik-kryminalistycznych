package frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import backend.db.FaceEntity;

/**
 * Represents panel displaying details of the image.
 */
class ImageDetailsPanel extends JPanel {

    final private static int MIN_TEXT_FIELD_WIDTH = 30, MIN_TEXT_FIELD_HEIGHT = 5;
    private final JFormattedTextField idField = new JFormattedTextField(), filenameField = new JFormattedTextField(),
            personIdField = new JFormattedTextField(), imageIdField = new JFormattedTextField(),
            mimeTypeField = new JFormattedTextField(), imageWidthField = new JFormattedTextField(),
            imageHeightField = new JFormattedTextField(), zoomField = new JFormattedTextField(),
            totalImagesNumberField = new JFormattedTextField(), timestampField = new JFormattedTextField();
    private final JLabel lblId = new JLabel("ID"), lblFilename = new JLabel("Filename"),
            lblPersonId = new JLabel("Person ID"), lblImageId = new JLabel("Image ID"),
            lblMimeType = new JLabel("MIME type"), lblImageWidth = new JLabel("Width"),
            lblImageHeight = new JLabel("Height"), lblZoom = new JLabel("Zoom"),
            lblTotalImagesNumber = new JLabel("Total images"), lblTimestamp = new JLabel("Timestamp");
    private int totalImagesNumber;

    public ImageDetailsPanel() {
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout());

        final JPanel leftTopDetails = new JPanel();
        add(leftTopDetails, BorderLayout.WEST);
        leftTopDetails.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        leftTopDetails.setLayout(new GridLayout(0, 1));

        final JPanel rightTopDetails = new JPanel();
        add(rightTopDetails, BorderLayout.CENTER);
        rightTopDetails.setLayout(new GridLayout(0, 1));

        final JFormattedTextField[] fieldsToAdd = { idField, personIdField, imageIdField, filenameField, mimeTypeField,
                imageWidthField, imageHeightField, zoomField, totalImagesNumberField, timestampField };

        final JLabel[] labelsToAdd = { lblId, lblPersonId, lblImageId, lblFilename, lblMimeType, lblImageWidth,
                lblImageHeight, lblZoom, lblTotalImagesNumber, lblTimestamp };

        assert labelsToAdd.length == fieldsToAdd.length;

        for (int i = 0; i < fieldsToAdd.length; i++) {
            fieldsToAdd[i].setColumns(10);
            fieldsToAdd[i].setEditable(false);
            fieldsToAdd[i].setMinimumSize(new Dimension(MIN_TEXT_FIELD_WIDTH, MIN_TEXT_FIELD_HEIGHT));
            fieldsToAdd[i].setBorder(BorderFactory.createEmptyBorder());
            rightTopDetails.add(fieldsToAdd[i]);
            labelsToAdd[i].setLabelFor(fieldsToAdd[i]);
            leftTopDetails.add(labelsToAdd[i]);
        }
    }

    public void setDetails(ImageListCell cell) {
        int personId = cell.getPersonId();
        String personIdString = personId == FaceEntity.UNKNOWN_PERSON_ID ? "unknown" : Integer.toString(personId);
        int imageId = cell.getImageId();
        String imageIdString = imageId == FaceEntity.UNKNOWN_IMAGE_ID ? "unknown" : Integer.toString(imageId);
        Timestamp timestamp = cell.getTimestamp();
        String timestampString = timestamp == null ? "unknown" : timestamp.toString();
        idField.setText(Integer.toString(cell.getDisplayedId()));
        filenameField.setText(cell.getFilename());
        personIdField.setText(personIdString);
        imageIdField.setText(imageIdString);
        mimeTypeField.setText(cell.getMimeType());
        imageWidthField.setText(Integer.toString(cell.getImageWidth()) + " px");
        imageHeightField.setText(Integer.toString(cell.getImageHeight()) + " px");
        zoomField.setText("100%");
        totalImagesNumberField.setText(Integer.toString(totalImagesNumber));
        timestampField.setText(timestampString);
    }

    public void clearDetails(int totalImagesNumber) {
        final JFormattedTextField[] fieldsToClear = { idField, filenameField, personIdField, imageWidthField,
                imageHeightField, zoomField };
        for (JFormattedTextField f : fieldsToClear) {
            f.setText("");
        }
        setTotalImagesNumber(totalImagesNumber);
    }

    public void setTotalImagesNumber(int totalImagesNumber) {
        this.totalImagesNumber = totalImagesNumber;
        totalImagesNumberField.setText(Integer.toString(totalImagesNumber));
    }

    public void setZoom(double zoomPercent) {
        zoomField.setText(String.format("%.2f", zoomPercent));
    }

    public void setPersonId(String personId) {
        personIdField.setText(personId);
    }
}
