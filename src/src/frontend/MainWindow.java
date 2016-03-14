package frontend;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainWindow {

	private JFrame frame;
	private static final int WIDTH = 1024, HEIGHT = 768;
	private static final String TITLE = "Eigen faces";
	private File[] images;

	/**
	 * Launch the application.
	 */
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
	 * Create the application.
	 *
	 * @wbp.parser.entryPoint
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setSize(WIDTH, HEIGHT);
		frame.setTitle(TITLE);
		frame.setLocationRelativeTo(null); // center window on screen
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		final JMenu mnfile = new JMenu("File");
		mnfile.setMnemonic('F');
		menuBar.add(mnfile);

		final JMenuItem mntmOpen = new JMenuItem(new AbstractAction("Open") {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser c = new JFileChooser();
				c.setMultiSelectionEnabled(true);
				c.addChoosableFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
				c.setAcceptAllFileFilterUsed(false);
				final int result = c.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					images = c.getSelectedFiles();
					System.out.println("Loaded files' names:");
					for (final File f : images) {
						System.out.println(f);
					}
				}
			}
		});
		mntmOpen.setIcon(new ImageIcon(MainWindow.class.getResource("/img/open.png")));
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnfile.add(mntmOpen);

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
		mnfile.add(mntmSave);

		final JMenuItem mntmExit = new JMenuItem(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mntmExit.setIcon(new ImageIcon(MainWindow.class.getResource("/img/exit.png")));
		mnfile.add(mntmExit);

		final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		frame.getContentPane().add(tabbedPane);

		final JScrollPane scrollPaneAllFaces = new JScrollPane();
		tabbedPane.addTab("All faces", new ImageIcon(MainWindow.class.getResource("/img/people.png")),
				scrollPaneAllFaces, "List of all faces stored in database");

		final JSplitPane splitPaneFindFace = new JSplitPane();
		tabbedPane.addTab("Find face", new ImageIcon(MainWindow.class.getResource("/img/find.png")), splitPaneFindFace,
				"Find face in database");
	}

}
