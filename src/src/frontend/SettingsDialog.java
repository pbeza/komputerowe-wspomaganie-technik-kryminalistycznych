package frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import backend.Log;
import net.miginfocom.swing.MigLayout;

class SettingsDialog extends JDialog {

    private final static Logger log = Log.getLogger();
    private static final String DIALOG_TITLE = "Algorithm settings", LEFT_COLUMN_CONSTRAINTS = "alignx trailing",
            RIGHT_COLUMN_CONSTRAINTS = "wrap";
    private static final int WIDTH = 500, HEIGHT = 200, EMPTY_BORDER_MARGIN = 5, MIN_ALLOWED_EIGENFACES_NUMBER = 0,
            MAX_ALLOWED_EIGENFACES_NUMBER = Integer.MAX_VALUE, DEFAULT_ALLOWED_EIGENFACES_NUMBER = 0,
            FACES_SPINNER_STEP_SIZE = 1, MIN_MAX_NUMBER_OF_DISPLAYED_RESULTS = 1;
    public static final int MAX_MAX_NUMBER_OF_DISPLAYED_RESULTS = 1000, DEFAULT_MAX_NUMBER_OF_DISPLAYED_RESULTS = 30,
            MAX_DISPLAYED_FACES_STEP_SIZE = 1, SPINNER_WIDTH = 200;
    private static final double DEFAULT_ALLOWED_THRESHOLD = Double.MAX_VALUE, MIN_ALLOWED_THRESHOLD = 1.0,
            MAX_ALLOWED_THRESHOLD = Double.MAX_VALUE, THRESHOLD_SPINNER_STEP_SIZE = 0.1;
    private final JPanel contentPanel = new JPanel();
    private boolean pressedOk = false;
    private final JSpinner eigenfacesJSpinner = new JSpinner(), thresholdJSpinner = new JSpinner(),
            maxNumbersOfDisplayedResultsJSpinner = new JSpinner();

    /**
     * Create the dialog.
     */
    public SettingsDialog(Frame owner, int eigenfacesNumber, double threshold, int maxNumbersOfDisplayedResults) {
        super(owner);
        setTitle(DIALOG_TITLE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModal(true);
        setSize(new Dimension(WIDTH, HEIGHT));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(
                new EmptyBorder(EMPTY_BORDER_MARGIN, EMPTY_BORDER_MARGIN, EMPTY_BORDER_MARGIN, EMPTY_BORDER_MARGIN));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("fill", "left"));

        addEigenfacesNumberRow(eigenfacesNumber);
        addThresholdRow(threshold);
        addMaxNumbersOfDisplayedResults(maxNumbersOfDisplayedResults);

        // Bottom panel

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        // OK button

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(e -> {
            try {
                thresholdJSpinner.commitEdit();
                eigenfacesJSpinner.commitEdit();
            } catch (Exception e1) {
                log.warning("Commiting settings value has failed.");
                return;
            }
            if (thresholdJSpinner.getValue() == null) {
                JOptionPane.showMessageDialog(null, "Threshold value must be nonegative floating value.");
            } else if (eigenfacesJSpinner.getValue() == null) {
                JOptionPane.showMessageDialog(null, "Eigenfaces number must be nonegative integer.");
            } else {
                pressedOk = true;
                SettingsDialog.this.setVisible(false);
            }
        });
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        // Cancel button

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(e -> {
            pressedOk = false;
            SettingsDialog.this.setVisible(false);
        });
        buttonPane.add(cancelButton);
    }

    private void setSpinnerWidth(JSpinner spinner) {
        JComponent field = spinner.getEditor();
        Dimension prefSize = field.getPreferredSize();
        prefSize = new Dimension(SPINNER_WIDTH, prefSize.height);
        field.setPreferredSize(prefSize);
    }

