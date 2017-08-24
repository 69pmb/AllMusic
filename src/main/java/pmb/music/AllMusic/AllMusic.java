package pmb.music.AllMusic;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.BasicFrame;

/**
 * Hello world!
 * 
 */
public class AllMusic {

	private static final Logger LOG = Logger.getLogger(AllMusic.class);

	private AllMusic() {
	}

	/**
	 * La méthode d'entrée du programme.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.debug("Lancement de AllMusic");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOG.debug("Fin de AllMusic");
			try (BufferedReader br = new BufferedReader(new FileReader(Constant.FILE_LOG_PATH));) {
				String line = br.readLine();
				if (line != null && StringUtils.isNotBlank(line)) {
					Runtime.getRuntime().exec(Constant.NOTEPAD_PATH + Constant.FILE_LOG_PATH);
				}
			} catch (IOException e) {
				LOG.error("Erreur lors de l'ouverture du fichier de log: " + Constant.FILE_LOG_PATH, e);
			}
		}));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			LOG.error("Impossible d'appliquer le style demandé", e);
		}
		EventQueue.invokeLater(() -> {
			final BasicFrame f = new BasicFrame();
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			f.setExtendedState(JFrame.MAXIMIZED_BOTH);
			try {
				f.setLocation(null);
			} catch (NullPointerException e) {
				LOG.debug("NPE");
			}
			f.pack();
			f.setVisible(true);
		});
	}
}
