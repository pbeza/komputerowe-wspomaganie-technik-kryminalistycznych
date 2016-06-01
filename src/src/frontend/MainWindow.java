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
import javax.swing.Timer;
import javax.swing.WindowConstants;
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
class MainWindow {

    private final static Log log = Log.getLogger();
    private static final String IMG_FIND_PNG = "/img/find.png", IMG_PEOPLE_PNG = "/img/people.png",
            IMG_EXIT_PNG = "/img/exit.png", IMG_RELOAD_PNG = "/img/reload.png", IMG_REMOVE_PNG = "/img/remove.png",
            IMG_SAVE_PNG = "/img/save.png", IMG_OPEN_PNG = "/img/open.png", IMG_SEARCH_PNG = "/img/search.png",
            IMG_SETTINGS_PNG = "/img/settings.png", TITLE = "Eigenfaces - Face identification program";
    private static final int WIDTH = 1024, HEIGHT = 512, MIN_WINDOW_WIDTH = 300, MIN_WINDOW_HEIGHT = 100;
    private final DefaultListModel<ImageListCell> imagesInAllFacesTab = new DefaultListModel<ImageListCell>();
    private final DefaultListModel<ImageListCell> imagesInFindFacesTab = new DefaultListModel<ImageListCell>();
    private ZoomableImagePanelWrapper previewPaneAllFaces;
    private ZoomableImagePanelWrapper previewPaneFindFaces;
    private ImageDetailsPanel detailsPanelAllFaces;
    private ImageDetailsPanel detailsPanelFindFaces;
    private JTabbedPane tabbedPane;
    private final Eigenfaces eigenfaces = new Eigenfaces();
    private boolean duringSearchingInDatabase = false;
    private ImageListCell faceToFindInDatabase;
    private JFrame mainWindowFrame;
    private final static DatabaseConnectionManager dbConnectionManager = DatabaseConnectionManager.getInstance();

    public static void main(String[] args) {
        log.fine("Starting main thread.");
        EventQueue.invokeLater(() -> {
            log.fine("Starting frotend thread.");
            try {
                startApp();
            } catch (Throwable e) {
                log.severe("Critical error. Details: " + e.getMessage() + e.getCause());
            }
            log.fine("Exiting frotend thread.");
        });
        log.fine("Exiting main thread.");
    }

    private static void startApp() throws Throwable {
        final PasswordDialog p = new PasswordDialog(null, "Authentication");
        p.setLocationRelativeTo(null);
        p.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (p.showDialog() && Login.authenticate(p.getName(), p.getPass())) {
            final MainWindow window = new MainWindow();
            window.mainWindowFrame.setVisible(true);
        } else {
            final String msg = "Sorry, wrong login or password. Exiting!";
            JOptionPane.showMessageDialog(null, msg);
            log.info(msg);
            MainWindow.onAppClosingCleaning();
            System.exit(0);
        }
    }

    private static void onAppClosingCleaning() {
        dbConnectionManager.closeConnection();
        log.info("Exiting from application. Bye!");
        log.closeHandlers();
    }

    /**
     * @wbp.parser.entryPoint
     */
    public MainWindow() {
        initMainWindow();
        final JSplitPane splitPaneMain = initVerticalSplitPane();
        final JProgressBar progressBar = initProgressBar(splitPaneMain);
        initMenu(progressBar);
        initTabbedPane(splitPaneMain);
        final JSplitPane splitPaneAllFaces = initAllFacesSplitPane();
        initAllFacesTab(splitPaneAllFaces);
        initFoundFacesTab();
        loadPredefinedImagesToAllFacesTab(); // TODO it should be asynchronous
    }

