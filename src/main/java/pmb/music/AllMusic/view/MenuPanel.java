package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

public class MenuPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BasicFrame myFrame;

	public MenuPanel(final BasicFrame myFrame) {
		this.myFrame = myFrame;
		initComponents();
	}

	public BasicFrame getFrame() {
		return myFrame;
	}

	public void setBasicFrame(final BasicFrame BasicFrame) {
		myFrame = BasicFrame;
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		final JMenuBar menu = MenuBar();
		myFrame.getContentPane().add(menu, BorderLayout.NORTH);
	}

	public JMenuBar MenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		final JMenu fichier = new JMenu("Fichier");
		final JMenu edition = new JMenu("Edition");
		final JMenu aff = new JMenu("Affichage");
		final JMenu aide = new JMenu("Aide");
		fichier.setMnemonic(KeyEvent.VK_F);
		edition.setMnemonic(KeyEvent.VK_E);
		aff.setMnemonic(KeyEvent.VK_A);
		aide.setMnemonic(KeyEvent.VK_H);

		final JMenuItem help = new JMenuItem("?");
		final JMenuItem excel = new JMenuItem("Ouvrir Fichier Excel");
		final JMenuItem exportXml = new JMenuItem("Exporter en XML");
		final JMenuItem close = new JMenuItem("Fermer");
		final JMenuItem calculStats = new JMenuItem("Calculer Statistique");
		final JMenuItem triDate = new JMenuItem("Trier par date");
		final JMenuItem add = new JMenuItem("Ajouter une ligne");
		final JMenuItem search = new JMenuItem("Rechercher");
		final JMenuItem remove = new JMenuItem("Supprimer les lignes sélectionnées");
		final JMenuItem addItem = new JMenuItem("Ajouter une sortie");
		final JMenuItem addGroupe = new JMenuItem("Ajouter un groupe à des sorties");
		final JMenuItem removeGroupe = new JMenuItem("Supprimer tous les groupes des sorties sélectionnées");

		final JRadioButtonMenuItem tout = new JRadioButtonMenuItem("Afficher tout les groupes");

		excel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		addItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		exportXml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		addGroupe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));
		calculStats.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		removeGroupe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK + KeyEvent.SHIFT_DOWN_MASK));

		fichier.add(excel);

		fichier.add(exportXml);

		fichier.add(calculStats);

		fichier.add(search);

		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final int option = JOptionPane.showConfirmDialog(null, "Voulez-vous VRAIMENT quitter ?", "Demande confirmation ",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (option == 0) {
					System.exit(0);
				} else {
				}
			}
		});

		fichier.add(triDate);
		fichier.add(close);

		edition.add(add);

		edition.add(addItem);

		edition.add(remove);

		edition.add(addGroupe);

		edition.add(removeGroupe);

		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				JOptionPane.showMessageDialog(null, "Ce logiciel permet de gérer les classements et palmarès de chansons et d'albums.\n"
						+ "Il a été developpé par M. Pierre-Marie Broca de janvier 2017 à XXX.", "HELP", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		aide.add(help);

		menuBar.add(fichier);
		menuBar.add(edition);
		menuBar.add(aff);
		menuBar.add(aide);
		aff.add(tout);

		return menuBar;
	}

	public BasicFrame getMyFrame() {
		return myFrame;
	}

	public void setMyFrame(BasicFrame myFrame) {
		this.myFrame = myFrame;
	}

}
