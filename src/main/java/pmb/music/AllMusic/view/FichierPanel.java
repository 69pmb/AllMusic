package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs
 * compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends JPanel {
	private static final long serialVersionUID = 8581952935884211032L;

	private static final Logger LOG = Logger.getLogger(FichierPanel.class);

	private JComboBox<String> auteur;
	private JTextField name;
	private JTextField publi;
	private JTextField rangeB;
	private JTextField rangeE;
	private JComboBox<String> cat;
	private JCheckBox sorted;
	private JButton search;
	private JButton reset;

	private JTable tableFiles;
	private JTable tableCompo;
	private List<Fichier> fichiers;
	private FichierModel fichieModel;
	private int selectedRow = -1;

	private static final String[] headerFiles = { "Auteur", "Nom du fichier", "Date de publication", "Categorie",
			"Dates", "Date de création", "Taille", "Classement", "Classé" };
	private static final String[] headerCompo = { "Artiste", "Titre", "Classement", "Type" };

	public FichierPanel(List<String> authors) {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BorderLayout());

		initSearchBtn(authors);
		initFichierTable();

		LOG.debug("End FichierPanel");
	}

	private void initSearchBtn(List<String> authors) {
		JPanel header = new JPanel();
		// Auteur
		JPanel auteurPanel = new JPanel();
		auteurPanel.setPreferredSize(new Dimension(200, 60));
		JLabel auteurLabel = new JLabel("Auteur : ");
		auteur = new JComboBox<>();
		auteur.addItem("");
		for (String author : authors) {
			auteur.addItem(author);
		}
		auteur.setPreferredSize(new Dimension(150, 25));
		auteurPanel.add(auteurLabel);
		auteurPanel.add(auteur);
		header.add(auteurPanel);
		// Name
		JPanel namePanel = new JPanel();
		namePanel.setPreferredSize(new Dimension(250, 60));
		JLabel nameLabel = new JLabel("Nom du fichier : ");
		name = new JTextField();
		name.setPreferredSize(new Dimension(250, 25));
		namePanel.add(nameLabel);
		namePanel.add(name);
		header.add(namePanel);
		// Publi
		JPanel publiPanel = new JPanel();
		publiPanel.setPreferredSize(new Dimension(150, 60));
		JLabel publiLabel = new JLabel("Année de publication : ");
		publi = new JTextField();
		publi.setPreferredSize(new Dimension(75, 25));
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		header.add(publiPanel);
		// Range
		JPanel rangePanel = new JPanel();
		rangePanel.setPreferredSize(new Dimension(200, 60));
		JLabel rangeLabel = new JLabel("Année(s) du classement :                ");
		rangeB = new JTextField();
		rangeE = new JTextField();
		rangeB.setPreferredSize(new Dimension(90, 25));
		rangeE.setPreferredSize(new Dimension(90, 25));
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		header.add(rangePanel);
		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(180, 60));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboBox<>();
		cat.addItem("");
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i].toString());
		}
		cat.setPreferredSize(new Dimension(120, 25));
		catPanel.add(catLabel);
		catPanel.add(cat);
		header.add(catPanel);
		// Sorted
		JPanel sortedPanel = new JPanel();
		sortedPanel.setPreferredSize(new Dimension(50, 60));
		JLabel sortedLabel = new JLabel("Classé: ");
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(50, 25));
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		header.add(sortedPanel);

		// SEARCH
		search = new JButton("Chercher");
		search.setBackground(Color.white);
		search.setPreferredSize(new Dimension(150, 60));
		search.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		header.add(search);
		// RESET
		reset = new JButton("Réinitialiser");
		reset.setBackground(Color.white);
		reset.setPreferredSize(new Dimension(150, 60));
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		header.add(reset);

		this.add(header, BorderLayout.PAGE_START);
	}

	private void initFichierTable() {
		tableFiles = new JTable();
		tableFiles.setAutoCreateRowSorter(true);
		tableFiles.setRowHeight(30);
		tableFiles.setFillsViewportHeight(true);
		tableFiles.setBackground(UIManager.getColor("Label.background"));
		tableFiles.setFont(UIManager.getFont("Label.font"));
		tableFiles.setBorder(UIManager.getBorder("Label.border"));
		fichieModel = new FichierModel(new Object[0][5], headerFiles);
		tableFiles.setModel(fichieModel);
		tableFiles.setRowSorter(new TableRowSorter<TableModel>(fichieModel));
		this.add(new JScrollPane(tableFiles), BorderLayout.CENTER);
	}

	private void colRenderer() {
		TableColumnModel modelecolonne = tableFiles.getColumnModel();
		int total = modelecolonne.getColumnCount();
		for (int i = 0; i < total; i++) {
			int taille = 0;
			int total2 = tableFiles.getRowCount();
			for (int j = 0; j < total2; j++) {
				int taille2 = tableFiles.getValueAt(j, i).toString().length() * 7; // determination
				// arbitraire
				if (taille2 > taille) {
					taille = taille2;
				}
			}
			modelecolonne.getColumn(i).setPreferredWidth(taille + 50);
		}

		DefaultTableCellRenderer renderer = new EvenOddRenderer();
		for (int i = 0; i < tableFiles.getColumnCount(); i++) {
			renderer.setHorizontalAlignment(JLabel.CENTER);
			tableFiles.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		fichiers = new ArrayList<Fichier>(
				ImportXML.importXML(Constant.FINAL_FILE_PATH).stream().map(Composition::getFiles).flatMap(List::stream)
						.collect(Collectors.toMap(Fichier::getFileName, f -> f, (p, q) -> p)).values());
		if (CollectionUtils.isNotEmpty(fichiers)) {
			CollectionUtils.filter(fichiers,
					(Object f) -> SearchUtils.evaluateFichierStrictly(publi.getText(), name.getText(),
							auteur.getSelectedItem().toString(), cat.getSelectedItem().toString(), rangeB.getText(),
							rangeE.getText(), f));
			updateTable();
		}
		LOG.debug("End searchAction");
	}

	private void updateTable() {
		LOG.debug("Start updateTable");
		fichieModel.setRowCount(0);
		fichieModel.setDataVector(FichierUtils.convertListForJTable(fichiers, null),
				new Vector<>(Arrays.asList(headerFiles)));
		colRenderer();
		fichieModel.fireTableDataChanged();
		tableFiles.getRowSorter().toggleSortOrder(6);
		tableFiles.getRowSorter().toggleSortOrder(6);
		tableFiles.repaint();
		selectedRow = -1;
		LOG.debug("Start updateTable");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		auteur.setSelectedItem("");
		name.setText("");
		publi.setText("");
		rangeB.setText("");
		rangeE.setText("");
		cat.setSelectedItem("");
		LOG.debug("End resetAction");
	}
}
