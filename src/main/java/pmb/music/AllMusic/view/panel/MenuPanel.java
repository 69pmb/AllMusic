package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
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

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.XML.NgExportXml;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.GetProperties;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.BasicFrame;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;

/**
 * Classe pour le menu de l'application.
 * 
 * @author pmbroca
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

		final JMenu fichier = fichierMenu();

		// Edition
		final JMenu edition = edititonMenu();

		// Affichage
		final JMenu aff = affichageMenu();

		// Aide
		final JMenu aide = helpMenu();

		menuBar.add(fichier);
		menuBar.add(edition);
		menuBar.add(aff);
		menuBar.add(aide);

		LOG.debug("End menuBar");
		return menuBar;
	}

	private JMenu fichierMenu() {
		final JMenu fichier = new JMenu("Fichier");
		fichier.setMnemonic(KeyEvent.VK_F);

		final JMenuItem log = new JMenuItem("Ouvrir le fichier de Log");
		fichier.add(log);
		log.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		log.addActionListener((ActionEvent ae) -> {
			try {
				FichierUtils.openFileInNotepad(Optional.of(Constant.FILE_LOG_PATH), Optional.empty());
			} catch (MyException e) {
				LOG.error("Error when opening log file", e);
			}
		});

		final JMenuItem config = new JMenuItem("Ouvrir le fichier de configuration");
		fichier.add(config);
		config.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		config.addActionListener((ActionEvent ae) -> {
			try {
				FichierUtils.openFileInNotepad(Optional.of(Constant.getConfigPath()), Optional.empty());
			} catch (MyException e) {
				LOG.error("Error when opening config file", e);
			}
		});

		final JMenuItem modif = new JMenuItem("Ouvrir le fichier de modification");
		fichier.add(modif);
		modif.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		modif.addActionListener((ActionEvent ae) -> {
			try {
				FichierUtils.openFileInNotepad(Optional.of(Constant.MODIF_FILE_PATH), Optional.empty());
			} catch (MyException e) {
				LOG.error("Error when opening modif file", e);
			}
		});

		final JMenuItem outputDir = new JMenuItem("Ouvrir le dossier de sortie");
		fichier.add(outputDir);
		outputDir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		outputDir.addActionListener((ActionEvent ae) -> {
			try {
				Desktop.getDesktop().open(new File(Constant.getOutputDir()));
			} catch (IOException e) {
				LOG.error("Error when opening output directory", e);
			}
		});

		final JMenuItem xmlDir = new JMenuItem("Ouvrir le dossier des fichiers XML");
		fichier.add(xmlDir);
		xmlDir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		xmlDir.addActionListener((ActionEvent ae) -> {
			try {
				Desktop.getDesktop().open(new File(Constant.getXmlPath()));
			} catch (IOException e) {
				LOG.error("Error when opening XML files directory", e);
			}
		});

		final JMenuItem txtDir = new JMenuItem("Ouvrir le dossier Musique");
		fichier.add(txtDir);
		txtDir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		txtDir.addActionListener((ActionEvent ae) -> {
			try {
				Desktop.getDesktop().open(new File(Constant.getMusicAbsDirectory()));
			} catch (IOException e) {
				LOG.error("Error when opening txt files directory", e);
			}
		});

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
		return fichier;
	}

	private JMenu edititonMenu() {
		final JMenu edition = new JMenu("Edition");
		edition.setMnemonic(KeyEvent.VK_E);

		final JMenuItem export = new JMenuItem("Exporter le fichier final");
		export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		export.addActionListener((ActionEvent ae) -> {
			new Thread(() -> {
				try {
					NgExportXml.ngExportXml(ImportXML.importXML(Constant.getFinalFilePath()), Constant.getFinalFile());
					JOptionPane.showMessageDialog(null, "Final File successfully exported.", "",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {
					LOG.error("Export of final file for Angular failed", e);
					ExceptionDialog exceptionDialog = new ExceptionDialog("Export of final file for Angular failed", e.getMessage(), e);
					exceptionDialog.setVisible(true);
				}
			}).start();
		});
		edition.add(export);

		final JMenuItem reloadProperties = new JMenuItem("Recharger le fichier de configuration");
		reloadProperties.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		reloadProperties.addActionListener((ActionEvent ae) -> {
			if (GetProperties.reloadProperties()) {
				JOptionPane.showMessageDialog(null, "Properties successfully reloaded.", "",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Error when reloading properties.", "", JOptionPane.ERROR_MESSAGE);
			}
		});
		edition.add(reloadProperties);

		return edition;
	}

	private JMenu affichageMenu() {
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
		return aff;
	}

	private JMenu helpMenu() {
		final JMenu aide = new JMenu("Aide");
		aide.setMnemonic(KeyEvent.VK_H);
		final JMenuItem help = new JMenuItem("?");
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		help.addActionListener((ActionEvent ae) -> JOptionPane.showMessageDialog(null,
				"Ce logiciel permet de gérer les classements et palmarès de chansons et d'albums.\n"
						+ "Il a été developpé par M. Pierre-Marie Broca de janvier 2017 à septembre 2018.",
				"HELP", JOptionPane.INFORMATION_MESSAGE));
		aide.add(help);
		return aide;
	}

	public BasicFrame getMyFrame() {
		return myFrame;
	}

	public void setMyFrame(BasicFrame myFrame) {
		this.myFrame = myFrame;
	}

}
