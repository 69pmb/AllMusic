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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Une "pop-up" permettant de modifier un fichier.
 * 
 * @see {@link JDialog}
 * @author pmbroca
 */
public class ModifyFichierDialog extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;
	private static final Logger LOG = Logger.getLogger(ModifyFichierDialog.class);
	private final Vector<String> fichier;
	private boolean sendData;

	/**
	 * Constructeur de {@link ModifyFichierDialog}.
	 * 
	 * @param parent {@link JFrame} la fenetre parente
	 * @param header {@link String} les entetes de la popup
	 * @param modal {@code boolean} si la popup bloque l'utilisateur
	 * @param fichier {@link Vector} le fichier à modifier
	 */
	public ModifyFichierDialog(JFrame parent, String header, boolean modal, Vector<String> fichier) {
		super(parent, header, modal);
		LOG.debug("Start ModifyFichierDialog");
		this.setSize(new Dimension(1200, 150));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.fichier = fichier;
		this.setResizable(true);
		initComposant();
		this.getRootPane().registerKeyboardAction(e -> this.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		LOG.debug("End ModifyFichierDialog");
	}

	@SuppressWarnings("unchecked")
	private void initComposant() {
		LOG.debug("Start initComposant");
		JPanel content = new JPanel();

		// FileName
		JTextField fileName = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
				.withLabel("Nom du fichier : ").withPanelWidth(450)
				.withInitialValue(String.valueOf(fichier.get(FichierPanel.INDEX_FILE_FILE_NAME)))
				.withComponentWidth(430).withLabelWidth(430).build();

		// Publish Year
		JTextField publishYear = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
				.withLabel("Publication : ").withPanelWidth(80)
				.withInitialValue(String.valueOf(fichier.get(FichierPanel.INDEX_FILE_PUBLISH))).withComponentWidth(50)
				.withLabelWidth(80).build();

		// Range
		MyInputRange range = (MyInputRange) new ComponentBuilder<String>(MyInputRange.class).withParent(content)
				.withLabel("Année(s) du classement : ").withPanelWidth(300).withComponentWidth(170).withLabelWidth(200)
				.withInitialValue((String) fichier.get(FichierPanel.INDEX_FILE_RANGE)).build();

		// Cat
		JComboBox<Cat> cat = (JComboBox<Cat>) new ComponentBuilder<Cat>(JComboBox.class).withParent(content)
				.withPanelWidth(180).withLabel("Catégorie : ").withValues(Cat.values())
				.withInitialValue(Cat.getByValue((String) fichier.get(FichierPanel.INDEX_FILE_CAT)))
				.withComponentWidth(150).withLabelWidth(150).build();

		// Size
		JTextField size = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
				.withLabel("Taille : ").withPanelWidth(50)
				.withInitialValue(String.valueOf(fichier.get(FichierPanel.INDEX_FILE_SIZE))).withComponentWidth(50)
				.withLabelWidth(50).build();

		// Sorted
		JCheckBox sorted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(content)
				.withLabel("Classé : ")
				.withInitialValue(
						StringUtils.equalsIgnoreCase(fichier.get(FichierPanel.INDEX_FILE_SORTED), "oui") ? Boolean.TRUE
								: Boolean.FALSE)
				.withPanelWidth(50).withComponentWidth(50).withLabelWidth(50).build();

		JPanel control = new JPanel();
		JButton okBouton = new JButton("OK");

		okBouton.addActionListener((ActionEvent arg0) -> {
			setVisible(false);
			sendData = true;
			fichier.set(FichierPanel.INDEX_FILE_FILE_NAME, fileName.getText());
			fichier.set(FichierPanel.INDEX_FILE_PUBLISH, publishYear.getText());
			fichier.set(FichierPanel.INDEX_FILE_CAT, cat.getSelectedItem().toString());
			fichier.set(FichierPanel.INDEX_FILE_SIZE, size.getText());
			fichier.set(FichierPanel.INDEX_FILE_SORTED, sorted.isSelected() ? "Oui" : "Non");
			fichier.set(FichierPanel.INDEX_FILE_RANGE,
					range.getFirst().getText() + " - " + range.getSecond().getText());
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
	 * Affiche une {@link ModifyFichierDialog}.
	 */
	public void showModifyFichierDialog() {
		LOG.debug("Start showModifyFichierDialog");
		this.sendData = false;
		this.setVisible(true);
		LOG.debug("End showModifyFichierDialog");
	}

	public Vector<String> getFichier() {
		return fichier;
	}

	public boolean isSendData() {
		return sendData;
	}
}
