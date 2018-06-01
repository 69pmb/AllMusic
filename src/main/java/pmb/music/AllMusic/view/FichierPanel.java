package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs
 * compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends JPanel {
	private static final long serialVersionUID = 8581952935884211032L;

	private static final Logger LOG = Logger.getLogger(FichierPanel.class);

	private static final int MIN_HEIGHT_TABLE = 42;
	private static final int MAX_HEIGHT_TABLE = 84;

	private JComboBox<String> auteur;
	private JTextField name;
	private JTextField publi;
	private JTextField rangeB;
	private JTextField rangeE;
	private JComboBox<String> cat;
	private JCheckBox sorted;
	private JButton search;
	private JButton reset;
	private JLabel resultLabel;

	private JPanel fichierPanel;
	private JTable tableFiles;
	private FichierModel fichieModel;
	private List<Fichier> fichiers;
	private JButton hideFileList;
	private boolean showFichierTable = true;

	private JPanel compoPanel;
	private JTable tableCompo;
	private CompoModel compoModel;
	private List<Composition> compositionList;
	private JButton hideCompoList;
	private boolean showCompoTable = true;

	private Dimension parentSize;

	private static final String[] headerFiles = { "Auteur", "Nom du fichier", "Date de publication", "Categorie",
			"Dates", "Date de création", "Taille", "Classement", "Classé" };
	private static final String[] headerCompo = { "Artiste", "Titre", "Type", "Classement", "" };

	public FichierPanel() {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		LOG.debug("End FichierPanel");
	}

	public void initPanel(ArtistPanel artistPanel, List<String> authors) {

		parentSize = this.getParent().getPreferredSize();
		initSearchBtn(artistPanel, authors);
		initFichierTable();
		initCompoTable(artistPanel);

	}

	private void initSearchBtn(ArtistPanel artistPanel, List<String> authors) {
		JPanel header = new JPanel();
		// Auteur
		JPanel auteurPanel = new JPanel();
		auteurPanel.setPreferredSize(new Dimension(150, 60));
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
		search.setPreferredSize(new Dimension(120, 60));
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
		reset.setPreferredSize(new Dimension(120, 60));
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		header.add(reset);
		// hideFileList
		hideFileList = new JButton("Cacher la liste des fichiers");
		hideFileList.setBackground(Color.white);
		hideFileList.setPreferredSize(new Dimension(180, 60));
		hideFileList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showFichierTable = !showFichierTable;
				fichierPanel.setVisible(showFichierTable);
				if (showFichierTable) {
					hideFileList.setText("Cacher la liste des fichiers");
					setTableSize(compoPanel, MIN_HEIGHT_TABLE);
				} else {
					hideFileList.setText("Afficher la liste des fichiers");
					setTableSize(compoPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		header.add(hideFileList);
		// hideCompoList
		hideCompoList = new JButton("Cacher la liste des compositions");
		hideCompoList.setBackground(Color.white);
		hideCompoList.setPreferredSize(new Dimension(200, 60));
		hideCompoList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showCompoTable = !showCompoTable;
				compoPanel.setVisible(showCompoTable);
				if (showCompoTable) {
					hideCompoList.setText("Cacher la liste des compositions");
					setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
				} else {
					hideCompoList.setText("Afficher la liste des compositions");
					setTableSize(fichierPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		header.add(hideCompoList);
		// Delete Btn
		JButton delete = new JButton("Supprimer les compositions sélectionnées");
		delete.setBackground(Color.white);
		delete.setPreferredSize(new Dimension(250, 60));
		delete.addActionListener((ActionEvent e) -> {
			PanelUtils.deleteCompositionAction(artistPanel, compositionList, compoModel.getSelected(), resultLabel);
			updateCompoTable(compositionList);
		});
		header.add(delete);
		// Label pour afficher les resultats
		JPanel resultPanel = new JPanel();
		resultPanel.setPreferredSize(new Dimension(400, 40));
		resultLabel = new JLabel("");
		resultLabel.setForeground(new Color(8, 187, 81));
		Font labelFont2 = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 20));
		resultPanel.add(resultLabel);
		header.add(resultPanel);

		PanelUtils.setSize(header, (int) parentSize.getWidth(), Math.floorDiv(10 * (int) parentSize.getHeight(), 100));
		this.add(header);
	}

	private void initFichierTable() {
		fichierPanel = new JPanel(new BorderLayout());

		tableFiles = new JTable();
		tableFiles.setAutoCreateRowSorter(true);
		tableFiles.setRowHeight(30);
		tableFiles.setFillsViewportHeight(true);
		tableFiles.setBackground(UIManager.getColor("Label.background"));
		tableFiles.setFont(UIManager.getFont("Label.font"));
		tableFiles.setBorder(UIManager.getBorder("Label.border"));
		fichieModel = new FichierModel(new Object[0][9], headerFiles);
		tableFiles.setModel(fichieModel);
		tableFiles.setRowSorter(new TableRowSorter<TableModel>(fichieModel));
		tableFiles.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForFileTable(e);
			}
		});

		fichierPanel.add(new JScrollPane(tableFiles), BorderLayout.CENTER);
		this.add(fichierPanel);
		setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
	}

	private void initCompoTable(ArtistPanel artistPanel) {
		compoPanel = new JPanel(new BorderLayout());

		tableCompo = new JTable();
		tableCompo.setAutoCreateRowSorter(true);
		tableCompo.setRowHeight(30);
		tableCompo.setFillsViewportHeight(true);
		tableCompo.setBackground(UIManager.getColor("Label.background"));
		tableCompo.setFont(UIManager.getFont("Label.font"));
		tableCompo.setBorder(UIManager.getBorder("Label.border"));
		compoModel = new CompoModel(new Object[0][6], headerCompo);
		tableCompo.setModel(compoModel);
		tableCompo.setRowSorter(new TableRowSorter<TableModel>(compoModel));
		tableCompo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForCompoTable(e, artistPanel);
			}
		});

		compoPanel.add(new JScrollPane(tableCompo), BorderLayout.CENTER);
		this.add(compoPanel);
		setTableSize(compoPanel, MIN_HEIGHT_TABLE);
	}

	@SuppressWarnings("unchecked")
	private void mouseActionForFileTable(MouseEvent e) {
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse");
			// Double click avec le bouton gauche
			// Ouvre une popup pour afficher les compositions du fichier sélectionné
			JTable target = (JTable) e.getSource();
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
			compositionList = ImportXML.importXML(Constant.XML_PATH + v.get(1) + Constant.XML_EXTENSION);
			updateCompoTable(compositionList);
			LOG.debug("End left mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Ouvre le fichier txt
			JTable target = (JTable) e.getSource();
			int rowAtPoint = target
					.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
			if (rowAtPoint > -1) {
				target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
			}
			Vector<String> v = (Vector<String>) ((FichierModel) target.getModel()).getDataVector()
					.get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
			Optional<String> filePath = FichierUtils.buildTxtFilePath(v.get(1), v.get(0));
			try {
				FichierUtils.openFileInNotepad(filePath);
			} catch (MyException e1) {
				LOG.error("Erreur lors de l'ouverture du fichier: " + filePath, e1);
			}
			LOG.debug("End right mouse");
		}
	}

	@SuppressWarnings("unchecked")
	private void mouseActionForCompoTable(MouseEvent e, ArtistPanel artistPanel) {
		JTable target = (JTable) e.getSource();
		int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
		if (rowAtPoint > -1) {
			target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
		}
		Vector<String> selectedRow = (Vector<String>) ((CompoModel) target.getModel()).getDataVector()
				.get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse");
			modifyCompositionAction(artistPanel, selectedRow);
			LOG.debug("End left mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Copie dans le clipboard l'artist et l'oeuvre
			StringSelection selection = new StringSelection(selectedRow.get(0) + " " + selectedRow.get(1));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
		}
	}

	@SuppressWarnings("unchecked")
	private void modifyCompositionAction(final ArtistPanel artistPanel, Object selected) {
		LOG.debug("Start modif");
		resultLabel.setText("");
		artistPanel.interruptUpdateArtist();
		String label = "Élément modifié";
		Composition compoToModifInFinal;
		Composition compoToModifInTable;
		// On récupère la ligne selectionnée
		Vector<String> v = (Vector<String>) selected;
		List<Composition> importXML;
		importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
		Fichier currentFile = null;
		try {
			// On récupère la composition à modifier
			compoToModifInTable = CompositionUtils.findByArtistTitreAndType(compositionList, v.get(0), v.get(1),
					v.get(2), true);
			currentFile = compoToModifInTable.getFiles().get(0);
			Optional<Composition> findByFileAndRank = CompositionUtils.findByFile(importXML, currentFile);
			if(!findByFileAndRank.isPresent()) {
				resultLabel.setText("Impossible de trouver la composition dans Final.xml");
				return;
			}
			compoToModifInFinal = findByFileAndRank.get();
		} catch (MyException e1) {
			String log = "Erreur dans modifAction, impossible de trouver la compo à modifier";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}
		int indexOfXml = importXML.indexOf(compoToModifInFinal);
		int indexOfResult = compositionList.indexOf(compoToModifInTable);
		// Lancement de la popup de modification
		ModifyDialog md = new ModifyDialog(null, "Modifier une composition", true, new Dimension(800, 150), v);
		md.showDialogFileTable();
		if (md.isSendData()) {
			// On recupère la compo si elle a bien été modifiée
			LOG.debug("Compo modifiée");
			v = md.getCompo();
		} else {
			LOG.debug("Aucune modification");
			return;
		}
		// On modifier le fichier xml en conséquence
		try {
			CompositionUtils.modifyCompositionsInFiles(compoToModifInTable, v.get(0), v.get(1), v.get(2));
		} catch (MyException e1) {
			String log = "Erreur lors de la modification d'une composition";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}
		// On modifie la composition
		compoToModifInTable.setArtist(v.get(0));
		compoToModifInTable.setTitre(v.get(1));
		compoToModifInTable.setRecordType(RecordType.valueOf(v.get(2)));

		// On remplace par la nouvelle composition dans le tableau
		compositionList.remove(indexOfResult);
		compositionList.add(compoToModifInTable);

		// Gestion du fichier final.xml
		if (importXML.get(indexOfXml).getFiles().size() > 1) {
			LOG.debug("Plusieurs fichiers, suppression du fichier en cours");
			importXML.get(indexOfXml).getFiles().remove(currentFile);
		} else {
			LOG.debug("Un seul fichier, suppression de la composition");
			importXML.remove(indexOfXml);
		}
		Composition compoExist = CompositionUtils.compoExist(importXML, compoToModifInTable);
		if (compoExist == null) {
			LOG.debug("Pas de regroupement");
			importXML.add(compoToModifInTable);
		} else {
			LOG.debug("La compo existe déjà, on regroupe");
			compoExist.getFiles().addAll(compoToModifInTable.getFiles());
		}
		try {
			ExportXML.exportXML(importXML, Constant.FINAL_FILE);
			artistPanel.updateArtistPanel();
		} catch (IOException e1) {
			String log = "Erreur lors de l'export du fichier final !!";
			LOG.error(log, e1);
			label = log;
		}
		resultLabel.setText(label);
		updateCompoTable(compositionList);
		LOG.debug("End modif");
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		resultLabel.setText("");
		fichiers = new ArrayList<Fichier>(
				ImportXML.importXML(Constant.FINAL_FILE_PATH).stream().map(Composition::getFiles).flatMap(List::stream)
						.collect(Collectors.toMap(Fichier::getFileName, f -> f, (p, q) -> p)).values());
		if (CollectionUtils.isNotEmpty(fichiers)) {
			CollectionUtils.filter(fichiers,
					(Object f) -> SearchUtils.evaluateFichierStrictly(publi.getText(), name.getText(),
							auteur.getSelectedItem().toString(), cat.getSelectedItem().toString(), rangeB.getText(),
							rangeE.getText(), f));
			if (sorted.isSelected()) {
				fichiers = fichiers.stream().filter(f -> Boolean.TRUE.equals(f.getSorted()))
						.collect(Collectors.toList());
			}
			updateFileTable();
		}
		resultLabel.setText(fichiers.size() + " fichiers trouvé(s) ");
		LOG.debug("End searchAction");
	}

	private void updateFileTable() {
		LOG.debug("Start updateFileTable");
		fichieModel.setRowCount(0);
		fichieModel.setDataVector(FichierUtils.convertListForJTable(fichiers),
				new Vector<>(Arrays.asList(headerFiles)));
		PanelUtils.colRenderer(tableFiles, true);
		fichieModel.fireTableDataChanged();
		tableFiles.getRowSorter().toggleSortOrder(1);
		tableFiles.repaint();
		updateCompoTable(new ArrayList<>());
		LOG.debug("Start updateFileTable");
	}

	/**
	 * Met à jour le tableau des compositions.
	 * @param compo la liste des compositions à afficher
	 */
	private void updateCompoTable(List<Composition> compo) {
		LOG.debug("Start updateCompoTable");
		compoModel.setRowCount(0);
		compoModel.setDataVector(CompositionUtils.convertCompositionListToVector(compo, true, true),
				new Vector<>(Arrays.asList(headerCompo)));
		PanelUtils.colRenderer(tableCompo, false);
		compoModel.fireTableDataChanged();
		tableCompo.getRowSorter().toggleSortOrder(3);
		tableCompo.repaint();
		LOG.debug("Start updateCompoTable");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		auteur.setSelectedItem("");
		name.setText("");
		publi.setText("");
		rangeB.setText("");
		rangeE.setText("");
		cat.setSelectedItem("");
		sorted.setSelected(false);
		LOG.debug("End resetAction");
	}

	private void setTableSize(JPanel panel, int height) {
		PanelUtils.setSize(panel, (int) parentSize.getWidth(), Math.floorDiv(height * (int) parentSize.getHeight(), 100));
	}
}
