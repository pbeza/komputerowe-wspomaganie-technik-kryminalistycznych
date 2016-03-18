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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainWindow {

	private JFrame frame;
	private final static int WIDTH = 1024, HEIGHT = 768, MIN_WINDOW_WIDTH = 300, MIN_WINDOW_HEIGHT = 100;
	private final static String TITLE = "Eigen faces";
	private final static String FACES_DATABASE_PATH = "../faces";
	private final DefaultListModel<ImageListCell> facesListModel = new DefaultListModel<ImageListCell>();
	private JList<ImageListCell> listAllFaces;
	private ZoomableImagePanel previewPane;
	private ImageDetailsPanel detailsPanel;

	public static void main(String[] args) {
		System.out.println("Starting main thread.");
		EventQueue.invokeLater(() -> {
			System.out.println("Starting frotend thread.");
			try {
				final MainWindow window = new MainWindow();
				window.frame.setVisible(true);
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
		frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));

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
				c.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
				c.setAcceptAllFileFilterUsed(false);
				final int result = c.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					addFacesToView(c.getSelectedFiles());
				}
			}
		});
		mntmOpen.setIcon(new ImageIcon(MainWindow.class.getResource("/img/open.png")));
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
					System.out.println("Saved file path: " + c.getSelectedFile());
				}
			}
		});
		mntmSave.setIcon(new ImageIcon(MainWindow.class.getResource("/img/save.png")));
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mntmSave.setToolTipText("Save report in text file"); // TODO not
																// implemented
																// yet
		mnfile.add(mntmSave);

		// Clear all faces menu item

		final JMenuItem mntmClearAllFaces = new JMenuItem(new AbstractAction("Clear all") {

			@Override
			public void actionPerformed(ActionEvent e) {
				facesListModel.clear();
				System.out.println("All faces removed from database");
			}
		});
		mntmClearAllFaces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		mntmClearAllFaces.setIcon(new ImageIcon(MainWindow.class.getResource("/img/remove.png")));
		mntmClearAllFaces.setToolTipText("Remove all images from faces database");
		mnfile.add(mntmClearAllFaces);

		// Exit menu item

		final JMenuItem mntmExit = new JMenuItem(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mntmExit.setIcon(new ImageIcon(MainWindow.class.getResource("/img/exit.png")));
		mntmExit.setToolTipText("Close the application");
		mnfile.add(mntmExit);

		// Tabs pane

		final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		frame.getContentPane().add(tabbedPane);

		// All faces tab

		final JSplitPane splitPaneAllFaces = new JSplitPane();
		splitPaneAllFaces.setResizeWeight(0.75);
		tabbedPane.addTab("All faces", new ImageIcon(MainWindow.class.getResource("/img/people.png")),
				splitPaneAllFaces, "List of all faces stored in database");

		// Load predefined faces database

		try {
			loadPredefinedFacesDatabase();
		} catch (final IOException e) {
			System.out.println(
					"Loading predefined faces database has failed (it's not critical). Details: " + e.getMessage());
		}

		// Create JList with all faces stored in database for 'All faces' tab

		listAllFaces = new JList<ImageListCell>(facesListModel);
		listAllFaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAllFaces.setCellRenderer(new ImageListCellRenderer());
		listAllFaces.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		listAllFaces.setVisibleRowCount(0);
		listAllFaces.addListSelectionListener(new AllFacesListSelectionListener());

		// Scroll pane in left subtab in 'All faces' tab

		final JScrollPane scrollPaneAllFaces = new JScrollPane(listAllFaces);
		splitPaneAllFaces.setLeftComponent(scrollPaneAllFaces);

		// Details in right subtab in 'All faces' tab

		final JSplitPane splitPaneDetails = new JSplitPane();
		splitPaneDetails.setResizeWeight(0.75);
		splitPaneDetails.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneAllFaces.setRightComponent(splitPaneDetails);

		// Add preview pane to upper (left) panel

		previewPane = new ZoomableImagePanel();
		splitPaneDetails.setLeftComponent(previewPane);

		// Add details pane to bottom (right) panel

		detailsPanel = new ImageDetailsPanel();
		splitPaneDetails.setRightComponent(detailsPanel);

		// Create 'Find face' tab

		final JSplitPane splitPaneFindFace = new JSplitPane();
		splitPaneFindFace.setResizeWeight(0.75);
		tabbedPane.addTab("Find face", new ImageIcon(MainWindow.class.getResource("/img/find.png")), splitPaneFindFace,
				"Find face in database");
	}

	private void loadPredefinedFacesDatabase() throws IOException {
		final File dir = new File(FACES_DATABASE_PATH);
		final File[] dirListing = dir.listFiles();
		if (dirListing == null) {
			System.out.println("Cannot load database from " + FACES_DATABASE_PATH);
			return;
		}
		facesListModel.clear();
		for (final File f : dirListing) {
			final BufferedImage bufImg = ImageIO.read(f);
			final ImageListCell imgListCell = new ImageListCell(bufImg, f.getName(), f.getAbsolutePath());
			facesListModel.addElement(imgListCell);
			System.out.println(f.getAbsolutePath() + " successfully added");
		}
	}

	protected void addFacesToView(File[] images) {
		BufferedImage bufImg = null;
		System.out.println("Loaded files' names:");
		for (final File img : images) {
			try {
				bufImg = ImageIO.read(img);
			} catch (final IOException e) {
				System.out.println("Error during reading " + img.getPath() + " image! Skipping. (Details: "
						+ e.getMessage() + ").");
			}
			final String path = img.getAbsolutePath();
			facesListModel.addElement(new ImageListCell(bufImg, img.getName(), path));
			System.out.println(path);
		}
	}

	protected class AllFacesListSelectionListener implements ListSelectionListener {

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