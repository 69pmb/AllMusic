package pmb.music.AllMusic;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.BasicFrame;
import pmb.music.AllMusic.view.ExceptionDialog;

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
		Arrays.asList(args).stream().forEach(LOG::debug);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				LOG.error("An uncaught exception has been thrown: ", e);
				ExceptionDialog ed = new ExceptionDialog("An uncaught exception has been thrown", e.getMessage(), e);
				ed.setVisible(true);
			};
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOG.debug("Fin de AllMusic");
			Optional<String> savedLogFile = FichierUtils.saveLogFileIfNotEmpty();
			if (savedLogFile.isPresent()) {
				try {
					FichierUtils.openFileInNotepad(savedLogFile);
				} catch (MyException e1) {
					LOG.error("Erreur lors l'ouverture du fichier de log historisé: " + savedLogFile);
				}
			}
		}));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			LOG.error("Impossible d'appliquer le style demandé", e);
		}
		EventQueue.invokeLater(() -> {
			boolean withArtist = args.length > 0 ? Boolean.parseBoolean(args[0]) : true;
			final BasicFrame f = new BasicFrame(withArtist);
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
			f.setExtendedState(JFrame.MAXIMIZED_BOTH);
			try {
				f.setLocation(null);
			} catch (NullPointerException e) {
				LOG.debug("NPE");
			}
			SwingUtilities.updateComponentTreeUI(f);
			f.pack();
			f.setVisible(true);
			LOG.debug("Look: " + UIManager.getLookAndFeel());
		});
	}
}