    private void addThresholdRow(double threshold) {
        JLabel lblThreshold = new JLabel("Threshold");
        contentPanel.add(lblThreshold, LEFT_COLUMN_CONSTRAINTS);

        thresholdJSpinner.setToolTipText(
                "If distance between identified image and best found face's image is greater than threshold, than image is discarded and won't be displayed. Default value is maximum possible which is Double.MAX_VALUE.");
        thresholdJSpinner.setValue(threshold);
        thresholdJSpinner.setModel(new SpinnerNumberModel(DEFAULT_ALLOWED_THRESHOLD, MIN_ALLOWED_THRESHOLD,
                MAX_ALLOWED_THRESHOLD, THRESHOLD_SPINNER_STEP_SIZE));
        thresholdJSpinner.setEditor(new JSpinner.NumberEditor(thresholdJSpinner, "0.######E0"));
        contentPanel.add(thresholdJSpinner, RIGHT_COLUMN_CONSTRAINTS);

        setSpinnerWidth(thresholdJSpinner);
    }

    private void addEigenfacesNumberRow(int eigenfacesNumber) {
        JLabel eigenfacesNumberLabel = new JLabel("Components / eigenfaces");
        contentPanel.add(eigenfacesNumberLabel, LEFT_COLUMN_CONSTRAINTS);

        eigenfacesJSpinner.setToolTipText(
                "Number of eigenfaces (components). 0 is handled in special way: it indicates that OpenCV should use its default value for face's components number.");
        eigenfacesJSpinner.setValue(eigenfacesNumber);
        eigenfacesJSpinner.setModel(new SpinnerNumberModel(DEFAULT_ALLOWED_EIGENFACES_NUMBER,
                MIN_ALLOWED_EIGENFACES_NUMBER, MAX_ALLOWED_EIGENFACES_NUMBER, FACES_SPINNER_STEP_SIZE));
        eigenfacesJSpinner.setEditor(new JSpinner.NumberEditor(eigenfacesJSpinner));
        contentPanel.add(eigenfacesJSpinner, RIGHT_COLUMN_CONSTRAINTS);

        setSpinnerWidth(eigenfacesJSpinner);
    }

    private void addMaxNumbersOfDisplayedResults(int maxNumbersOfDisplayedResults) {
        JLabel maxNumbersOfDisplayedResultsLabel = new JLabel("Max number of displayed results");
        contentPanel.add(maxNumbersOfDisplayedResultsLabel, LEFT_COLUMN_CONSTRAINTS);

        maxNumbersOfDisplayedResultsJSpinner.setToolTipText("Maximum number of displayed results.");
        maxNumbersOfDisplayedResultsJSpinner.setValue(maxNumbersOfDisplayedResults);
        maxNumbersOfDisplayedResultsJSpinner.setModel(
                new SpinnerNumberModel(DEFAULT_MAX_NUMBER_OF_DISPLAYED_RESULTS, MIN_MAX_NUMBER_OF_DISPLAYED_RESULTS,
                        MAX_MAX_NUMBER_OF_DISPLAYED_RESULTS, MAX_DISPLAYED_FACES_STEP_SIZE));
        maxNumbersOfDisplayedResultsJSpinner.setEditor(new JSpinner.NumberEditor(maxNumbersOfDisplayedResultsJSpinner));
        contentPanel.add(maxNumbersOfDisplayedResultsJSpinner, RIGHT_COLUMN_CONSTRAINTS);

        setSpinnerWidth(maxNumbersOfDisplayedResultsJSpinner);
    }

    private boolean okPressed() {
        return pressedOk;
    }

    boolean showDialog() {
        setVisible(true);
        return okPressed();
    }

    public double getThreshold() {
        return (double) thresholdJSpinner.getValue();
    }

    public int getEigenfacesNumber() {
        return (int) eigenfacesJSpinner.getValue();
    }

    public int getMaxNumberOfDisplayedResults() {
        return (int) maxNumbersOfDisplayedResultsJSpinner.getValue();
    }
}
