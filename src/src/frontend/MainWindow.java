package frontend;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import backend.Login;

public class MainWindow {

    private static final String IMG_FIND_PNG = "/img/find.png",
            IMG_PEOPLE_PNG = "/img/people.png", IMG_EXIT_PNG = "/img/exit.png",
            IMG_RELOAD_PNG = "/img/reload.png",
            IMG_REMOVE_PNG = "/img/remove.png", IMG_SAVE_PNG = "/img/save.png",
            IMG_OPEN_PNG = "/img/open.png",
            FACES_DATABASE_PATH = "../../faces/YaleFacedatabaseA",
            TITLE = "Eigenfaces - Face identification program";
    private static final int WIDTH = 1024, HEIGHT = 768, MIN_WINDOW_WIDTH = 300,
            MIN_WINDOW_HEIGHT = 100, MIN_IMAGE_ID = 1;
    private static int latelyAssignedId = MIN_IMAGE_ID;
    private final DefaultListModel<ImageListCell> facesListModel = new DefaultListModel<ImageListCell>();
    private JFrame frame;
    private JList<ImageListCell> listAllFaces;
    private ZoomableImagePanelWrapper previewPane;
    private ImageDetailsPanel detailsPanel;

    public static void main(String[] args) {
        System.out.println("Starting main thread.");
        EventQueue.invokeLater(() -> {
            System.out.println("Starting frotend thread.");
            try {
                final PasswordDialog p = new PasswordDialog(null, "Test");
                p.setLocationRelativeTo(null);
                p.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                if (p.showDialog()
                        && Login.authenticate(p.getName(), p.getPass())) {
                    final MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } else {
                    final String msg = "Sorry, wrong login or password. Exiting!";
                    JOptionPane.showMessageDialog(null, msg);
                    System.out.println(msg);
                    System.exit(0);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exiting frotend thread.");
        });
        System.out.println("Exiting main thread.");
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
        frame.setMinimumSize(
                new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));

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
                final JFileChooser c = new JFileChooser();
                c.setMultiSelectionEnabled(true);
                c.addChoosableFileFilter(new FileNameExtensionFilter(
                        "Image files", ImageIO.getReaderFileSuffixes()));
                c.setAcceptAllFileFilterUsed(false);
                final int result = c.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    addFaces(Arrays.asList(c.getSelectedFiles()));
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
                    System.out
                            .println("Saved file path: " + c.getSelectedFile());
                }
            }
        });
        mntmSave.setIcon(
                new ImageIcon(MainWindow.class.getResource(IMG_SAVE_PNG)));
        mntmSave.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mntmSave.setToolTipText("Save report in text file"); // TODO not
                                                             // implemented
                                                             // yet
        mnfile.add(mntmSave);

        // Clear all faces menu item

        final JMenuItem mntmClearAllFaces = new JMenuItem(
                new AbstractAction("Clear all") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
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
        mntmLoadPredefined.setToolTipText(
                "Remove all loaded images and load images from predefined directory which is '"
                        + FACES_DATABASE_PATH + "'");
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

        // Tabs pane

        final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        frame.getContentPane().add(tabbedPane);

        // All faces tab

        final JSplitPane splitPaneAllFaces = new JSplitPane();
        splitPaneAllFaces.setResizeWeight(0.75);
        tabbedPane.addTab("All faces",
                new ImageIcon(MainWindow.class.getResource(IMG_PEOPLE_PNG)),
                splitPaneAllFaces, "List of all faces stored in database");

        initializeAllFacesTab(tabbedPane, splitPaneAllFaces);
    }

    private void initializeAllFacesTab(final JTabbedPane tabbedPane,
            final JSplitPane splitPaneAllFaces) {

        // Create JList with all faces stored in database for 'All faces' tab

        listAllFaces = new JList<ImageListCell>(facesListModel);
        listAllFaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAllFaces.setCellRenderer(new ImageListCellRenderer());
        listAllFaces.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listAllFaces.setVisibleRowCount(0);
        listAllFaces
                .addListSelectionListener(new AllFacesListSelectionListener());

        // Scroll pane in left subtab in 'All faces' tab

        final JScrollPane scrollPaneAllFaces = new JScrollPane(listAllFaces);
        splitPaneAllFaces.setLeftComponent(scrollPaneAllFaces);

        // Details in right subtab in 'All faces' tab

        final JSplitPane splitPaneDetails = new JSplitPane();
        splitPaneDetails.setResizeWeight(0.75);
        splitPaneDetails.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneAllFaces.setRightComponent(splitPaneDetails);

        // Add details pane to bottom (right) panel

        detailsPanel = new ImageDetailsPanel();
        splitPaneDetails.setRightComponent(detailsPanel);
        detailsPanel.setTotalImagesNumber(facesListModel.size());

        // Add preview pane to upper (left) panel

        previewPane = new ZoomableImagePanelWrapper(detailsPanel);
        splitPaneDetails.setLeftComponent(previewPane);

        // Create 'Find face' tab

        final JSplitPane splitPaneFindFace = new JSplitPane();
        splitPaneFindFace.setResizeWeight(0.75);
        tabbedPane.addTab("Find face",
                new ImageIcon(MainWindow.class.getResource(IMG_FIND_PNG)),
                splitPaneFindFace, "Find face in database");

        // Load predefined faces database

        loadPredefinedImages();
    }

    private void loadPredefinedImages() {
        ArrayList<String> imagesPathsToLoad = new ArrayList<>();
        imagesPathsToLoad
                .addAll(getAllPredefinedFacesImagesPaths(FACES_DATABASE_PATH));
        Collections.sort(imagesPathsToLoad);
        addFaces(imagesPathsToLoad);
    }

    private void clearAllImages() {
        facesListModel.clear();
        detailsPanel.clearDetails(0);
        previewPane.clearImage();
        latelyAssignedId = MIN_IMAGE_ID;
        System.out.println("All faces removed from database");
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
        System.out.println(dir.getAbsolutePath());
        final File[] dirListing = dir.listFiles();
        if (dirListing == null) {
            System.out.println(
                    "Cannot load database from " + FACES_DATABASE_PATH);
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
                System.out.println(
                        imgPath + " can't be read as image. Skipping.");
                continue;
            }
            allPaths.add(imgPath);
        }
        return allPaths;
    }

    private void addFaces(ArrayList<String> imagesPaths) {
        ArrayList<File> files = new ArrayList<>(imagesPaths.size());
        for (String path : imagesPaths) {
            files.add(new File(path));
        }
        addFaces(files);
    }

    protected void addFaces(List<File> images) {
        BufferedImage bufImg = null;
        System.out.println("Loaded files' names:");
        for (final File img : images) {
            try {
                bufImg = ImageIO.read(img);
            } catch (final IOException e) {
                System.out.println("Error during reading " + img.getPath()
                        + " image! Skipping. (Details: " + e.getMessage()
                        + ").");
            }
            String path;
            try {
                path = img.getCanonicalPath();
            } catch (IOException e) {
                System.out.println("Can't get canonical path. Skipping image.");
                continue;
            }
            facesListModel.addElement(new ImageListCell(latelyAssignedId++,
                    bufImg, img.getName(), path));
            System.out.println(path);
        }
        detailsPanel.setTotalImagesNumber(facesListModel.size());
    }

    protected class AllFacesListSelectionListener
            implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            System.out.print("Selected face has changed: ");
            if (!listAllFaces.isSelectionEmpty()) {
                final ImageListCell c = listAllFaces.getSelectedValue();
                System.out.println(c.getText());
                detailsPanel.setDetails(c);
                previewPane.setImage(c.getImage());
            }
        }
    }
}