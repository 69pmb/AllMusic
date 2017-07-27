package pmb.music.AllMusic;

import java.awt.EventQueue;

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

	private AllMusic() {
	}

	/**
	 * La méthode d'entrée du programme.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.debug("Lancement de AllMusic");
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				LOG.debug("Fin de AllMusic");
				// Si je veux ajouter un traitement qui se lance quand l'appli
				// se ferme
			}
		}));
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			LOG.error("Impossible d'appliquer le style demandé", e);
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final BasicFrame f = new BasicFrame();
				JFrame.setDefaultLookAndFeelDecorated(true);
				JDialog.setDefaultLookAndFeelDecorated(true);
				f.setExtendedState(JFrame.MAXIMIZED_BOTH);
				try {
					f.setLocation(null);
				} catch (NullPointerException e) {
					LOG.debug(e.getMessage());
				}
				f.pack();
				f.setVisible(true);
			}
		});
	}
}
