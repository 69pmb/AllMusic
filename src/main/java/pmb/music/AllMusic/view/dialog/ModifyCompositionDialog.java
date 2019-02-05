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

import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.view.ComponentBuilder;

/**
 * Une "pop-up" permettant de modifier une composition.
 * 
 * @see {@link JDialog}
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
	 * @param compo {@link Vector} la compo à modifier
	 * @param artistIndex index de l'artiste dans la composition
	 * @param titleIndex index du titre dans la composition
	 * @param typeIndex index du type dans la composition
	 * @param deleteIndex index du boolean deleted
	 */
	public ModifyCompositionDialog(Vector<String> compo, int artistIndex, int titleIndex, int typeIndex,
			int deleteIndex) {
		super((JFrame) null, "Modifier une composition", true);
		LOG.debug("Start ModifyCompositionDialog");
		this.artistIndex = artistIndex;
		this.titleIndex = titleIndex;
		this.typeIndex = typeIndex;
		this.deleteIndex = deleteIndex;
		this.setSize(new Dimension(950, 150));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.compo = new Vector<>(compo);
		this.setResizable(true);
		initComposant();
		this.getRootPane().registerKeyboardAction(e -> this.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		LOG.debug("End ModifyCompositionDialog");
	}

	@SuppressWarnings("unchecked")
	private void initComposant() {
		LOG.debug("Start initComposant");
		JPanel content = new JPanel();

		// Artiste
		JPanel artistPanel = new JPanel();
		artistPanel.setPreferredSize(new Dimension(250, 60));
		JLabel artistLabel = new JLabel("Artiste : ");
		JTextField artist = new JTextField((String) compo.get(artistIndex));
		artist.setPreferredSize(new Dimension(230, 30));
		artistPanel.add(artistLabel);
		artistPanel.add(artist);
		content.add(artistPanel);

		// Titre
		JPanel titrePanel = new JPanel();
		titrePanel.setPreferredSize(new Dimension(300, 60));
		JLabel titreLabel = new JLabel("Titre : ");
		JTextField titre = new JTextField((String) compo.get(titleIndex));
		titre.setPreferredSize(new Dimension(270, 30));
		titrePanel.add(titreLabel);
		titrePanel.add(titre);
		content.add(titrePanel);

		// Type
		JComboBox<RecordType> type = (JComboBox<RecordType>) new ComponentBuilder<RecordType>(JComboBox.class)
				.withParent(content).withPanelWidth(180).withLabel("Type : ").withValues(RecordType.values())
				.withInitialValue(RecordType.valueOf((String) compo.get(typeIndex))).withComponentWidth(150).build();

		// Deleted
		JCheckBox deleted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(content)
				.withLabel("Supprimé : ").withPanelWidth(120).withComponentWidth(100)
				.withInitialValue(Boolean.parseBoolean((String) compo.get(deleteIndex))).build();

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
