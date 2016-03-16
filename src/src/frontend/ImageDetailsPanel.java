package frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ImageDetailsPanel extends JPanel {

	final JFormattedTextField fullPathField, filenameField, imageWidthField, imageHeightField;
	final private static int MIN_TEXT_FIELD_WIDTH = 30, MIN_TEXT_FIELD_HEIGHT = 5;

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

		filenameField = new JFormattedTextField();
		filenameField.setColumns(10);
		filenameField.setEditable(false);
		filenameField.setMinimumSize(new Dimension(MIN_TEXT_FIELD_WIDTH, MIN_TEXT_FIELD_HEIGHT));
		filenameField.setBorder(BorderFactory.createEmptyBorder());
		rightTopDetails.add(filenameField);

		fullPathField = new JFormattedTextField();
		fullPathField.setColumns(10);
		fullPathField.setEditable(false);
		fullPathField.setMinimumSize(new Dimension(MIN_TEXT_FIELD_WIDTH, MIN_TEXT_FIELD_HEIGHT));
		fullPathField.setBorder(BorderFactory.createEmptyBorder());
		rightTopDetails.add(fullPathField);

		imageWidthField = new JFormattedTextField();
		imageWidthField.setColumns(10);
		imageWidthField.setEditable(false);
		imageWidthField.setMinimumSize(new Dimension(MIN_TEXT_FIELD_WIDTH, MIN_TEXT_FIELD_HEIGHT));
		imageWidthField.setBorder(BorderFactory.createEmptyBorder());
		rightTopDetails.add(imageWidthField);

		imageHeightField = new JFormattedTextField();
		imageHeightField.setColumns(10);
		imageHeightField.setEditable(false);
		imageHeightField.setMinimumSize(new Dimension(MIN_TEXT_FIELD_WIDTH, MIN_TEXT_FIELD_HEIGHT));
		imageHeightField.setBorder(BorderFactory.createEmptyBorder());
		rightTopDetails.add(imageHeightField);

		final JLabel lblFilename = new JLabel("Filename");
		lblFilename.setLabelFor(filenameField);
		leftTopDetails.add(lblFilename);

		final JLabel lblFullPath = new JLabel("Full path");
		lblFullPath.setLabelFor(fullPathField);
		leftTopDetails.add(lblFullPath);

		final JLabel lblImageWidth = new JLabel("Width");
		lblImageWidth.setLabelFor(imageWidthField);
		leftTopDetails.add(lblImageWidth);

		final JLabel lblImageHeight = new JLabel("Height");
		lblImageHeight.setLabelFor(imageHeightField);
		leftTopDetails.add(lblImageHeight);
	}

	public void setDetails(ImageListCell cell) {
		filenameField.setText(cell.getFilename());
		fullPathField.setText(cell.getFullPath());
		imageWidthField.setText(Integer.toString(cell.getImageWidth()) + " px");
		imageHeightField.setText(Integer.toString(cell.getImageHeight()) + " px");
	}
}
