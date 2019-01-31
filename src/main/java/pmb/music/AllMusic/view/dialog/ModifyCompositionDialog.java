/**
 * 
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.RecordType;

/**
 * Une "pop-up" permettant de modifier une composition.
 * 
 * @see {@link JDialog}
 * @author pmbroca
 */
public class ModifyCompositionDialog extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;
	private static final Logger LOG = Logger.getLogger(ModifyCompositionDialog.class);
	private final Vector<String> compo;
	private boolean sendData;
	private int artistIndex;
	private int titleIndex;
	private int typeIndex;
	private int deleteIndex;

	/**
	 * Constructeur de {@link ModifyCompositionDialog}.
	 * 
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param dim {@link Dimension} les dimension de la popup
	 * @param compo {@link Vector} la compo à modifier
	 * @param artistIndex index de l'artiste dans la composition
	 * @param titleIndex index du titre dans la composition
	 * @param typeIndex index du type dans la composition
	 * @param deleteIndex index du boolean deleted
	 */
	public ModifyCompositionDialog(JFrame parent, String header, boolean modal, Dimension dim, Vector<String> compo,
			int artistIndex, int titleIndex, int typeIndex, int deleteIndex) {
		super(parent, header, modal);
		LOG.debug("Start ModifyCompositionDialog");
		this.artistIndex = artistIndex;
		this.titleIndex = titleIndex;
		this.typeIndex = typeIndex;
		this.deleteIndex = deleteIndex;
		this.setSize(dim);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.compo = new Vector<>(compo);
		this.setResizable(true);
		initComposant();
		this.getRootPane().registerKeyboardAction(e -> this.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		LOG.debug("End ModifyCompositionDialog");
	}

	private void initComposant() {
		LOG.debug("Start initComposant");
		// Artiste
		JPanel artistPanel = new JPanel();
		artistPanel.setPreferredSize(new Dimension(250, 60));
		JLabel artistLabel = new JLabel("Artiste : ");
		JTextField artist = new JTextField((String) compo.get(artistIndex));
		artist.setPreferredSize(new Dimension(230, 30));
		artistPanel.add(artistLabel);
		artistPanel.add(artist);

		// Titre
		JPanel titrePanel = new JPanel();
		titrePanel.setPreferredSize(new Dimension(300, 60));
		JLabel titreLabel = new JLabel("Titre : ");
		JTextField titre = new JTextField((String) compo.get(titleIndex));
		titre.setPreferredSize(new Dimension(270, 30));
		titrePanel.add(titreLabel);
		titrePanel.add(titre);

		// Type
		JPanel typePanel = new JPanel();
		typePanel.setPreferredSize(new Dimension(180, 60));
		JLabel typeLabel = new JLabel("Type : ");
		JComboBox<RecordType> type = new JComboBox<>(RecordType.values());
		type.setPreferredSize(new Dimension(150, 25));
		type.setSelectedItem(RecordType.valueOf((String) compo.get(typeIndex)));
		typePanel.add(typeLabel);
		typePanel.add(type);

		// Deleted
		JPanel deletedPanel = new JPanel();
		deletedPanel.setPreferredSize(new Dimension(120, 60));
		JLabel deletedLabel = new JLabel("Supprimé : ");
		JCheckBox deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(100, 25));
		deleted.setSelected(Boolean.parseBoolean((String) compo.get(deleteIndex)));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);

		JPanel content = new JPanel();
		content.add(artistPanel);
		content.add(titrePanel);
		content.add(typePanel);
		content.add(deletedPanel);

		JPanel control = new JPanel();
		JButton okBouton = new JButton("OK");

		okBouton.addActionListener((ActionEvent arg0) -> {
			setVisible(false);
			sendData = true;
			compo.set(artistIndex, artist.getText());
			compo.set(titleIndex, titre.getText());
			compo.set(typeIndex, type.getSelectedItem().toString());
			compo.set(deleteIndex, String.valueOf(deleted.isSelected()));
		});

		JButton cancelBouton = new JButton("Annuler");
		cancelBouton.addActionListener((ActionEvent arg0) -> {
			setVisible(false);
			sendData = false;
		});

		control.add(okBouton);
		control.add(cancelBouton);

		this.getContentPane().add(content, BorderLayout.CENTER);
		this.getContentPane().add(control, BorderLayout.SOUTH);
		LOG.debug("End initComposant");
	}

	/**
	 * Affiche une {@link ModifyCompositionDialog}.
	 */
	public void showModifyCompositionDialog() {
		LOG.debug("Start showModifyCompositionDialog");
		this.sendData = false;
		this.setVisible(true);
		LOG.debug("End showModifyCompositionDialog");
	}

	/**
	 * @return la composition à modifier
	 */
	public Vector<String> getCompo() {
		return compo;
	}

	/**
	 * @return if true the user has validate the modifications
	 */
	public boolean isSendData() {
		return sendData;
	}
}
