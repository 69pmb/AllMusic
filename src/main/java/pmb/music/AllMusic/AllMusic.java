package pmb.music.AllMusic;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.view.BasicFrame;

/**
 * Hello world!
 * 
 */
public class AllMusic {

	private static final Logger LOG = Logger.getLogger(AllMusic.class);

	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				// try {
				// ExportXML.exportXML(ImportXML.getAllCompo(FINAL_FILE_PATH),
				// "final");
				// } catch (NullPointerException e) {
				// } catch (Exception e) {
				// LOG.error("", e);
				// }
			}
		}));
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			LOG.error("", e);
		}
		// ImportXML.getInstance(FINAL_FILE_PATH);
		// List<Composition> fusionFiles =
		// ImportXML.fusionFiles(System.getProperty("user.dir") +
		// RESOURCES_DIRECTORY);
		final BasicFrame f = new BasicFrame();
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		dim.height = 19 * dim.height / 20;
		// f.setPreferredSize(dim);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		try {
			f.setLocation(null);
		} catch (NullPointerException e) {
		}
		f.pack();
		f.setVisible(true);
	}
}
