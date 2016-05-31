package frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import net.miginfocom.swing.MigLayout;

public class SettingsDialog extends JDialog {

    private static final String DIALOG_TITLE = "Algorithm settings";
    private static final int WIDTH = 500, HEIGHT = 200, COLUMNS = 20,
            MIN_ALLOWED_EIGENFACES_NUMBER = 0,
            MAX_ALLOWED_EIGENFACES_NUMBER = Integer.MAX_VALUE,
            DEFAULT_EIGENFACES_NUMBER = 0;
    private static final double MIN_ALLOWED_THRESHOLD_NUMBER = 0.0,
            MAX_ALLOWED_THRESHOLD_NUMBER = Double.MAX_VALUE,
            DEFAULT_THRESHOLD = Double.MAX_VALUE;
    private final JPanel contentPanel = new JPanel();
    private boolean pressedOk = false;
    private JFormattedTextField eigenfacesNumberFormattedTextField,
            thresholdFormattedTextField;

    /**
     * Create the dialog.
     */
    public SettingsDialog(Frame owner) {
        super(owner);
        setTitle(DIALOG_TITLE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModal(true);
        setSize(new Dimension(WIDTH, HEIGHT));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("fill", "center"));

        final String leftColumnConstraints = "alignx trailing",
                rightColumnConstraints = "wrap";

        // Eigenfaces number label

        JLabel eigenfacesNumberLabel = new JLabel("Components / eigenfaces");
        contentPanel.add(eigenfacesNumberLabel, leftColumnConstraints);

        // Formatter for eigenfaces number formatted text field

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(MIN_ALLOWED_EIGENFACES_NUMBER);
        formatter.setMaximum(MAX_ALLOWED_EIGENFACES_NUMBER);

        // Eigenfaces number formatted text field

        eigenfacesNumberFormattedTextField = new JFormattedTextField(formatter);
        eigenfacesNumberFormattedTextField
                .setToolTipText("Number of eigenfaces / components.");
        eigenfacesNumberFormattedTextField
                .setText(Integer.toString(DEFAULT_EIGENFACES_NUMBER));
        eigenfacesNumberFormattedTextField.setColumns(COLUMNS);
        contentPanel.add(eigenfacesNumberFormattedTextField,
                rightColumnConstraints);

        // Threshold label

        JLabel lblThreshold = new JLabel("Threshold");
        contentPanel.add(lblThreshold, leftColumnConstraints);

        // Formatter for eigenfaces number formatted text field

        NumberFormat format2 = NumberFormat.getInstance();
        NumberFormatter formatter2 = new NumberFormatter(format2);
        formatter2.setValueClass(Double.class);
        formatter2.setMinimum(MIN_ALLOWED_THRESHOLD_NUMBER);
        formatter2.setMaximum(MAX_ALLOWED_THRESHOLD_NUMBER);

        // Threshold formatted text field

        thresholdFormattedTextField = new JFormattedTextField(formatter2);
        thresholdFormattedTextField.setToolTipText(
                "If image's distance from identified image is greater than threshold, than image is discarded. By default it is set to Double.MAX_VALUE.");
        thresholdFormattedTextField.setText(Double.toString(DEFAULT_THRESHOLD));
        thresholdFormattedTextField.setColumns(COLUMNS);
        contentPanel.add(thresholdFormattedTextField, rightColumnConstraints);

        // Bottom panel

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        // OK button

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedOk = false;
                SettingsDialog.this.setVisible(false);
            }
        });
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        // Cancel button

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedOk = true;
                SettingsDialog.this.setVisible(false);
            }
        });
        buttonPane.add(cancelButton);
    }

    public boolean okPressed() {
        return pressedOk;
    }

    public boolean showDialog() {
        setVisible(true);
        return okPressed();
    }

    public double getThreshold() {
        return (double) thresholdFormattedTextField.getValue();
    }

    public int getEigenfacesNumber() {
        return (int) eigenfacesNumberFormattedTextField.getValue();
    }
}