    private void initMainWindow() {
        mainWindowFrame = new JFrame();
        mainWindowFrame.setSize(WIDTH, HEIGHT);
        mainWindowFrame.setTitle(TITLE);
        mainWindowFrame.setLocationRelativeTo(null); // center window on screen
        mainWindowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindowFrame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        mainWindowFrame.setExtendedState(mainWindowFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onAppClosingCleaning();
            }
        };
        mainWindowFrame.addWindowListener(exitListener);
    }

    private JSplitPane initVerticalSplitPane() {
        final JSplitPane verticalSplitPane = new JSplitPane();
        // TODO lower part of the split panel should have constant height
        verticalSplitPane.setDividerLocation(0.95);
        verticalSplitPane.setResizeWeight(0.98);
        verticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        mainWindowFrame.getContentPane().add(verticalSplitPane);
        return verticalSplitPane;
    }

    private JProgressBar initProgressBar(final JSplitPane splitPaneMain) {
        final JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        splitPaneMain.setBottomComponent(progressBar);
        return progressBar;
    }

    private void initMenu(final JProgressBar progressBar) {
        final JMenuBar menuBar = initMenuBar();
        final JMenu fileMenu = initFileMenu(menuBar);
        initOpenMenuItem(fileMenu);
        initSaveMenuItem(fileMenu);
        initClearAllFacesMenuItem(fileMenu);
        initLoadPredefinedMenuItem(fileMenu);
        initExitMenuItem(fileMenu);
        final JMenu identificationMenu = initIdentificationMenu(menuBar);
        final JMenuItem mntmOpenImageToFind = new JMenuItem();
        final JMenuItem mntmSearchFaceInDatabase = initSearchFaceInDatabaseMenuItem(progressBar, identificationMenu,
                mntmOpenImageToFind);
        initOpenImageToFindMenuItem(identificationMenu, mntmOpenImageToFind, mntmSearchFaceInDatabase);
        initSettingsMenuItem(identificationMenu);
    }

    private void initTabbedPane(final JSplitPane splitPaneMain) {
        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        splitPaneMain.setTopComponent(tabbedPane);
    }

    private JSplitPane initAllFacesSplitPane() {
        final JSplitPane splitPaneAllFaces = new JSplitPane();
        splitPaneAllFaces.setResizeWeight(0.75);
        tabbedPane.addTab("All faces", new ImageIcon(MainWindow.class.getResource(IMG_PEOPLE_PNG)), splitPaneAllFaces,
                "List of all faces stored in database");
        return splitPaneAllFaces;
    }

    private void initAllFacesTab(final JSplitPane splitPaneAllFaces) {
        JList<ImageListCell> imagesInAllFacesTabJList = initListWithAllFaces();
        initScrollPaneInAllFacesTab(splitPaneAllFaces, imagesInAllFacesTabJList);
        final JSplitPane splitPaneDetailsAllFaces = initSplitPaneInAllFacesTab(splitPaneAllFaces);
        initDetailsPaneInAllFacesTab(splitPaneDetailsAllFaces);
        initPreviewPaneInAllFacesTab(splitPaneDetailsAllFaces);
    }

    private JList<ImageListCell> initListWithAllFaces() {
        JList<ImageListCell> imagesInAllFacesTabJList = new JList<ImageListCell>(imagesInAllFacesTab);
        imagesInAllFacesTabJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imagesInAllFacesTabJList.setCellRenderer(new ImageListCellRenderer());
        imagesInAllFacesTabJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imagesInAllFacesTabJList.setVisibleRowCount(0);
        imagesInAllFacesTabJList.addListSelectionListener(e -> {
            log.fine("Selected face has changed: ");
            if (!imagesInAllFacesTabJList.isSelectionEmpty()) {
                ImageListCell c = imagesInAllFacesTabJList.getSelectedValue();
                log.fine(c.getText());
                detailsPanelAllFaces.setDetails(c);
                previewPaneAllFaces.setImage(c.getImage());
            }
        });
        return imagesInAllFacesTabJList;
    }

    private void initFoundFacesTab() {
        final JSplitPane splitPaneFindFaces = new JSplitPane();
        splitPaneFindFaces.setResizeWeight(0.75);
        tabbedPane.addTab("Found faces", new ImageIcon(MainWindow.class.getResource(IMG_FIND_PNG)), splitPaneFindFaces,
                "Find face in database");

        // Create JList with all faces stored in database for 'Find faces' tab

        final JList<ImageListCell> imagesInFindFacesTabJList = new JList<ImageListCell>(imagesInFindFacesTab);
        imagesInFindFacesTabJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imagesInFindFacesTabJList.setCellRenderer(new ImageListCellRenderer());
        imagesInFindFacesTabJList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imagesInFindFacesTabJList.setVisibleRowCount(0);
        // listFindFaces.addListSelectionListener(new
        // AllFacesListSelectionListener()); // TODO na pewno inny listener

        // Scroll pane in left subtab in 'Find faces' tab

        initScrollPaneInAllFacesTab(splitPaneFindFaces, imagesInFindFacesTabJList);

        final JSplitPane splitPaneDetailsFindFaces = initSplitPaneInAllFacesTab(splitPaneFindFaces);

        // Add details pane to bottom (right) panel TODO what if not details
        // panel in find face

        detailsPanelFindFaces = new ImageDetailsPanel();
        splitPaneDetailsFindFaces.setRightComponent(detailsPanelFindFaces);
        detailsPanelFindFaces.setTotalImagesNumber(imagesInFindFacesTab.size());

        // Add preview pane to upper (left) panel of Find face

        previewPaneFindFaces = new ZoomableImagePanelWrapper(detailsPanelFindFaces);
        splitPaneDetailsFindFaces.setLeftComponent(previewPaneFindFaces);
    }

    private void loadPredefinedImagesToAllFacesTab() {
        log.info("Opening connection with database and fetching all faces from database");
        List<FaceEntity> allFaces = dbConnectionManager.getAllFaces();
        log.info("Connection with database closed - " + allFaces.size() + " images fetched");
        addFacesToAllFacesTab(allFaces);
    }

    private void initScrollPaneInAllFacesTab(final JSplitPane splitPaneAllFaces,
            JList<ImageListCell> imagesInAllFacesTabJList) {
        final JScrollPane scrollPaneAllFaces = new JScrollPane(imagesInAllFacesTabJList);
        splitPaneAllFaces.setLeftComponent(scrollPaneAllFaces);
    }

    private JSplitPane initSplitPaneInAllFacesTab(final JSplitPane splitPaneAllFaces) {
        final JSplitPane splitPaneDetailsAllFaces = new JSplitPane();
        splitPaneDetailsAllFaces.setResizeWeight(0.75);
        splitPaneDetailsAllFaces.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneAllFaces.setRightComponent(splitPaneDetailsAllFaces);
        return splitPaneDetailsAllFaces;
    }

    private void initDetailsPaneInAllFacesTab(final JSplitPane splitPaneDetailsAllFaces) {
        detailsPanelAllFaces = new ImageDetailsPanel();
        splitPaneDetailsAllFaces.setRightComponent(detailsPanelAllFaces);
        detailsPanelAllFaces.setTotalImagesNumber(imagesInAllFacesTab.size());
    }

    private void initPreviewPaneInAllFacesTab(final JSplitPane splitPaneDetailsAllFaces) {
        previewPaneAllFaces = new ZoomableImagePanelWrapper(detailsPanelAllFaces);
        splitPaneDetailsAllFaces.setLeftComponent(previewPaneAllFaces);
    }

    private void initSettingsMenuItem(final JMenu identificationMenu) {
        JMenuItem mntmAlgorithmSettingsMenuItem = new JMenuItem(new AbstractAction("Settings") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final SettingsDialog p = new SettingsDialog(mainWindowFrame);
                p.setLocationRelativeTo(null);
                p.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                if (p.showDialog()) {
                    // TODO update settings
                    log.info("TODO: update settings");
                }
            }
        });
        mntmAlgorithmSettingsMenuItem.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_SETTINGS_PNG)));
        mntmAlgorithmSettingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        mntmAlgorithmSettingsMenuItem.setToolTipText("Open eigenfaces algorithm settings");
        identificationMenu.add(mntmAlgorithmSettingsMenuItem);
    }

    private void initOpenImageToFindMenuItem(final JMenu identificationMenu, final JMenuItem mntmOpenImageToFind,
            final JMenuItem mntmSearchFaceInDatabase) {
        mntmOpenImageToFind.setAction(new AbstractAction("Open image") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (duringSearchingInDatabase) {
                    String msg = "Currently we're searching in database, change face after processing";
                    log.fine(msg);
                    JOptionPane.showMessageDialog(null, msg);
                } else {
                    JFileChooser c = new JFileChooser(System.getProperty("user.dir"));
                    c.setMultiSelectionEnabled(false);
                    c.addChoosableFileFilter(
                            new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
                    c.setAcceptAllFileFilterUsed(false);
                    if (c.showOpenDialog(mainWindowFrame) == JFileChooser.APPROVE_OPTION) {
                        String fname = "uknown";
                        try {
                            File f = c.getSelectedFile();
                            fname = f.getName();
                            setImageToFind(f);
                        } catch (IOException e1) {
                            log.warning("Error during reading " + fname + " image! Details: " + e1.getMessage()
                                    + e1.getCause() + ".");
                        }
                        mntmSearchFaceInDatabase.setEnabled(true);
                    }
                }
            }
        });
        mntmOpenImageToFind.setText("Open");
        mntmOpenImageToFind.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_OPEN_PNG)));
        mntmOpenImageToFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
        mntmOpenImageToFind.setToolTipText("Open image of face to find in the database");
        identificationMenu.add(mntmOpenImageToFind);
    }

    private JMenuItem initSearchFaceInDatabaseMenuItem(final JProgressBar progressBar, final JMenu identificationMenu,
            final JMenuItem mntmOpenImageToFind) {
        final JMenuItem mntmSearchForFaceInDatabase = new JMenuItem(new AbstractAction("Search face") {

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
                    imagesInFindFacesTab.clear();
                    progressBar.setValue(0);
                    new DatabaseFaceSearcherSwingWorker().execute();
                }
            }

            class DatabaseFaceSearcherSwingWorker extends SwingWorker<Void, Integer> {

                private Eigenfaces.PredictedResult predictedResult;
                private boolean trainingFailed = false;
                private String failureDetails;
                private final static int TIMER_DELAY = 100;
                private final Timer timer = new Timer(TIMER_DELAY, e -> {
                    int progress = getProgress() + 1;
                    if (progress < 100 && !isDone()) {
                        setProgress(progress);
                        publish(progress);
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                });

                @Override
                public Void doInBackground() {
                    timer.start();
                    searchInDatabase();
                    timer.stop();
                    return null;
                }

                private void searchInDatabase() {
                    log.info("Starting training...");
                    if (!train()) {
                        String errMsg = "Training model has failed. See log file to see details";
                        log.severe(errMsg + failureDetails);
                        publish(0);
                        JOptionPane.showMessageDialog(null, errMsg);
                    } else {
                        log.info("Training database done, starting processing identified image");
                        predictedResult = predictLabel();
                        addFacesOfFoundLabel();
                        publish(100);
                    }
                }

                private void addFacesOfFoundLabel() {
                    for (int i = 0; i < imagesInAllFacesTab.size(); i++) {
                        ImageListCell c = imagesInAllFacesTab.get(i);
                        FaceEntity f = c.getFaceEntity();
                        if (f.getPersonId() == predictedResult.predictedLabel) {
                            imagesInFindFacesTab.addElement(c);
                        }
                    }
                }

                private Eigenfaces.PredictedResult predictLabel() {
                    // TODO remove filepath - use BufferedImage from
                    // faceToFindInDatabase instead mangling with paths
                    String facePath = faceToFindInDatabase.getFilepath();
                    Mat m = null;
                    try {
                        m = FaceEntity.convertGifToMat(facePath);
                    } catch (IOException e) {
                        log.severe("Converting from GIF to matrix has failed");
                    }
                    return eigenfaces.predictFaces(m);
                }

                private boolean train() {
                    List<FaceEntity> l = new ArrayList<>(imagesInAllFacesTab.size());
                    Enumeration<ImageListCell> e = imagesInAllFacesTab.elements();
                    while (e.hasMoreElements()) {
                        ImageListCell c = e.nextElement();
                        FaceEntity f = c.getFaceEntity();
                        l.add(f);
                    }
                    try {
                        eigenfaces.train(l);
                    } catch (IOException e1) {
                        trainingFailed = true;
                        failureDetails = e1.getMessage() + e1.getCause();
                    }
                    return !trainingFailed;
                }

                @Override
                protected void done() {
                    duringSearchingInDatabase = false;
                    mntmOpenImageToFind.setEnabled(true);
                    setEnabled(true);
                    final String msg = predictedResult == null
                            ? "Prediction has failed (probably low level JNI OpenCV Face module error)."
                            : "Face is most similar to face number " + predictedResult.predictedLabel
                                    + ". Images distance equals " + predictedResult.predictedConfidence + ".";
                    JOptionPane.showMessageDialog(null, msg);
                }

                @Override
                protected void process(List<Integer> progressValues) {
                    int lastProgressValue = progressValues.get(progressValues.size() - 1);
                    progressBar.setValue(lastProgressValue);
                }
            }
        });
        mntmSearchForFaceInDatabase.setText("Find");
        mntmSearchForFaceInDatabase.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_SEARCH_PNG)));
        mntmSearchForFaceInDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        mntmSearchForFaceInDatabase.setToolTipText("Search for face in database");
        mntmSearchForFaceInDatabase.setEnabled(false);
        identificationMenu.add(mntmSearchForFaceInDatabase);
        return mntmSearchForFaceInDatabase;
    }

    private JMenu initIdentificationMenu(final JMenuBar menuBar) {
        final JMenu identificationMenu = new JMenu("Identification");
        identificationMenu.setMnemonic('I');
        menuBar.add(identificationMenu);
        return identificationMenu;
    }

    private void initExitMenuItem(final JMenu fileMenu) {
        final JMenuItem mntmExit = new JMenuItem(new AbstractAction("Exit") {

            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindowFrame.dispatchEvent(new WindowEvent(mainWindowFrame, WindowEvent.WINDOW_CLOSING));
            }
        });
        mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        mntmExit.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_EXIT_PNG)));
        mntmExit.setToolTipText("Close the application");
        fileMenu.add(mntmExit);
    }

    private void initLoadPredefinedMenuItem(final JMenu fileMenu) {
        final JMenuItem mntmLoadPredefined = new JMenuItem(new AbstractAction("Load predefined") {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllImagesInAllFacesTab();
                loadPredefinedImagesToAllFacesTab();
            }
        });
        mntmLoadPredefined.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
        mntmLoadPredefined.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_RELOAD_PNG)));
        mntmLoadPredefined.setToolTipText("Reload images from database");
        fileMenu.add(mntmLoadPredefined);
    }

    private void initClearAllFacesMenuItem(final JMenu fileMenu) {
        final JMenuItem mntmClearAllFaces = new JMenuItem(new AbstractAction("Clear all") {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO implement removing from database
                log.info("[TODO] Removing all faces from database is not implemented yet");
                clearAllImagesInAllFacesTab();
            }
        });
        mntmClearAllFaces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
        mntmClearAllFaces.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_REMOVE_PNG)));
        mntmClearAllFaces.setToolTipText("Remove all images from faces database");
        fileMenu.add(mntmClearAllFaces);
    }

    private void initSaveMenuItem(final JMenu fileMenu) {
        final JMenuItem mntmSave = new JMenuItem(new AbstractAction("Save") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser c = new JFileChooser();
                final int result = c.showSaveDialog(mainWindowFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // TODO add saving file code
                    // log.info("Saved file path: " + c.getSelectedFile());
                    log.info("[TODO] Saving results is not implemented yet");
                }
            }
        });
        mntmSave.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_SAVE_PNG)));
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mntmSave.setToolTipText("Save result in text file");
        fileMenu.add(mntmSave);
    }

    private void initOpenMenuItem(final JMenu fileMenu) {
        final JMenuItem mntmOpen = new JMenuItem(new AbstractAction("Open") {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser c = new JFileChooser(System.getProperty("user.dir"));
                c.setMultiSelectionEnabled(true);
                c.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
                c.setAcceptAllFileFilterUsed(false);
                final int result = c.showOpenDialog(mainWindowFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // TODO adding faces to existing database
                    // addFaces(Arrays.asList(c.getSelectedFiles()));
                    eigenfaces.setModelTrained(false);
                    log.info("Adding images to database is not implemented");
                }
            }
        });
        mntmOpen.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_OPEN_PNG)));
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        mntmOpen.setToolTipText("Open image files and add them to faces database");
        fileMenu.add(mntmOpen);
    }

    private JMenu initFileMenu(final JMenuBar menuBar) {
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);
        return fileMenu;
    }

    private JMenuBar initMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        mainWindowFrame.setJMenuBar(menuBar);
        return menuBar;
    }

    private void clearAllImagesInAllFacesTab() {
        imagesInAllFacesTab.clear();
        detailsPanelAllFaces.clearDetails(0);
        previewPaneAllFaces.clearImage();
        ImageListCell.resetImagesNumbering();
        log.info("All faces removed from all faces tab");
    }

    private void addFacesToAllFacesTab(List<FaceEntity> allFaces) {
        log.info("Loaded from database face entities names:");
        for (FaceEntity f : allFaces) {
            ImageListCell cell = new ImageListCell(f);
            imagesInAllFacesTab.addElement(cell);
            log.finer(cell.getFilename());
        }
        detailsPanelAllFaces.setTotalImagesNumber(imagesInAllFacesTab.size());
    }

    private void setImageToFind(File img) throws IOException {
        log.finer("Loaded image " + img.getAbsolutePath() + " to find in database");
        faceToFindInDatabase = new ImageListCell(img);
        detailsPanelFindFaces.setDetails(faceToFindInDatabase);
        previewPaneFindFaces.setImage(faceToFindInDatabase.getImage());
        tabbedPane.setSelectedIndex(1);
    }
}