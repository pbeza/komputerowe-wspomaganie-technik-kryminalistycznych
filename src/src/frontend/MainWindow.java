package frontend;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.Mat;

import backend.Eigenfaces;
import backend.Log;
import backend.Login;
import backend.db.DatabaseConnectionManager;
import backend.db.FaceEntity;

/**
 * Main window of the application. This window contains a few JPanels.
 */
public class MainWindow {

    private final static Log log = Log.getLogger();
    private static final String IMG_FIND_PNG = "/img/find.png",
            IMG_PEOPLE_PNG = "/img/people.png", IMG_EXIT_PNG = "/img/exit.png",
            IMG_RELOAD_PNG = "/img/reload.png",
            IMG_REMOVE_PNG = "/img/remove.png", IMG_SAVE_PNG = "/img/save.png",
            IMG_OPEN_PNG = "/img/open.png", IMG_SEARCH_PNG = "/img/search.png",
            TITLE = "Eigenfaces - Face identification program";
    private static final int WIDTH = 1024, HEIGHT = 512, MIN_WINDOW_WIDTH = 300,
            MIN_WINDOW_HEIGHT = 100;
    private final DefaultListModel<ImageListCell> imagesInAllFacesTab = new DefaultListModel<ImageListCell>();
    private final JList<ImageListCell> imagesInAllFacesTabJList;
    private final DefaultListModel<ImageListCell> imagesInFindFacesTab = new DefaultListModel<ImageListCell>();
    private final ZoomableImagePanelWrapper previewPaneAllFaces;
    private final ZoomableImagePanelWrapper previewPaneFindFaces;
    private final ImageDetailsPanel detailsPanelAllFaces;
    private final ImageDetailsPanel detailsPanelFindFaces;
    private final JTabbedPane tabbedPane;
    private final Eigenfaces eigenfaces = new Eigenfaces();
    private boolean duringSearchingInDatabase = false;
    private ImageListCell faceToFindInDatabase;
    private JFrame frame;
    private final static DatabaseConnectionManager dbConnectionManager = DatabaseConnectionManager
            .getInstance();

    public static void main(String[] args) {
        log.fine("Starting main thread.");
        EventQueue.invokeLater(() -> {
            log.fine("Starting frotend thread.");
            try {
                final PasswordDialog p = new PasswordDialog(null,
                        "Authentication");
                p.setLocationRelativeTo(null);
                p.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                if (p.showDialog()
                        && Login.authenticate(p.getName(), p.getPass())) {
                    final MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } else {
                    final String msg = "Sorry, wrong login or password. Exiting!";
                    JOptionPane.showMessageDialog(null, msg);
                    log.info(msg);
                    MainWindow.onAppClosingCleaning();
                    System.exit(0);
                }
            } catch (Exception e) {
                log.severe("Critical error. Details: " + e.getMessage());
            }
            log.fine("Exiting frotend thread.");
        });
        log.fine("Exiting main thread.");
    }

    public static void onAppClosingCleaning() {
        dbConnectionManager.closeConnection();
        log.info("Exiting from application. Bye!");
        log.closeHandlers();
    }

