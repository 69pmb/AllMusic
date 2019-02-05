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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;
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
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(450, 60));
		JLabel fileNameLabel = new JLabel("Nom du fichier : ");
		JTextField fileName = new JTextField((String) fichier.get(FichierPanel.INDEX_FILE_FILE_NAME));
		fileName.setPreferredSize(new Dimension(430, 30));
		fileNamePanel.add(fileNameLabel);
		fileNamePanel.add(fileName);
		content.add(fileNamePanel);

		// Publish Year
		JPanel publishYearPanel = new JPanel();
		publishYearPanel.setPreferredSize(new Dimension(80, 60));
		JLabel publishYearLabel = new JLabel("Publication : ");
		JTextField publishYear = new JTextField(String.valueOf(fichier.get(FichierPanel.INDEX_FILE_PUBLISH)));
		publishYear.setPreferredSize(new Dimension(50, 30));
		publishYearPanel.add(publishYearLabel);
		publishYearPanel.add(publishYear);
		content.add(publishYearPanel);

		// Range
		JPanel rangePanel = new JPanel();
		PanelUtils.setSize(rangePanel, 200, 60);
		JLabel rangeLabel = new JLabel("Année(s) du classement : ");
		String[] split = StringUtils.split((String) fichier.get(FichierPanel.INDEX_FILE_RANGE), " - ");
		JTextField rangeDateBegin = new JTextField(split[0]);
		JTextField rangeDateEnd = new JTextField(split[1]);
		PanelUtils.setSize(rangeDateBegin, 85, 30);
		PanelUtils.setSize(rangeDateEnd, 85, 30);
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeDateBegin);
		rangePanel.add(rangeDateEnd);
		content.add(rangePanel);

		// Cat
		JComboBox<Cat> cat = (JComboBox<Cat>) new ComponentBuilder<Cat>(JComboBox.class).withParent(content)
				.withPanelWidth(180).withLabel("Catégorie : ").withValues(Cat.values())
				.withInitialValue(Cat.getByValue((String) fichier.get(FichierPanel.INDEX_FILE_CAT)))
				.withComponentWidth(150).withLabelWidth(150).build();

		// Size
		JPanel sizePanel = new JPanel();
		sizePanel.setPreferredSize(new Dimension(50, 60));
		JLabel sizeLabel = new JLabel("Taille : ");
		JTextField size = new JTextField(String.valueOf(fichier.get(FichierPanel.INDEX_FILE_SIZE)));
		size.setPreferredSize(new Dimension(50, 30));
		sizePanel.add(sizeLabel);
		sizePanel.add(size);
		content.add(sizePanel);

		// Sorted
		JPanel sortedPanel = new JPanel();
		sortedPanel.setPreferredSize(new Dimension(50, 60));
		JLabel sortedLabel = new JLabel("Classé: ");
		JCheckBox sorted = new JCheckBox();
		sorted.setSelected(
				StringUtils.equalsIgnoreCase(fichier.get(FichierPanel.INDEX_FILE_SORTED), "oui") ? Boolean.TRUE
						: Boolean.FALSE);
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		content.add(sortedPanel);

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
			fichier.set(FichierPanel.INDEX_FILE_RANGE, rangeDateBegin.getText() + " - " + rangeDateEnd.getText());
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
