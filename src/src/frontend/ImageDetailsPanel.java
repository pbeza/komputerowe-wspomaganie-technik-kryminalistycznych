package frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Represents panel displaying details of the image.
 */
public class ImageDetailsPanel extends JPanel {

    final private static int MIN_TEXT_FIELD_WIDTH = 30, MIN_TEXT_FIELD_HEIGHT = 5;
    final JFormattedTextField idField = new JFormattedTextField(), filenameField = new JFormattedTextField(),
            fullPathField = new JFormattedTextField(), mimeTypeField = new JFormattedTextField(),
            imageWidthField = new JFormattedTextField(), imageHeightField = new JFormattedTextField(),
            zoomField = new JFormattedTextField(), totalImagesNumberField = new JFormattedTextField();
    final JLabel lblId = new JLabel("Image ID"), lblFilename = new JLabel("Filename"),
            lblFullPath = new JLabel("Full path"), lblMimeType = new JLabel("MIME type"),
            lblImageWidth = new JLabel("Width"), lblImageHeight = new JLabel("Height"), lblZoom = new JLabel("Zoom"),
            lblTotalImagesNumber = new JLabel("Total images");
    protected int totalImagesNumber;

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

        final JFormattedTextField[] fieldsToAdd = { idField, filenameField, fullPathField, mimeTypeField,
                imageWidthField, imageHeightField, zoomField, totalImagesNumberField };

        final JLabel[] labelsToAdd = { lblId, lblFilename, lblFullPath, lblMimeType, lblImageWidth, lblImageHeight,
                lblZoom, lblTotalImagesNumber };

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
        idField.setText(String.format("%03d", cell.getId()));
        filenameField.setText(cell.getFilename());
        fullPathField.setText(""); // TODO
        fullPathField.setToolTipText(""); // TODO
        mimeTypeField.setText(cell.getMimeType());
        imageWidthField.setText(Integer.toString(cell.getImageWidth()) + " px");
        imageHeightField.setText(Integer.toString(cell.getImageHeight()) + " px");
        zoomField.setText("100%");
        totalImagesNumberField.setText(Integer.toString(totalImagesNumber));
    }

    public void clearDetails(int totalImagesNumber) {
        final JFormattedTextField[] fieldsToClear = { idField, filenameField, fullPathField, imageWidthField,
                imageHeightField, zoomField };
        for (JFormattedTextField f : fieldsToClear) {
            f.setText("");
        }
        setTotalImagesNumber(totalImagesNumber);
        fullPathField.setToolTipText("");
    }

    public void setTotalImagesNumber(int totalImagesNumber) {
        this.totalImagesNumber = totalImagesNumber;
        totalImagesNumberField.setText(Integer.toString(totalImagesNumber));
    }

    public void setZoom(double zoomPercent) {
        zoomField.setText(String.format("%.2f", zoomPercent));
    }
}
