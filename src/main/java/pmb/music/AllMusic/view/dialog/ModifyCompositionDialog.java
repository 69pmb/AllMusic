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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.view.ComponentBuilder;

/**
 * Une "pop-up" permettant de modifier une composition.
 * 
 * @see {@link JDialog}
 * @author pmbroca
 */
public class ModifyCompositionDialog {
	private static final Logger LOG = LogManager.getLogger(ModifyCompositionDialog.class);
	private JDialog dialog;
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
		LOG.debug("Start ModifyCompositionDialog");
		this.dialog = new JDialog((JFrame) null, "Modifier une composition", true);
		this.artistIndex = artistIndex;
		this.titleIndex = titleIndex;
		this.typeIndex = typeIndex;
		this.deleteIndex = deleteIndex;
		this.dialog.setSize(new Dimension(950, 150));
		this.dialog.setLocationRelativeTo(null);
		this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.compo = new Vector<>(compo);
		this.dialog.setResizable(true);
		initComposant();
		this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		LOG.debug("End ModifyCompositionDialog");
	}

	@SuppressWarnings("unchecked")
	private void initComposant() {
		LOG.debug("Start initComposant");
		JPanel content = new JPanel();

		// Artiste
		JTextField artist = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
				.withLabel("Artiste : ").withPanelWidth(250).withInitialValue((String) compo.get(artistIndex))
				.withComponentWidth(230).withLabelWidth(230).build();

		// Titre
		JTextField titre = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
				.withLabel("Titre : ").withPanelWidth(300).withInitialValue((String) compo.get(titleIndex))
				.withComponentWidth(270).withLabelWidth(270).build();

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
			dialog.setVisible(false);
			sendData = true;
			compo.set(artistIndex, artist.getText());
			compo.set(titleIndex, titre.getText());
			compo.set(typeIndex, type.getSelectedItem().toString());
			compo.set(deleteIndex, String.valueOf(deleted.isSelected()));
		});

		JButton cancelBouton = new JButton("Annuler");
		cancelBouton.addActionListener((ActionEvent arg0) -> {
			dialog.setVisible(false);
			sendData = false;
		});

		control.add(okBouton);
		control.add(cancelBouton);

		this.dialog.getContentPane().add(content, BorderLayout.CENTER);
		this.dialog.getContentPane().add(control, BorderLayout.SOUTH);
		LOG.debug("End initComposant");
	}

	/**
	 * Affiche une {@link ModifyCompositionDialog}.
	 */
	public void showModifyCompositionDialog() {
		LOG.debug("Start showModifyCompositionDialog");
		this.sendData = false;
		this.dialog.setVisible(true);
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
