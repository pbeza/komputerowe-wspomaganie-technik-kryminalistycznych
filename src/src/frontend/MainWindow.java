package frontend;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import backend.Log;
import backend.Eigenfaces;
import backend.Login;

public class MainWindow {

    private final static Log log = Log.getLogger();
    private static final String IMG_FIND_PNG = "/img/find.png", IMG_PEOPLE_PNG = "/img/people.png",
            IMG_EXIT_PNG = "/img/exit.png", IMG_RELOAD_PNG = "/img/reload.png", IMG_REMOVE_PNG = "/img/remove.png",
            IMG_SAVE_PNG = "/img/save.png", IMG_OPEN_PNG = "/img/open.png", IMG_SEARCH_PNG = "/img/search.png",
            FACES_DATABASE_PATH = "././faces/YaleFacedatabaseA", TITLE = "Eigenfaces - Face identification program";
    private static final int WIDTH = 1024, HEIGHT = 768, MIN_WINDOW_WIDTH = 300, MIN_WINDOW_HEIGHT = 100,
            MIN_IMAGE_ID = 1;
    private static final double SPLIT_PANE_MAIN_DEVIDER = 0.97;
    private static int latelyAssignedId = MIN_IMAGE_ID;

    JSplitPane splitPaneMain;
    /***
     * List of images(full image and UI icon), source (permanent binding) for
     * {@code listAllFaces}
     */
    private final DefaultListModel<ImageListCell> facesAllListModel = new DefaultListModel<ImageListCell>();
    /***
     * UI list of images in All Faces tab
     */
    private JList<ImageListCell> listAllFaces;
    /***
     * List of images(full image and UI icon), source (permanent binding) for
     * {@code listFindFaces}
     */
    private final DefaultListModel<ImageListCell> facesFindListModel = new DefaultListModel<ImageListCell>();
    /***
     * UI list of images in Find Faces tab
     */
    private JList<ImageListCell> listFindFaces;
    private JFrame frame;

    private ZoomableImagePanelWrapper previewPaneAllFaces;
    private ZoomableImagePanelWrapper previewPaneFindFaces;
    private ImageDetailsPanel detailsPanelAllFaces;
    private ImageDetailsPanel detailsPanelFindFaces;

    private ImageListCell faceToFindInDatabase = null;
    private Eigenfaces eigenfaces = new Eigenfaces();
    private boolean duringSearchingInDatabase = false;

    JMenuItem mntmOpenImageToFind;
    JMenuItem mntmSearchForFaceInDatabase;
    JProgressBar progressBar;

    public static void main(String[] args) {
        log.fine("Starting main thread.");
        EventQueue.invokeLater(() -> {
            log.fine("Starting frotend thread.");
            try {
                final PasswordDialog p = new PasswordDialog(null, "Test");
                p.setLocationRelativeTo(null);
                p.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                if (p.showDialog() && Login.authenticate(p.getName(), p.getPass())) {
                    final MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } else {
                    final String msg = "Sorry, wrong login or password. Exiting!";
                    JOptionPane.showMessageDialog(null, msg);
                    log.info(msg);
                    System.exit(0);
                }
            } catch (final Exception e) {
                log.severe("Critical error. Details: " + e.getMessage());
            }
            log.fine("Exiting frotend thread.");
        });
        log.fine("Exiting main thread.");
    }

    /**
     * @wbp.parser.entryPoint
     */
    public MainWindow() {
        initialize();
    }

