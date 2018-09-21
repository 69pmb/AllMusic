package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.BasicFrame;

/**
 * Classe pour le menu de l'application.
 * 
 */
public class MenuPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BasicFrame myFrame;
	private static final Logger LOG = Logger.getLogger(MenuPanel.class);

	/**
	 * Instentiation du menu.
	 * 
	 * @param myFrame
	 */
	public MenuPanel(final BasicFrame myFrame) {
		this.myFrame = myFrame;
		initComponents();
	}

	public BasicFrame getFrame() {
		return myFrame;
	}

	public void setBasicFrame(final BasicFrame basicFrame) {
		myFrame = basicFrame;
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		final JMenuBar menu = menuBar();
		myFrame.getContentPane().add(menu, BorderLayout.NORTH);
	}

	/**
	 * Création de la barre de menu.
	 * 
	 * @return le {@link JMenuBar} crée
	 */
	public JMenuBar menuBar() {
		LOG.debug("Start menuBar");
		final JMenuBar menuBar = new JMenuBar();

		// Fichier
		final JMenu fichier = new JMenu("Fichier");
		fichier.setMnemonic(KeyEvent.VK_F);

		final JMenuItem excel = new JMenuItem("Ouvrir Fichier Excel");
		fichier.add(excel);
		excel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		final JMenuItem exportXml = new JMenuItem("Exporter en XML");
		fichier.add(exportXml);
		exportXml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

		final JMenuItem calculStats = new JMenuItem("Calculer Statistique");
		fichier.add(calculStats);
		calculStats.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		final JMenuItem search = new JMenuItem("Rechercher");
		fichier.add(search);
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		search.addActionListener((ActionEvent ae) -> getSelectedTab());

		final JMenuItem triDate = new JMenuItem("Trier par date");
		fichier.add(triDate);

		final JMenuItem close = new JMenuItem("Fermer");
		fichier.add(close);
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		close.addActionListener((ActionEvent ae) -> {
			getMyFrame().getTab().getOnglets().getSelectedIndex();
			final int option = JOptionPane.showConfirmDialog(null, "Voulez-vous VRAIMENT quitter ?",
					"Demande confirmation ", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (option == 0) {
				LOG.debug("Exit");
				System.exit(0);
			} else {
				// Nothing to do
			}
		});

		// Edition
		final JMenu edition = new JMenu("Edition");
		edition.setMnemonic(KeyEvent.VK_E);

		final JMenuItem add = new JMenuItem("Ajouter une ligne");
		add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		edition.add(add);

		final JMenuItem remove = new JMenuItem("Supprimer les lignes sélectionnées");
		remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		edition.add(remove);

		final JMenuItem addItem = new JMenuItem("Ajouter une sortie");
		addItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		edition.add(addItem);

		final JMenuItem addGroupe = new JMenuItem("Ajouter un groupe à des sorties");
		addGroupe.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		edition.add(addGroupe);

		final JMenuItem removeGroupe = new JMenuItem("Supprimer tous les groupes des sorties sélectionnées");
		removeGroupe.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		edition.add(removeGroupe);

		// Affichage
		final JMenu aff = new JMenu("Affichage");
		aff.setMnemonic(KeyEvent.VK_A);
		Arrays.stream(UIManager.getInstalledLookAndFeels()).forEach(laf -> {
			JMenuItem mi = aff.add(new JMenuItem(laf.getName()));
			mi.addActionListener(e -> {
				try {
					Optional<LookAndFeelInfo> found = Arrays.stream(UIManager.getInstalledLookAndFeels())
							.filter(look -> laf.getName().equals(look.getName())).findFirst();
					if (found.isPresent()) {
						UIManager.setLookAndFeel(found.get().getClassName());
						SwingUtilities.updateComponentTreeUI(myFrame);
						myFrame.pack();
					}
				} catch (Exception ex) {
					// If error, fall back to cross-platform
					LOG.error("Impossible d'appliquer le style choisi", ex);
					try {
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e1) {
						LOG.error("Impossible d'appliquer le style par defaut", e1);
					}
				}
			});
		});

		// Aide
		final JMenu aide = new JMenu("Aide");
		aide.setMnemonic(KeyEvent.VK_H);
		final JMenuItem help = new JMenuItem("?");
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		help.addActionListener((ActionEvent ae) -> JOptionPane.showMessageDialog(null,
				"Ce logiciel permet de gérer les classements et palmarès de chansons et d'albums.\n"
						+ "Il a été developpé de janvier 2017 à septembre 2018.",
				"HELP", JOptionPane.INFORMATION_MESSAGE));
		aide.add(help);

		menuBar.add(fichier);
		menuBar.add(edition);
		menuBar.add(aff);
		menuBar.add(aide);

		LOG.debug("End menuBar");
		return menuBar;
	}

	public BasicFrame getMyFrame() {
		return myFrame;
	}

	public void setMyFrame(BasicFrame myFrame) {
		this.myFrame = myFrame;
	}

	private String getSelectedTab() {
		int index = getMyFrame().getTab().getOnglets().getSelectedIndex();
		String tab = "";
		switch (index) {
		case 0:
			tab = Constant.ONGLET_SEARCH;
			break;
		case 1:
			tab = Constant.ONGLET_ARTIST;
			break;
		case 2:
			tab = Constant.ONGLET_FICHIER;
			break;
		case 3:
			tab = Constant.ONGLET_IMPORT;
			break;
		case 4:
			tab = Constant.ONGLET_BATCH;
			break;
		default:
			break;
		}
		LOG.debug(tab);
		return tab;
	}

}
