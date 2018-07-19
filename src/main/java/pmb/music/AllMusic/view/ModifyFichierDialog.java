/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.model.Cat;

/**
 * Une "pop-up" permettant de modifier un fichier.
 * 
 * @see {@link JDialog}
 */
public class ModifyFichierDialog extends JDialog {

	private static final long serialVersionUID = 1304786661370052913L;
	private static final Logger LOG = Logger.getLogger(ModifyFichierDialog.class);
	private static final int INDEX_FILE_NAME = 1;
	private static final int INDEX_PUBLISH = 2;
	private static final int INDEX_CAT = 3;
	private static final int INDEX_RANGE = 4;
	private static final int INDEX_SIZE = 6;
	private static final int INDEX_SORTED = 7;
	private final Vector<String> fichier;
	private JTextField fileName;
	private JTextField publishYear;
	private JTextField rangeDateBegin;
	private JTextField rangeDateEnd;
	private JTextField size;
	private JCheckBox sorted;
	private JComboBox<Cat> cat;
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
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.fichier = fichier;
		this.setResizable(true);
		initComposant();
		LOG.debug("End ModifyFichierDialog");
	}

	private void initComposant() {
		LOG.debug("Start initComposant");
		// FileName
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setPreferredSize(new Dimension(450, 60));
		JLabel fileNameLabel = new JLabel("Nom du fichier : ");
		fileName = new JTextField((String) fichier.get(INDEX_FILE_NAME));
		fileName.setPreferredSize(new Dimension(430, 30));
		fileNamePanel.add(fileNameLabel);
		fileNamePanel.add(fileName);

		// Publish Year
		JPanel publishYearPanel = new JPanel();
		publishYearPanel.setPreferredSize(new Dimension(80, 60));
		JLabel publishYearLabel = new JLabel("Publication : ");
		publishYear = new JTextField(String.valueOf(fichier.get(INDEX_PUBLISH)));
		publishYear.setPreferredSize(new Dimension(50, 30));
		publishYearPanel.add(publishYearLabel);
		publishYearPanel.add(publishYear);

		// Range
		JPanel rangePanel = new JPanel();
		PanelUtils.setSize(rangePanel, 200, 60);
		JLabel rangeLabel = new JLabel("Année(s) du classement : ");
		String[] split = StringUtils.split((String) fichier.get(INDEX_RANGE), " - ");
		rangeDateBegin = new JTextField(split[0]);
		rangeDateEnd = new JTextField(split[1]);
		PanelUtils.setSize(rangeDateBegin, 85, 30);
		PanelUtils.setSize(rangeDateEnd, 85, 30);
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeDateBegin);
		rangePanel.add(rangeDateEnd);

		// Cat
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(180, 60));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboBox<>();
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i]);
		}
		cat.setPreferredSize(new Dimension(150, 30));
		cat.setSelectedItem(Cat.valueOf((String) fichier.get(INDEX_CAT)));
		catPanel.add(catLabel);
		catPanel.add(cat);

		// Size
		JPanel sizePanel = new JPanel();
		sizePanel.setPreferredSize(new Dimension(50, 60));
		JLabel sizeLabel = new JLabel("Taille : ");
		size = new JTextField(String.valueOf(fichier.get(INDEX_SIZE)));
		size.setPreferredSize(new Dimension(50, 30));
		sizePanel.add(sizeLabel);
		sizePanel.add(size);

		// Sorted
		JPanel sortedPanel = new JPanel();
		sortedPanel.setPreferredSize(new Dimension(50, 60));
		JLabel sortedLabel = new JLabel("Classé: ");
		sorted = new JCheckBox();
		sorted.setSelected(
				StringUtils.equalsIgnoreCase(fichier.get(INDEX_SORTED), "oui") ? Boolean.TRUE : Boolean.FALSE);
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);

		JPanel content = new JPanel();
		content.add(fileNamePanel);
		content.add(publishYearPanel);
		content.add(rangePanel);
		content.add(catPanel);
		content.add(sizePanel);
		content.add(sortedPanel);

		JPanel control = new JPanel();
		JButton okBouton = new JButton("OK");

		okBouton.addActionListener((ActionEvent arg0) -> {
			setVisible(false);
			sendData = true;
			fichier.set(INDEX_FILE_NAME, fileName.getText());
			fichier.set(INDEX_PUBLISH, publishYear.getText());
			fichier.set(INDEX_CAT, cat.getSelectedItem().toString());
			fichier.set(INDEX_SIZE, size.getText());
			fichier.set(INDEX_SORTED, sorted.isSelected() ? "Oui" : "Non");
			fichier.set(INDEX_RANGE, rangeDateBegin.getText() + " - " + rangeDateEnd.getText());
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
	public void showDialogFileTable() {
		LOG.debug("Start showDialogFileTable");
		this.sendData = false;
		this.setVisible(true);
		LOG.debug("End showDialogFileTable");
	}

	public Vector<String> getFichier() {
		return fichier;
	}

	public boolean isSendData() {
		return sendData;
	}
}