    private void initialize() {

        // Main window

        frame = new JFrame();
        frame.setSize(WIDTH, HEIGHT);
        frame.setTitle(TITLE);
        frame.setLocationRelativeTo(null); // center window on screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.info("Exiting from application. Bye!");
                log.closeHandlers();
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
                final JFileChooser c = new JFileChooser(System.getProperty("user.dir"));
                c.setMultiSelectionEnabled(true);
                c.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
                c.setAcceptAllFileFilterUsed(false);
                final int result = c.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    addFaces(Arrays.asList(c.getSelectedFiles()));
                }
            }
        });
        mntmOpen.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_OPEN_PNG)));
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        mntmOpen.setToolTipText("Open image files and add them to faces database");
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
                }
            }
        });
        mntmSave.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_SAVE_PNG)));
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mntmSave.setToolTipText("Save report in text file");
        mnfile.add(mntmSave);

        // Clear all faces menu item

        final JMenuItem mntmClearAllFaces = new JMenuItem(new AbstractAction("Clear all") {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllImages();
            }
        });
        mntmClearAllFaces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
        mntmClearAllFaces.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_REMOVE_PNG)));
        mntmClearAllFaces.setToolTipText("Remove all images from faces database");
        mnfile.add(mntmClearAllFaces);

        // Load predefined menu item

        final JMenuItem mntmLoadPredefined = new JMenuItem(new AbstractAction("Load predefined") {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllImages();
                loadPredefinedImages();
            }
        });
        mntmLoadPredefined.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));
        mntmLoadPredefined.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_RELOAD_PNG)));
        mntmLoadPredefined
                .setToolTipText("Remove all loaded images and load images from predefined directory which is '"
                        + FACES_DATABASE_PATH + "'");
        mnfile.add(mntmLoadPredefined);

        // Exit menu item

        final JMenuItem mntmExit = new JMenuItem(new AbstractAction("Exit") {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        mntmExit.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_EXIT_PNG)));
        mntmExit.setToolTipText("Close the application");
        mnfile.add(mntmExit);

        // File menu item

        final JMenu mnImageToFind = new JMenu("Face to find");
        mnImageToFind.setMnemonic('I');
        menuBar.add(mnImageToFind);

        // Open menu item for image of face to find in database

        mntmOpenImageToFind = new JMenuItem(new AbstractAction("Open Image") {

            @Override
            public void actionPerformed(ActionEvent e) {
                log.finest("open 1 duringSearchingInDatabase=" + duringSearchingInDatabase);
                if (duringSearchingInDatabase) {
                    log.fine("Currently we search in database, change face after process");
                    return;
                }
                final JFileChooser c = new JFileChooser(System.getProperty("user.dir"));
                c.setMultiSelectionEnabled(false);
                c.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
                c.setAcceptAllFileFilterUsed(false);
                final int result = c.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    setImageToFind(c.getSelectedFile());
                    mntmSearchForFaceInDatabase.setEnabled(true);
                }
            }
        });
        mntmOpenImageToFind.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_OPEN_PNG)));
        mntmOpenImageToFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
        mntmOpenImageToFind.setToolTipText("Open image of face to find in database");
        mnImageToFind.add(mntmOpenImageToFind);

        // Search for face in database

        // final JMenuItem
        mntmSearchForFaceInDatabase = new JMenuItem(searchForFaceInDatabaseListener());
        mntmSearchForFaceInDatabase.setIcon(new ImageIcon(MainWindow.class.getResource(IMG_SEARCH_PNG)));// TODO
                                                                                                         // IMG_SEARCH_PNG
        mntmSearchForFaceInDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        mntmSearchForFaceInDatabase.setToolTipText("Search for face in database");
        mntmSearchForFaceInDatabase.setEnabled(false);
        mnImageToFind.add(mntmSearchForFaceInDatabase);

        // mainSplit pane gora i dol
        splitPaneMain = new JSplitPane();
        splitPaneMain.setDividerLocation(0.9);
        splitPaneMain.setResizeWeight(0.9);
        splitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
        frame.getContentPane().add(splitPaneMain);

        // progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        splitPaneMain.setBottomComponent(progressBar);

        // Tabs pane

        final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        splitPaneMain.setTopComponent(tabbedPane);

        // All faces tab

        final JSplitPane splitPaneAllFaces = new JSplitPane();
        splitPaneAllFaces.setResizeWeight(0.75);
        tabbedPane.addTab("All faces", new ImageIcon(MainWindow.class.getResource(IMG_PEOPLE_PNG)), splitPaneAllFaces,
                "List of all faces stored in database");

        initializeAllFacesTab(tabbedPane, splitPaneAllFaces);
    }

    private void initializeAllFacesTab(final JTabbedPane tabbedPane, final JSplitPane splitPaneAllFaces) {

        // Create JList with all faces stored in database for 'All faces' tab

        listAllFaces = new JList<ImageListCell>(facesAllListModel);
        listAllFaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAllFaces.setCellRenderer(new ImageListCellRenderer());
        listAllFaces.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listAllFaces.setVisibleRowCount(0);
        listAllFaces.addListSelectionListener(new AllFacesListSelectionListener());

        // Scroll pane in left subtab in 'All faces' tab

        final JScrollPane scrollPaneAllFaces = new JScrollPane(listAllFaces);
        splitPaneAllFaces.setLeftComponent(scrollPaneAllFaces);

        // Details in right subtab in 'All faces' tab

        final JSplitPane splitPaneDetailsAllFaces = new JSplitPane();
        splitPaneDetailsAllFaces.setResizeWeight(0.75);
        splitPaneDetailsAllFaces.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneAllFaces.setRightComponent(splitPaneDetailsAllFaces);

        // Add details pane to bottom (right) panel

        detailsPanelAllFaces = new ImageDetailsPanel();
        splitPaneDetailsAllFaces.setRightComponent(detailsPanelAllFaces);
        detailsPanelAllFaces.setTotalImagesNumber(facesAllListModel.size());

        // Add preview pane to upper (left) panel

        previewPaneAllFaces = new ZoomableImagePanelWrapper(detailsPanelAllFaces);
        splitPaneDetailsAllFaces.setLeftComponent(previewPaneAllFaces);

        // Create 'Find face' tab

        final JSplitPane splitPaneFindFaces = new JSplitPane();
        splitPaneFindFaces.setResizeWeight(0.75);
        tabbedPane.addTab("Find face", new ImageIcon(MainWindow.class.getResource(IMG_FIND_PNG)), splitPaneFindFaces,
                "Find face in database");

        // Create JList with all faces stored in database for 'All faces' tab

        listFindFaces = new JList<ImageListCell>(facesFindListModel);
        listFindFaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listFindFaces.setCellRenderer(new ImageListCellRenderer());
        listFindFaces.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listFindFaces.setVisibleRowCount(0);
        // listFindFaces.addListSelectionListener(new
        // AllFacesListSelectionListener()); TODO na pewno inny listener

        // Scroll pane in left subtab in 'Find faces' tab

        final JScrollPane scrollPaneFindFaces = new JScrollPane(listFindFaces);
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
        detailsPanelFindFaces.setTotalImagesNumber(facesFindListModel.size());

        // Add preview pane to upper (left) panel of Find face

        previewPaneFindFaces = new ZoomableImagePanelWrapper(detailsPanelFindFaces);
        splitPaneDetailsFindFaces.setLeftComponent(previewPaneFindFaces);

        // Load predefined faces database

        loadPredefinedImages();

        // set size of progress bar
        restoreDefaults();
    }

    private void restoreDefaults() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                splitPaneMain.setDividerLocation(SPLIT_PANE_MAIN_DEVIDER);
            }
        });
    }

    /***
     * load images from predefined directory path
     */
    private void loadPredefinedImages() {
        ArrayList<String> imagesPathsToLoad = new ArrayList<>();
        imagesPathsToLoad.addAll(getAllPredefinedFacesImagesPaths(FACES_DATABASE_PATH));
        Collections.sort(imagesPathsToLoad);
        addFaces(imagesPathsToLoad);
    }

    private void clearAllImages() {
        facesAllListModel.clear();
        detailsPanelAllFaces.clearDetails(0);
        previewPaneAllFaces.clearImage();
        latelyAssignedId = MIN_IMAGE_ID;
        log.info("All faces removed from database");
    }

    /**
     * Recursively search database from predefined directory. Note: Clear
     * {@code facesListModel} before using this method unless
     * {@code facesListModel} is empty.
     *
     * @throws IOException
     */
    private ArrayList<String> getAllPredefinedFacesImagesPaths(String path) {
        final ArrayList<String> allPaths = new ArrayList<>();
        final File dir = new File(path);
        final File[] dirListing = dir.listFiles();
        if (dirListing == null) {
            log.warning("Cannot load database from " + path + " : path is wrong or IO error occured");
            return allPaths;
        }
        for (final File f : dirListing) {
            String imgPath = f.getPath();
            if (f.isDirectory()) {
                allPaths.addAll(getAllPredefinedFacesImagesPaths(imgPath));
                continue;
            }
            boolean isImage = false;
            try {
                isImage = ImageIO.read(f) != null;
            } catch (IOException e) {
                isImage = false;
            }
            if (!isImage) {
                log.warning(imgPath + " can't be read as image. Skipping.");
                continue;
            }
            allPaths.add(imgPath);
        }
        return allPaths;
    }

    /***
     * Add images of faces from list of paths
     */
    private void addFaces(ArrayList<String> imagesPaths) {
        ArrayList<File> files = new ArrayList<>(imagesPaths.size());
        for (String path : imagesPaths) {
            files.add(new File(path));
        }
        addFaces(files);
    }

    /***
     * Add images of faces from list of files to {@code facesListModel}
     */
    protected void addFaces(List<File> images) {
        log.info("Loaded files' names:");
        for (final File img : images) {
            ImageListCell cell = createImageCell(img);
            if (cell != null) {
                facesAllListModel.addElement(cell);
                log.finer(cell.getFullPath());
            }
        }
        detailsPanelAllFaces.setTotalImagesNumber(facesAllListModel.size());
    }

    private ImageListCell createImageCell(File img) {
        BufferedImage bufImg = null;
        try {
            bufImg = ImageIO.read(img);
        } catch (final IOException e) {
            log.warning(
                    "Error during reading " + img.getPath() + " image! Skipping. (Details: " + e.getMessage() + ").");
            return null;
        }
        String path;
        try {
            path = img.getCanonicalPath();
        } catch (IOException e) {
            log.warning("Can't get canonical path. Skipping image.");
            return null;
        }
        log.info(path);
        return new ImageListCell(latelyAssignedId++, bufImg, img.getName(), path);
    }

    private void setImageToFind(File img) {
        log.finer("Loaded image to find in database");
        ImageListCell cell = createImageCell(img);
        if (cell != null) {
            faceToFindInDatabase = cell;
            log.finer(cell.getFullPath());
            detailsPanelFindFaces.setDetails(cell);
            previewPaneFindFaces.setImage(cell.getImage());
        }
    }

    private int getLabelFromFilePath(String facePath) {

        return 0;
    }

    /***
     * When we select image, then set details and preview
     */
    protected class AllFacesListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            log.fine("Selected face has changed: ");
            if (!listAllFaces.isSelectionEmpty()) {
                final ImageListCell c = listAllFaces.getSelectedValue();
                log.fine(c.getText());
                detailsPanelAllFaces.setDetails(c);
                previewPaneAllFaces.setImage(c.getImage());
            }
        }
    }

    private AbstractAction searchForFaceInDatabaseListener() {
        return new AbstractAction("Search face") {
            @Override
            public void actionPerformed(ActionEvent e) {

                mntmSearchForFaceInDatabase.setEnabled(false);
                mntmOpenImageToFind.setEnabled(false);
                new Thread(new Runnable() {
                    public void run() {
                        (new SearchForFaceInDatabase()).execute();
                    }
                }).start();

            }

            // SwingWorker(T,V) 
            //  T- type of param which is returned by doInBackgroud,
            //  V - parameter showing progress
            //      usage publish(V progress) 
            //      process(List<V>)- show result to UI
            class SearchForFaceInDatabase extends SwingWorker<Object, Integer> {
                @Override
                public Object doInBackground() {
                    searchInDatabase();
                    return null;
                }

                private void searchInDatabase() {
                    publish(10);
                    if (faceToFindInDatabase == null) {
                        log.info("No image to find");
                        return;
                    }
                    try {
                        eigenfaces.train();
                    } catch (IOException | URISyntaxException e2) {
                        log.warning("Exception during traning database " + e2.getMessage());
                        e2.printStackTrace();
                        return;
                    }
                    publish(50);
                    String facePath = faceToFindInDatabase.getFullPath();
                    int predictedLabel = -1;
                    try {
                        predictedLabel = eigenfaces.predictFaces(facePath);
                    } catch (IOException | URISyntaxException e1) {
                        log.warning("Exception during prediction: " + e1.getMessage());
                        return;
                    }
                    log.fine("Face is most similar to face number : " + predictedLabel);
                    publish(100);
                }

                @Override
                protected void done() {
                    try {
                        mntmOpenImageToFind.setEnabled(true);
                        mntmSearchForFaceInDatabase.setEnabled(true);
                    } catch (Exception ignore) {
                    }
                }

                @Override
                protected void process(List<Integer> progressValues) {
                    for (Integer i : progressValues) {
                        progressBar.setValue(i);
                    }
                }
            }
        };

    }
}