    /**
     * @wbp.parser.entryPoint
     */
    public MainWindow() {

        // Main window

        frame = new JFrame();
        frame.setSize(WIDTH, HEIGHT);
        frame.setTitle(TITLE);
        frame.setLocationRelativeTo(null); // center window on screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(
                new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onAppClosingCleaning();
            }
        };
        frame.addWindowListener(exitListener);

        // Menu bar

        final JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // File menu item

        final JMenu mnfile = new JMenu("File");
        mnfile.setMnemonic('F');
        menuBar.add(mnfile);

        // Open menu item

        final JMenuItem mntmOpen = new JMenuItem(new AbstractAction("Open") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser c = new JFileChooser(
                        System.getProperty("user.dir"));
                c.setMultiSelectionEnabled(true);
                c.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Image files", ImageIO.getReaderFileSuffixes()));
                c.setAcceptAllFileFilterUsed(false);
                final int result = c.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // TODO adding faces to existing database
                    // addFaces(Arrays.asList(c.getSelectedFiles()));
                    log.info(
                            "[TODO] Adding images to database is not implemented yet");
                }
            }
        });
        mntmOpen.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_OPEN_PNG)));
        mntmOpen.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        mntmOpen.setToolTipText(
                "Open image files and add them to faces database");
        mnfile.add(mntmOpen);

        // Save menu item

        final JMenuItem mntmSave = new JMenuItem(new AbstractAction("Save") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser c = new JFileChooser();
                final int result = c.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // TODO add saving file code
                    // log.info("Saved file path: " + c.getSelectedFile());
                    log.info("[TODO] Saving results is not implemented yet");
                }
            }
        });
        mntmSave.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_SAVE_PNG)));
        mntmSave.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mntmSave.setToolTipText("Save result in text file");
        mnfile.add(mntmSave);

        // Clear all faces menu item

        final JMenuItem mntmClearAllFaces = new JMenuItem(
                new AbstractAction("Clear all") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO implement removing from database
                        log.info(
                                "[TODO] Removing all faces from database is not implemented yet");
                        clearAllImages();
                    }
                });
        mntmClearAllFaces.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
        mntmClearAllFaces.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_REMOVE_PNG)));
        mntmClearAllFaces
                .setToolTipText("Remove all images from faces database");
        mnfile.add(mntmClearAllFaces);

        // mainSplit pane up and down

        final JSplitPane splitPaneMain = new JSplitPane();
        // TODO lower part of the split panel should have constant height
        splitPaneMain.setDividerLocation(0.95);
        splitPaneMain.setResizeWeight(0.95);
        splitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
        frame.getContentPane().add(splitPaneMain);

        // Progress bar

        final JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        splitPaneMain.setBottomComponent(progressBar);

        // Load predefined menu item

        final JMenuItem mntmLoadPredefined = new JMenuItem(
                new AbstractAction("Load predefined") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clearAllImages();
                        loadPredefinedImages();
                    }
                });
        mntmLoadPredefined.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
        mntmLoadPredefined.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_RELOAD_PNG)));
        mntmLoadPredefined.setToolTipText("Reload images from database");
        mnfile.add(mntmLoadPredefined);

        // Exit menu item

        final JMenuItem mntmExit = new JMenuItem(new AbstractAction("Exit") {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(
                        new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        mntmExit.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        mntmExit.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_EXIT_PNG)));
        mntmExit.setToolTipText("Close the application");
        mnfile.add(mntmExit);

        // File menu item

        final JMenu mnImageToFind = new JMenu("Identify");
        mnImageToFind.setMnemonic('I');
        menuBar.add(mnImageToFind);

        // Open menu item for image of face to find in database

        final JMenuItem mntmOpenImageToFind = new JMenuItem();

        // Search for face in database

        final JMenuItem mntmSearchForFaceInDatabase = new JMenuItem(
                new AbstractAction("Search face") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (faceToFindInDatabase == null) {
                            String msg = "No image set to find";
                            log.info(msg);
                            JOptionPane.showMessageDialog(null, msg);
                        } else {
                            setEnabled(false);
                            mntmOpenImageToFind.setEnabled(false);
                            duringSearchingInDatabase = true;
                            progressBar.setValue(0);
                            new DatabaseFaceSearcherSwingWorker().execute();
                        }
                    }

                    class DatabaseFaceSearcherSwingWorker
                            extends SwingWorker<Void, Integer> {

                        private int predictedLabel;

                        @Override
                        public Void doInBackground() {
                            searchInDatabase();
                            return null;
                        }

                        private void searchInDatabase() {
                            publish(10);
                            log.info("Starting training...");
                            train();
                            log.info(
                                    "Training database done, starting processing identified image");
                            publish(50);
                            predictedLabel = predictLabel();
                            publish(100);
                        }

                        private int predictLabel() {
                            // TODO remove filepath - use BufferedImage
                            // BufferedImage face =
                            // faceToFindInDatabase.getImage();
                            // int predictedLabel =
                            // eigenfaces.predictFaces(face);
                            String facePath = faceToFindInDatabase
                                    .getFilepath();
                            Mat m = null;
                            try {
                                m = FaceEntity.convertGifToMat(facePath);
                            } catch (IOException e) {
                                log.severe(
                                        "Converting from GIF to matrix has failed");
                            }
                            int predictedLabel = -1;
                            try {
                                predictedLabel = eigenfaces.predictFaces(m);
                            } catch (IOException | URISyntaxException e) {
                                log.severe("Predicting face has failed");
                            }
                            return predictedLabel;
                        }

                        private void train() {
                            List<FaceEntity> l = new ArrayList<>(
                                    imagesInAllFacesTab.size());
                            Enumeration<ImageListCell> e = imagesInAllFacesTab
                                    .elements();
                            while (e.hasMoreElements()) {
                                ImageListCell c = e.nextElement();
                                FaceEntity f = c.getFaceEntity();
                                l.add(f);
                            }
                            eigenfaces.train(l);
                        }

                        @Override
                        protected void done() {
                            duringSearchingInDatabase = false;
                            mntmOpenImageToFind.setEnabled(true);
                            setEnabled(true);
                            final String msg = "Face is most similar to face number: "
                                    + predictedLabel;
                            JOptionPane.showMessageDialog(null, msg);
                        }

                        @Override
                        protected void process(List<Integer> progressValues) {
                            for (Integer i : progressValues) {
                                progressBar.setValue(i);
                            }
                        }
                    }
                });
        mntmSearchForFaceInDatabase.setText("Find");
        mntmSearchForFaceInDatabase.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_SEARCH_PNG)));
        mntmSearchForFaceInDatabase.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        mntmSearchForFaceInDatabase
                .setToolTipText("Search for face in database");
        mntmSearchForFaceInDatabase.setEnabled(false);
        mnImageToFind.add(mntmSearchForFaceInDatabase);

        // Open menu item for image of face to find in database

        mntmOpenImageToFind.setAction(new AbstractAction("Open image") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (duringSearchingInDatabase) {
                    String msg = "Currently we're searching in database, change face after processing";
                    log.fine(msg);
                    JOptionPane.showMessageDialog(null, msg);
                } else {
                    JFileChooser c = new JFileChooser(
                            System.getProperty("user.dir"));
                    c.setMultiSelectionEnabled(false);
                    c.addChoosableFileFilter(new FileNameExtensionFilter(
                            "Image files", ImageIO.getReaderFileSuffixes()));
                    c.setAcceptAllFileFilterUsed(false);
                    if (c.showOpenDialog(
                            frame) == JFileChooser.APPROVE_OPTION) {
                        String fname = "uknown";
                        try {
                            File f = c.getSelectedFile();
                            fname = f.getName();
                            setImageToFind(f);
                        } catch (IOException e1) {
                            log.warning("Error during reading " + fname
                                    + " image! Details: " + e1.getMessage()
                                    + e1.getCause() + ".");
                        }
                        mntmSearchForFaceInDatabase.setEnabled(true);
                    }
                }
            }
        });
        mntmOpenImageToFind.setText("Open");
        mntmOpenImageToFind.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_OPEN_PNG)));
        mntmOpenImageToFind.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
        mntmOpenImageToFind
                .setToolTipText("Open image of face to find in the database");
        mnImageToFind.add(mntmOpenImageToFind);

        // Tabs pane

        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        splitPaneMain.setTopComponent(tabbedPane);

        // All faces tab

        final JSplitPane splitPaneAllFaces = new JSplitPane();
        splitPaneAllFaces.setResizeWeight(0.75);
        tabbedPane.addTab("All faces",
                new ImageIcon(MainWindow.class.getResource(IMG_PEOPLE_PNG)),
                splitPaneAllFaces, "List of all faces stored in database");

        /*
         * Initialize all faces tab
         */

        // Create JList with all faces stored in database for 'All faces' tab

        imagesInAllFacesTabJList = new JList<ImageListCell>(
                imagesInAllFacesTab);
        imagesInAllFacesTabJList
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imagesInAllFacesTabJList.setCellRenderer(new ImageListCellRenderer());
        imagesInAllFacesTabJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imagesInAllFacesTabJList.setVisibleRowCount(0);
        imagesInAllFacesTabJList
                .addListSelectionListener(new AllFacesListSelectionListener());

        // Scroll pane in left subtab in 'All faces' tab

        final JScrollPane scrollPaneAllFaces = new JScrollPane(
                imagesInAllFacesTabJList);
        splitPaneAllFaces.setLeftComponent(scrollPaneAllFaces);

        // Details in right subtab in 'All faces' tab

        final JSplitPane splitPaneDetailsAllFaces = new JSplitPane();
        splitPaneDetailsAllFaces.setResizeWeight(0.75);
        splitPaneDetailsAllFaces.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneAllFaces.setRightComponent(splitPaneDetailsAllFaces);

        // Add details pane to bottom (right) panel

        detailsPanelAllFaces = new ImageDetailsPanel();
        splitPaneDetailsAllFaces.setRightComponent(detailsPanelAllFaces);
        detailsPanelAllFaces.setTotalImagesNumber(imagesInAllFacesTab.size());

        // Add preview pane to upper (left) panel

        previewPaneAllFaces = new ZoomableImagePanelWrapper(
                detailsPanelAllFaces);
        splitPaneDetailsAllFaces.setLeftComponent(previewPaneAllFaces);

        // Create 'Find face' tab

        final JSplitPane splitPaneFindFaces = new JSplitPane();
        splitPaneFindFaces.setResizeWeight(0.75);
        tabbedPane.addTab("Find face",
                new ImageIcon(MainWindow.class.getResource(IMG_FIND_PNG)),
                splitPaneFindFaces, "Find face in database");

        // Create JList with all faces stored in database for 'All faces' tab

        final JList<ImageListCell> imagesInFindFacesTabJList = new JList<ImageListCell>(
                imagesInFindFacesTab);
        imagesInFindFacesTabJList
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imagesInFindFacesTabJList.setCellRenderer(new ImageListCellRenderer());
        imagesInFindFacesTabJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imagesInFindFacesTabJList.setVisibleRowCount(0);
        // listFindFaces.addListSelectionListener(new
        // AllFacesListSelectionListener()); // TODO na pewno inny listener

        // Scroll pane in left subtab in 'Find faces' tab

        final JScrollPane scrollPaneFindFaces = new JScrollPane(
                imagesInFindFacesTabJList);
        splitPaneFindFaces.setLeftComponent(scrollPaneFindFaces);

        // Details in right subtab in 'All faces' tab

        final JSplitPane splitPaneDetailsFindFaces = new JSplitPane();
        splitPaneDetailsFindFaces.setResizeWeight(0.75);
        splitPaneDetailsFindFaces.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneFindFaces.setRightComponent(splitPaneDetailsFindFaces);

        // Add details pane to bottom (right) panel TODO what if not details
        // panel in find face

        detailsPanelFindFaces = new ImageDetailsPanel();
        splitPaneDetailsFindFaces.setRightComponent(detailsPanelFindFaces);
        detailsPanelFindFaces.setTotalImagesNumber(imagesInFindFacesTab.size());

        // Add preview pane to upper (left) panel of Find face

        previewPaneFindFaces = new ZoomableImagePanelWrapper(
                detailsPanelFindFaces);
        splitPaneDetailsFindFaces.setLeftComponent(previewPaneFindFaces);

        // Load predefined faces database

        loadPredefinedImages();

        // Set size of progress bar

        // restoreDefaults();
    }

    // private void restoreDefaults() {
    // SwingUtilities.invokeLater(() ->
    // splitPaneMain.setDividerLocation(SPLIT_PANE_MAIN_DEVIDER));
    // }

    private void clearAllImages() {
        imagesInAllFacesTab.clear();
        detailsPanelAllFaces.clearDetails(0);
        previewPaneAllFaces.clearImage();
        ImageListCell.resetImagesNumbering();
        log.info("All faces removed from database");
    }

    /**
     * Load images from database
     */
    private void loadPredefinedImages() {
        log.info(
                "Opening connection with database and fetching all faces from database");
        List<FaceEntity> allFaces = dbConnectionManager.getAllFaces();
        log.info("Connection with database closed - " + allFaces.size()
                + " images fetched");
        addFaces(allFaces);
    }

    /**
     * Add images of faces from list of files to {@code facesListModel}
     */
    protected void addFaces(List<FaceEntity> allFaces) {
        log.info("Loaded from database face entities names:");
        for (FaceEntity f : allFaces) {
            ImageListCell cell = new ImageListCell(f);
            imagesInAllFacesTab.addElement(cell);
            log.finer(cell.getFilename());
        }
        detailsPanelAllFaces.setTotalImagesNumber(imagesInAllFacesTab.size());
    }

    // protected void addFacesFromFiles(List<File> images) {
    // log.info("Loaded files' names:");
    // for (File img : images) {
    // ImageListCell cell = createImageCell(img);
    // if (cell != null) {
    // imagesInAllFacesTab.addElement(cell);
    // log.finer(cell.getFilename());
    // }
    // }
    // detailsPanelAllFaces.setTotalImagesNumber(imagesInAllFacesTab.size());
    // }

    private void setImageToFind(File img) throws IOException {
        log.finer("Loaded image " + img.getAbsolutePath()
                + " to find in database");
        faceToFindInDatabase = new ImageListCell(img); // TODO add label ID to
                                                       // constructor's call
        detailsPanelFindFaces.setDetails(faceToFindInDatabase);
        previewPaneFindFaces.setImage(faceToFindInDatabase.getImage());
        tabbedPane.setSelectedIndex(1);
    }

    /**
     * When we select image, then set details and preview
     */
    protected class AllFacesListSelectionListener
            implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            log.fine("Selected face has changed: ");
            if (!imagesInAllFacesTabJList.isSelectionEmpty()) {
                ImageListCell c = imagesInAllFacesTabJList.getSelectedValue();
                log.fine(c.getText());
                detailsPanelAllFaces.setDetails(c);
                previewPaneAllFaces.setImage(c.getImage());
            }
        }
    }
}