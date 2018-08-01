package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.Score;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.model.CompoSearchPanelModel;

/**
 * Gère le panel search.
 * 
 */
public class SearchPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(SearchPanel.class);

	private static final long serialVersionUID = 2593372709628283573L;
	private JLabel countLabel;
	private JLabel deleteLabel;

	private JCheckBox inFiles;

	private JButton search;

	private JTextField publi;
	private JTextField rangeB;
	private JTextField rangeE;
	private JTextField fileName;
	private JCheckBox sorted;
	private JCheckBox deleted;
	private JCheckBox topTen;

	private final JTable tableResult;

	private JComboBox<Cat> cat;
	private JComboBox<RecordType> type;
	private JComboBox<String> searchMethod;
	private JComboBox<String> titre;
	private JComboBox<String> artist;
	private JComboBox<String> author;

	private List<Composition> compoResult = new ArrayList<>();

	private static final String[] title = { "Artiste", "Titre", "Type", "Nombre de fichiers", "Score", "", "" };

	private static final int INDEX_ARTIST = 0;
	private static final int INDEX_TITRE = 1;
	private static final int INDEX_TYPE = 2;
	private static final int INDEX_SCORE = 4;
	private static final int INDEX_SELECTED = 5;
	private static final int INDEX_DELETED = 6;
	private Integer sortedColumn;
	private SortOrder sortOrder;

	private final CompoSearchPanelModel model;
	private Score score;
	private int selectedRow = -1;

	/**
	 * Génère le panel search.
	 * 
	 * @param artist2 le panel artiste
	 * @param artistList
	 * @param titleList
	 * @param authorList
	 * @param score
	 */
	public SearchPanel(final ArtistPanel artist2, List<String> artistList, List<String> titleList,
			List<String> authorList, Score score) {
		super();
		LOG.debug("Start SearchPanel");
		this.score = score;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		initButtons(artist2, header);
		initSearchFields(artistList, titleList, authorList, header);
		this.add(header);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());

		// result
		tableResult = new JTable();
		tableResult.setAutoCreateRowSorter(true);
		tableResult.setRowHeight(30);
		tableResult.setFillsViewportHeight(true);
		tableResult.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableResult.setBackground(UIManager.getColor("Label.background"));
		tableResult.getTableHeader().setResizingAllowed(true);
		tableResult.setFont(UIManager.getFont("Label.font"));
		tableResult.setBorder(UIManager.getBorder("Label.border"));
		model = new CompoSearchPanelModel(new Object[0][6], title);
		tableResult.setModel(model);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
			@Override
			public boolean isSortable(int column) {
				if (column != INDEX_SELECTED)
					return true;
				else
					return false;
			};
		};
		tableResult.setRowSorter(sorter);
		tableResult.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseClickAction(e);
			}
		});
		tableResult.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				selectedRow = PanelUtils.keyShortcutAction(e, selectedRow, sortedColumn);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableResult.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void sorterChanged(RowSorterEvent e) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					List<SortKey> sortKeys = e.getSource().getSortKeys();
					if (!sortKeys.isEmpty()) {
						sortedColumn = ((SortKey) sortKeys.get(0)).getColumn();
						sortOrder = ((SortKey) sortKeys.get(0)).getSortOrder();
					}
				}
			}
		});
		bottom.add(new JScrollPane(tableResult), BorderLayout.CENTER);

		this.add(bottom);
		LOG.debug("End SearchPanel");
	}

	/**
	 * Insert les boutons du panel search en haut.
	 * 
	 * @param artist2 le panel artist
	 * @param header le header de l'onglet
	 */
	@SuppressWarnings("unchecked")
	private void initButtons(final ArtistPanel artist2, JPanel header) {
		JPanel top = new JPanel();
		AbstractAction searchAction = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		};

		search = new JButton("Chercher");
		search.setBackground(Color.white);
		search.setPreferredSize(new Dimension(220, PanelUtils.PANEL_HEIGHT));
		search.addActionListener(searchAction);
		search.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "Enter_pressed");
		search.getActionMap().put("Enter_pressed", searchAction);
		top.add(search);

		// inFiles
		JPanel inFilesPanel = new JPanel();
		PanelUtils.setSize(inFilesPanel, 200, PanelUtils.PANEL_HEIGHT);
		JLabel inFilesLabel = PanelUtils.createJLabel("Rechercher dans les fichiers : ", 150);
		inFiles = new JCheckBox();
		PanelUtils.setSize(inFiles, 150, PanelUtils.COMPONENT_HEIGHT);
		inFiles.setSelected(true);
		inFiles.setHorizontalAlignment(SwingConstants.CENTER);
		inFilesPanel.add(inFilesLabel);
		inFilesPanel.add(inFiles);
		top.add(inFilesPanel);

		// Clear Btn
		JButton clear = new JButton("Réinitialiser recherche");
		clear.setBackground(Color.white);
		clear.setPreferredSize(new Dimension(200, PanelUtils.PANEL_HEIGHT));
		AbstractAction cleanAction = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				cleanAction();
			}
		};
		clear.addActionListener(cleanAction);
		top.add(clear);

		// Delete Btn
		JButton delete = new JButton("Supprimer les compositions sélectionnées");
		delete.setBackground(Color.white);
		delete.setPreferredSize(new Dimension(300, PanelUtils.PANEL_HEIGHT));
		delete.addActionListener((ActionEvent e) -> {
			PanelUtils.deleteCompositionAction(artist2, compoResult, model.getSelected(), deleteLabel);
			updateTable();
		});
		top.add(delete);

		// Modif Btn
		JButton modif = new JButton("Modifier la composition sélectionnée");
		modif.setBackground(Color.white);
		modif.setPreferredSize(new Dimension(300, PanelUtils.PANEL_HEIGHT));
		modif.addActionListener((ActionEvent e) -> modifAction(artist2));
		top.add(modif);

		// CSV
		JButton csv = new JButton("Télécharger le résultat de la recherche en CSV");
		csv.setBackground(Color.white);
		csv.setPreferredSize(new Dimension(300, PanelUtils.PANEL_HEIGHT));
		csv.addActionListener((ActionEvent e) -> {
			List<String> c = Arrays
					.asList(publi.getText(), rangeB.getText(), rangeE.getText(), fileName.getText(),
							searchMethod.getSelectedItem() == null ? "" : searchMethod.getSelectedItem().toString(),
							cat.getSelectedItem() == null ? "" : cat.getSelectedItem().toString(),
							type.getSelectedItem() == null ? "" : type.getSelectedItem().toString(),
							titre.getSelectedItem() == null ? "" : titre.getSelectedItem().toString(),
							artist.getSelectedItem() == null ? "" : artist.getSelectedItem().toString(),
							author.getSelectedItem() == null ? "" : author.getSelectedItem().toString(),
							"Sorted:" + Boolean.toString(sorted.isSelected()),
							"Deleted:" + Boolean.toString(deleted.isSelected()),
							"Top Ten:" + Boolean.toString(topTen.isSelected()))
					.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
			String criteres = StringUtils.join(c, " ");
			String[] csvHeader = { "Artiste", "Titre", "Type", "Nombre de fichiers", "Score", "Critères: " + criteres };
			String name = CsvFile.exportCsv("search", MiscUtils.convertVectorToList(model.getDataVector()),
					tableResult.getRowSorter().getSortKeys().get(0), csvHeader);
			try {
				Runtime.getRuntime().exec(Constant.getExcelPath() + name);
			} catch (IOException e1) {
				LOG.error("Impossible d'ouvrir excel: " + Constant.getExcelPath(), e1);
			}
		});
		top.add(csv);
		header.add(top);
	}

	/**
	 * Initialise les champs de recherche.
	 * 
	 * @param artistList la liste des artistes
	 * @param titleList la liste des titres
	 * @param authorList la liste des auteurs
	 * @param header le header de l'onglet
	 */
	private void initSearchFields(List<String> artistList, List<String> titleList, List<String> authorList,
			JPanel header) {
		LOG.debug("Start initSearchFields");
		JPanel searchFields = new JPanel();
		searchFields.setLayout(new GridLayout(2, 5));

		// Artiste
		JPanel artistPanel = new JPanel();
		JLabel artistLabel = PanelUtils.createJLabel("Artiste : ", 200);
		artist = new JComboBox<>();
		AutoCompleteSupport.install(artist, GlazedLists.eventListOf(artistList.toArray()));
		artist.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		artistPanel.add(artistLabel);
		artistPanel.add(artist);
		searchFields.add(artistPanel);

		// Titre
		JPanel titrePanel = new JPanel();
		JLabel titreLabel = PanelUtils.createJLabel("Titre : ", 180);
		titre = new JComboBox<>();
		AutoCompleteSupport.install(titre, GlazedLists.eventListOf(titleList.toArray()));
		titre.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		titrePanel.add(titreLabel);
		titrePanel.add(titre);
		searchFields.add(titrePanel);

		// Nom du fichier
		JPanel fileNamePanel = new JPanel();
		JLabel fileNameLabel = PanelUtils.createJLabel("Nom du fichier : ", 300);
		fileName = new JTextField();
		PanelUtils.setSize(fileName, 220, PanelUtils.COMPONENT_HEIGHT);
		fileNamePanel.add(fileNameLabel);
		fileNamePanel.add(fileName);
		searchFields.add(fileNamePanel);

		// Auteur
		JPanel authorPanel = new JPanel();
		JPanel panel = new JPanel();
		JLabel authorLabel = PanelUtils.createJLabel("Auteur : ", 150);
		author = new JComboBox<>();
		AutoCompleteSupport.install(author, GlazedLists.eventListOf(authorList.toArray()));
		PanelUtils.setSize(author, 150, PanelUtils.COMPONENT_HEIGHT);
		PanelUtils.setSize(panel, 150, PanelUtils.PANEL_HEIGHT);
		panel.add(authorLabel);
		panel.add(author);
		authorPanel.add(panel);
		searchFields.add(authorPanel);

		// SearchMethod
		JPanel searchMethodPanel = new JPanel();
		JLabel searchMethodLabel = PanelUtils.createJLabel("Méthode de recherche : ", 150);
		searchMethod = new JComboBox<>();
		SearchMethod[] valuesSearch = SearchMethod.values();
		for (int i = 0; i < valuesSearch.length; i++) {
			searchMethod.addItem(valuesSearch[i].getValue());
		}
		searchMethod.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		searchMethodPanel.add(searchMethodLabel);
		searchMethodPanel.add(searchMethod);
		searchFields.add(searchMethodPanel);

		// Type
		JPanel typePanel = new JPanel();
		JLabel typeLabel = PanelUtils.createJLabel("Type : ", 180);
		type = new JComboBox<>();
		type.addItem(null);
		RecordType[] valuesType = RecordType.values();
		for (int i = 0; i < valuesType.length; i++) {
			type.addItem(valuesType[i]);
		}
		type.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		typePanel.add(typeLabel);
		typePanel.add(type);
		searchFields.add(typePanel);

		// Range
		JPanel rangePanel = new JPanel();
		JLabel rangeLabel = PanelUtils.createJLabel("Année(s) du classement : ", 200);
		rangeB = new JTextField();
		rangeE = new JTextField();
		PanelUtils.setSize(rangeB, 100, PanelUtils.COMPONENT_HEIGHT);
		PanelUtils.setSize(rangeE, 100, PanelUtils.COMPONENT_HEIGHT);
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		searchFields.add(rangePanel);

		// Categorie
		JPanel catPanel = new JPanel();
		JLabel catLabel = PanelUtils.createJLabel("Catégorie : ", 150);
		cat = new JComboBox<>();
		cat.addItem(null);
		Cat[] values = Cat.values();
		for (int i = 0; i < values.length; i++) {
			cat.addItem(values[i]);
		}
		cat.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		searchFields.add(catPanel);

		// Publi
		JPanel publiPanel = new JPanel();
		JLabel publiLabel = PanelUtils.createJLabel("Année de publication : ", 200);
		publi = new JTextField();
		publi.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		publiPanel.add(publiLabel);
		publiPanel.add(publi);
		searchFields.add(publiPanel);

		// Sorted
		JPanel sortedPanel = new JPanel();
		JLabel sortedLabel = PanelUtils.createJLabel("Trié : ", 150);
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		sorted.setHorizontalAlignment(SwingConstants.CENTER);
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		searchFields.add(sortedPanel);

		// Deleted
		JPanel deletedPanel = new JPanel();
		JLabel deletedLabel = PanelUtils.createJLabel("Supprimé : ", 150);
		deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);
		searchFields.add(deletedPanel);

		// TopTen
		JPanel topPanel = new JPanel();
		JLabel topLabel = PanelUtils.createJLabel("Top 10 : ", 150);
		topTen = new JCheckBox();
		topTen.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		topTen.setHorizontalAlignment(SwingConstants.CENTER);
		topPanel.add(topLabel);
		topPanel.add(topTen);
		searchFields.add(topPanel);

		// Nombre de résultat
		JPanel countPanel = new JPanel();
		countLabel = PanelUtils.createJLabel("", 200);
		countLabel.setForeground(new Color(8, 187, 81));
		Font labelFont = countLabel.getFont();
		countLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 25));
		countLabel.setVerticalAlignment(SwingConstants.CENTER);
		countPanel.add(countLabel);
		searchFields.add(countPanel);

		// Nombre de suppression
		JPanel deletePanel = new JPanel();
		deleteLabel = PanelUtils.createJLabel("", 400);
		deleteLabel.setForeground(new Color(8, 187, 81));
		Font labelFont2 = deleteLabel.getFont();
		deleteLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 20));
		deletePanel.add(deleteLabel);
		searchFields.add(deletePanel);

		header.add(searchFields);
		LOG.debug("End initSearchFields");
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		deleteLabel.setText("");
		List<Composition> allCompo = ImportXML.importXML(Constant.getFinalFilePath());
		if (CollectionUtils.isNotEmpty(allCompo)) {
			Map<String, String> criteria = new HashMap<>();
			if (artist.getSelectedItem() != null) {
				criteria.put(SearchUtils.CRITERIA_ARTIST, artist.getSelectedItem().toString());
			}
			if (titre.getSelectedItem() != null) {
				criteria.put(SearchUtils.CRITERIA_TITRE, titre.getSelectedItem().toString());
			}
			if (type.getSelectedItem() != null) {
				criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.getSelectedItem().toString());
			}
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, publi.getText());
			criteria.put(SearchUtils.CRITERIA_FILENAME, fileName.getText());
			if (author.getSelectedItem() != null) {
				criteria.put(SearchUtils.CRITERIA_AUTHOR, author.getSelectedItem().toString());
			}
			if (cat.getSelectedItem() != null) {
				criteria.put(SearchUtils.CRITERIA_CAT, cat.getSelectedItem().toString());
			}
			criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, rangeB.getText());
			criteria.put(SearchUtils.CRITERIA_DATE_END, rangeE.getText());
			if (sorted.isSelected()) {
				criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
			}
			if (topTen.isSelected()) {
				criteria.put(SearchUtils.CRITERIA_TOP, Boolean.TRUE.toString());
			}

			compoResult = new ArrayList<>();
			compoResult.addAll(SearchUtils.search(allCompo, criteria, inFiles.isSelected(),
					SearchMethod.getByValue((String) searchMethod.getSelectedItem()), deleted.isSelected()));
			updateTable();
		}
		LOG.debug("End searchAction");
	}

	private void updateTable() {
		LOG.debug("Start updateTable");
		model.setRowCount(0);
		model.setDataVector(CompositionUtils.convertCompositionListToVector(compoResult, false, true, score),
				new Vector<>(Arrays.asList(title)));
		PanelUtils.colRenderer(tableResult, false, INDEX_DELETED);
		countLabel.setText(compoResult.size() + " résultats");
		model.fireTableDataChanged();
		if (sortedColumn == null) {
			sortedColumn = INDEX_SCORE;
			sortOrder = SortOrder.DESCENDING;
		}
		tableResult.getRowSorter()
				.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedColumn, sortOrder)));
		selectedRow = -1;
		tableResult.removeColumn(tableResult.getColumnModel().getColumn(INDEX_DELETED));
		tableResult.repaint();
		LOG.debug("Start updateTable");
	}

	private void cleanAction() {
		LOG.debug("Start cleanAction");
		artist.setSelectedItem(null);
		titre.setSelectedItem(null);
		type.setSelectedItem(null);
		searchMethod.setSelectedItem(SearchMethod.CONTAINS.getValue());
		publi.setText("");
		fileName.setText("");
		author.setSelectedItem(null);
		cat.setSelectedItem(null);
		sorted.setSelected(false);
		deleted.setSelected(false);
		topTen.setSelected(false);
		rangeB.setText("");
		rangeE.setText("");
		deleteLabel.setText("");
		countLabel.setText("");
		LOG.debug("End cleanAction");
	}

	@SuppressWarnings("unchecked")
	private void modifAction(final ArtistPanel artist2) {
		LOG.debug("Start modif");
		artist2.interruptUpdateArtist();
		String label = "Élément modifié";
		Object selected;
		Composition toModif;
		Vector<String> v;
		List<Composition> importXML;
		if (model.getSelected().size() > 1) {
			String msg = "Trop d'éléments sélectionnés";
			deleteLabel.setText(msg);
			LOG.debug(msg);
			return;
		} else {
			// On récupère la ligne selectionnée
			selected = model.getSelected().get(0);
			v = (Vector<String>) selected;
			importXML = ImportXML.importXML(Constant.getFinalFilePath());
			try {
				// On récupère la composition à modifier
				toModif = CompositionUtils.findByArtistTitreAndType(importXML, v.get(INDEX_ARTIST), v.get(INDEX_TITRE),
						v.get(INDEX_TYPE), true);
			} catch (MyException e1) {
				String log = "Erreur dans modifAction, impossible de trouver la compo à modifier";
				LOG.error(log, e1);
				deleteLabel.setText(log + e1);
				return;
			}
		}
		// Lancement de la popup de modification
		ModifyCompositionDialog md = new ModifyCompositionDialog(null, "Modifier une composition", true,
				new Dimension(800, 150), v);
		md.showDialogFileTable();
		if (md.isSendData()) {
			// On recupère la compo si elle a bien été modifiée
			LOG.debug("Compo modifiée");
			v = md.getCompo();
		} else {
			LOG.debug("Aucune modification");
			return;
		}
		int indexOfXml = importXML.indexOf(toModif);
		int indexOfResult = SearchUtils.indexOf(compoResult, toModif);

		// Modification du fichier final
		boolean isDeleted = false;
		toModif.setArtist(v.get(INDEX_ARTIST));
		toModif.setTitre(v.get(INDEX_TITRE));
		toModif.setRecordType(RecordType.valueOf(v.get(INDEX_TYPE)));
		importXML.remove(indexOfXml);
		compoResult.remove(indexOfResult);
		Composition compoExist = CompositionUtils.compoExist(importXML, toModif);
		if (compoExist == null) {
			LOG.debug("Pas de regroupement");
			importXML.add(toModif);
			compoResult.add(toModif);
		} else {
			LOG.debug("La compo existe déjà, on regroupe");
			compoExist.getFiles().addAll(toModif.getFiles());
			isDeleted = compoExist.isDeleted() || toModif.isDeleted();
			// Liste des compositions affichées
			Composition compoExistResult = CompositionUtils.compoExist(compoResult, toModif);
			if (compoExistResult != null) {
				// La compo apparait bien dans les resultats de recherche
				compoExistResult.getFiles().addAll(toModif.getFiles());
				compoExistResult.setDeleted(isDeleted);
			}
		}
		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
			artist2.updateArtistPanel();
		} catch (IOException e1) {
			String log = "Erreur lors de l'export du fichier final !!";
			LOG.error(log, e1);
			label = log;
		}

		// On modifier les fichiers xml en conséquence
		try {
			toModif.setDeleted(isDeleted);
			CompositionUtils.modifyCompositionsInFiles(toModif, v.get(INDEX_ARTIST), v.get(INDEX_TITRE),
					v.get(INDEX_TYPE));
		} catch (MyException e1) {
			String log = "Erreur lors de la modification d'une composition";
			LOG.error(log, e1);
			deleteLabel.setText(log + e1);
			return;
		}
		deleteLabel.setText(label);
		updateTable();
		LOG.debug("End modif");
	}

	private void mouseClickAction(MouseEvent e) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow(e);
		if (!selectedRow.isPresent()) {
			return;
		}
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start result mouse");
			// Ouvre une popup pour afficher les fichiers de la
			// composition sélectionnée
			try {
				DialogFileTable pop = new DialogFileTable(null, "Fichier", true,
						Arrays.asList(CompositionUtils.findByArtistTitreAndType(compoResult,
								selectedRow.get().get(INDEX_ARTIST), selectedRow.get().get(INDEX_TITRE),
								selectedRow.get().get(INDEX_TYPE), true)),
						new Dimension(1500, 400), DialogFileTable.INDEX_AUTEUR);
				pop.showDialogFileTable();
			} catch (MyException e1) {
				LOG.error("Ereur lors de l'affichage des fichier d'une compo", e1);
			}
			LOG.debug("End result mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Copie dans le clipboard l'artist et l'oeuvre
			StringSelection selection = new StringSelection(
					selectedRow.get().get(INDEX_ARTIST) + " " + selectedRow.get().get(INDEX_TITRE));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
		}
	}

	public JButton getSearch() {
		return search;
	}

	public void setSearch(JButton search) {
		this.search = search;
	}

	public JTable getTableResult() {
		return tableResult;
	}
}
