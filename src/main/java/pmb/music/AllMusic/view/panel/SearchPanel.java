package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.util.LinkedList;
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

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.dialog.ModifyCompositionDialog;
import pmb.music.AllMusic.view.model.CompoSearchPanelModel;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

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

	private JComboBox<String> searchRange;
	private MyInputText publi;
	private MyInputText rangeB;
	private MyInputText rangeE;
	private MyInputText fileName;
	private JCheckBox sorted;
	private JCheckBox deleted;
	private JCheckBox topTen;

	private JTable tableResult;

	private JComboCheckBox cat;
	private JComboCheckBox type;
	private JComboBox<String> searchMethod;
	private MyInputText titre;
	private MyInputText artist;
	private MyInputText author;

	private List<Composition> compoResult = new ArrayList<>();

	private static final String[] title = { "#", "Artiste", "Titre", "Type", "Nombre de fichiers", "Score", "", "" };

	public static final int INDEX_LINE_NUMBER = 0;
	public static final int INDEX_ARTIST = 1;
	public static final int INDEX_TITRE = 2;
	public static final int INDEX_TYPE = 3;
	public static final int INDEX_FILE_SIZE = 4;
	public static final int INDEX_SCORE = 5;
	public static final int INDEX_SELECTED = 6;
	public static final int INDEX_DELETED = 7;
	private Integer sortedColumn;
	private SortOrder sortOrder;
	private SortOrder sortDeletedOrder = SortOrder.ASCENDING;

	private CompoSearchPanelModel model;
	private int selectedRow = -1;

	/**
	 * Génère le panel search.
	 * 
	 * @param artist2 le panel artiste
	 */
	public SearchPanel(final ArtistPanel artist2) {
		super();
		LOG.debug("Start SearchPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		initButtons(artist2, header);
		initSearchFields(header);
		this.add(header);

		initTable();
		LOG.debug("End SearchPanel");
	}

	/**
	 * Insert les boutons du panel search en haut.
	 * 
	 * @param artist2 le panel artist
	 * @param header le header de l'onglet
	 */
	private void initButtons(final ArtistPanel artist2, JPanel header) {
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		AbstractAction searchAction = new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		};

		search = PanelUtils.createJButton("Rechercher", 220, Constant.ICON_SEARCH);
		search.addActionListener(searchAction);
		search.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "Enter_pressed");
		search.getActionMap().put("Enter_pressed", searchAction);
		top.add(search);

		// Clear Btn
		JButton clear = PanelUtils.createJButton("Réinitialiser recherche", 200, Constant.ICON_ERASE);
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
		JButton delete = PanelUtils.createJButton("Supprimer les compositions sélectionnées", 300,
				Constant.ICON_DELETE);
		delete.addActionListener((ActionEvent e) -> {
			PanelUtils.deleteCompositionAction(artist2, compoResult, model.getSelected(), deleteLabel);
			updateTable();
		});
		top.add(delete);

		// Modif Btn
		JButton modif = PanelUtils.createJButton("Modifier la composition sélectionnée", 300, Constant.ICON_EDIT);
		modif.addActionListener((ActionEvent e) -> modifAction(artist2));
		top.add(modif);

		// CSV
		JButton csv = PanelUtils.createJButton("Télécharger le résultat de la recherche en CSV", 300,
				Constant.ICON_DOWNLOAD);
		csv.addActionListener((ActionEvent e) -> {
			List<String> c = Arrays
					.asList(publi.getText(), rangeB.getText(), rangeE.getText(), fileName.getText(),
							searchMethod.getSelectedItem() == null ? "" : searchMethod.getSelectedItem().toString(),
							cat.getSelectedItems(), type.getSelectedItems(), titre.getText(), artist.getText(),
							author.getText(), "Sorted:" + Boolean.toString(sorted.isSelected()),
							"Deleted:" + Boolean.toString(deleted.isSelected()),
							"Top Ten:" + Boolean.toString(topTen.isSelected()))
					.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
			String criteres = StringUtils.join(c, " ");
			LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(title));
			csvHeader.add("Critères: " + criteres);
			String name = CsvFile.exportCsv("search", PanelUtils.convertDataVectorToList(tableResult), null,
					csvHeader.toArray(new String[title.length + 1]));
			try {
				FichierUtils.openFileInExcel(Optional.of(name));
			} catch (MyException e1) {
				LOG.error("Erreur de l'ouverture avec excel du fichier: " + name, e1);
			}
		});
		top.add(csv);
		header.add(top);
	}

	/**
	 * Initialise les champs de recherche.
	 * 
	 * @param header le header de l'onglet
	 */
	private void initSearchFields(JPanel header) {
		LOG.debug("Start initSearchFields");
		JPanel searchFields = new JPanel();
		searchFields.setLayout(new GridLayout(2, 5));

		// Artiste
		JPanel artistPanel = new JPanel();
		JLabel artistLabel = PanelUtils.createJLabel("Artiste : ", 200);
		artist = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport.install((JComboBox<?>) artist.getInput(),
				GlazedLists.eventListOf(OngletPanel.getArtistList().toArray()));
		PanelUtils.setSize(artistPanel, 300, PanelUtils.PANEL_HEIGHT);
		artistPanel.add(artistLabel);
		artistPanel.add(artist);
		searchFields.add(artistPanel);

		// Titre
		JPanel titrePanel = new JPanel();
		JLabel titreLabel = PanelUtils.createJLabel("Titre : ", 180);
		titre = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport.install((JComboBox<?>) titre.getInput(),
				GlazedLists.eventListOf(OngletPanel.getTitleList().toArray()));
		titrePanel.add(titreLabel);
		titrePanel.add(titre);
		searchFields.add(titrePanel);

		// SearchMethod
		JPanel searchMethodPanel = new JPanel();
		JLabel searchMethodLabel = PanelUtils.createJLabel("Méthode de recherche : ", 150);
		searchMethod = new JComboBox<>(
				Arrays.asList(SearchMethod.values()).stream().map(v -> v.getValue()).toArray(String[]::new));
		searchMethod.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		searchMethodPanel.add(searchMethodLabel);
		searchMethodPanel.add(searchMethod);
		searchFields.add(searchMethodPanel);

		// Nom du fichier
		JPanel fileNamePanel = new JPanel();
		JLabel fileNameLabel = PanelUtils.createJLabel("Nom du fichier : ", 250);
		fileName = new MyInputText(JTextField.class, 150);
		fileName.getInput().addFocusListener(PanelUtils.selectAll);
		fileNamePanel.add(fileNameLabel);
		fileNamePanel.add(fileName);
		searchFields.add(fileNamePanel);

		// Auteur
		JPanel authorPanel = new JPanel();
		JLabel authorLabel = PanelUtils.createJLabel("Auteur : ", 150);
		author = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport.install((JComboBox<?>) author.getInput(),
				GlazedLists.eventListOf(OngletPanel.getAuthorList().toArray()));
		authorPanel.add(authorLabel);
		authorPanel.add(author);
		searchFields.add(authorPanel);

		// Type
		JPanel typePanel = new JPanel();
		JLabel typeLabel = PanelUtils.createJLabel("Type : ", 180);
		type = new JComboCheckBox(
				Arrays.asList(RecordType.values()).stream().map(t -> t.getRecordType()).collect(Collectors.toList()));
		type.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		typePanel.add(typeLabel);
		typePanel.add(type);
		searchFields.add(typePanel);

		// Range
		JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel rangeLabel = PanelUtils.createJLabel("Année(s) du classement : ", 200);
		rangeB = new MyInputText(JTextField.class, 50);
		rangeE = new MyInputText(JTextField.class, 50);
		rangeB.getInput().addFocusListener(PanelUtils.selectAll);
		rangeE.getInput().addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				JTextField source = (JTextField) e.getSource();
				if (StringUtils.isNotBlank(rangeB.getText())) {
					source.setText(rangeB.getText());
					source.selectAll();
				}
			}
		});
		rangePanel.add(rangeLabel);
		rangePanel.add(rangeB);
		rangePanel.add(rangeE);
		searchFields.add(rangePanel);

		// Categorie
		JPanel catPanel = new JPanel();
		JLabel catLabel = PanelUtils.createJLabel("Catégorie : ", 150);
		cat = new JComboCheckBox(
				Arrays.asList(Cat.values()).stream().map(c -> c.getCat()).collect(Collectors.toList()));
		cat.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		searchFields.add(catPanel);

		// Publi
		JPanel publiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel publiLabel = PanelUtils.createJLabel("Année de publication : ", 240);
		publi = new MyInputText(JTextField.class, 100);
		searchRange = new JComboBox<String>(
				Arrays.asList(SearchRange.values()).stream().map(v -> v.getValue()).toArray(String[]::new));
		searchRange.setPreferredSize(new Dimension(45, PanelUtils.COMPONENT_HEIGHT));
		publi.getInput().addFocusListener(PanelUtils.selectAll);
		publiPanel.add(publiLabel);
		publiPanel.add(searchRange);
		publiPanel.add(publi);
		searchFields.add(publiPanel);

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
		searchFields.add(inFilesPanel);

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

	private void initTable() {
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
		model = new CompoSearchPanelModel(new Object[0][title.length - 1], title);
		tableResult.setModel(model);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
			@Override
			public boolean isSortable(int column) {
				if (column != INDEX_LINE_NUMBER)
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
				List<SortKey> sortKeys = e.getSource().getSortKeys();
				if (!sortKeys.isEmpty()) {
					if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
						// Sort of deleted column and store sorted column and order
						int column = sortKeys.get(0).getColumn();
						if (column == INDEX_SELECTED) {
							sortedColumn = INDEX_DELETED;
							sortDeletedOrder = sortDeletedOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING
									: SortOrder.ASCENDING;
							sortOrder = sortDeletedOrder;
							List<SortKey> list = new LinkedList<>(
									Arrays.asList(new RowSorter.SortKey(sortedColumn, sortDeletedOrder)));
							tableResult.getRowSorter().getSortKeys().stream().forEach(s -> list.add(s));
							tableResult.getRowSorter().setSortKeys(list);
						} else {
							sortOrder = sortKeys.get(0).getSortOrder();
							sortedColumn = column;
						}
					}
					// Handling of line numbers
					for (int i = 0; i < tableResult.getRowCount(); i++) {
						tableResult.setValueAt(i + 1, i, INDEX_LINE_NUMBER);
					}
				}
			}
		});
		bottom.add(new JScrollPane(tableResult), BorderLayout.CENTER);

		this.add(bottom);
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		deleteLabel.setText("");
		List<Composition> allCompo = ImportXML.importXML(Constant.getFinalFilePath());
		if (CollectionUtils.isNotEmpty(allCompo)) {
			Map<String, String> criteria = new HashMap<>();
			criteria.put(SearchUtils.CRITERIA_ARTIST, artist.getText());
			criteria.put(SearchUtils.CRITERIA_TITRE, titre.getText());
			criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.getSelectedItems());
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, publi.getText());
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE, (String) searchRange.getSelectedItem());
			criteria.put(SearchUtils.CRITERIA_FILENAME, fileName.getText());
			criteria.put(SearchUtils.CRITERIA_AUTHOR, author.getText());
			criteria.put(SearchUtils.CRITERIA_CAT, cat.getSelectedItems());
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
					SearchMethod.getByValue((String) searchMethod.getSelectedItem()), deleted.isSelected(), true));
			updateTable();
		}
		LOG.debug("End searchAction");
	}

	private void updateTable() {
		LOG.debug("Start updateTable");
		model.setRowCount(0);
		model.setDataVector(
				CompositionUtils.convertCompositionListToVector(compoResult, null, false, true, true, true, true),
				new Vector<>(Arrays.asList(title)));
		countLabel.setText(compoResult.size() + " résultats");
		if (sortedColumn == null) {
			sortedColumn = INDEX_SCORE;
			sortOrder = SortOrder.DESCENDING;
		}
		tableResult.getRowSorter()
				.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedColumn, sortOrder)));
		PanelUtils.colRenderer(tableResult, false, INDEX_DELETED, INDEX_TYPE);
		for (int i = 0; i < tableResult.getRowCount(); i++) {
			tableResult.setValueAt(i + 1, i, INDEX_LINE_NUMBER);
		}
		selectedRow = -1;
		tableResult.getColumnModel().getColumn(INDEX_LINE_NUMBER).setMinWidth(40);
		tableResult.getColumnModel().getColumn(INDEX_LINE_NUMBER).setMaxWidth(40);
		tableResult.removeColumn(tableResult.getColumnModel().getColumn(INDEX_DELETED));
		model.fireTableDataChanged();
		tableResult.repaint();
		LOG.debug("Start updateTable");
	}

	private void cleanAction() {
		LOG.debug("Start cleanAction");
		artist.setText(null);
		titre.setText(null);
		type.clearSelection();
		searchMethod.setSelectedItem(SearchMethod.CONTAINS.getValue());
		publi.setText("");
		fileName.setText("");
		author.setText(null);
		cat.clearSelection();
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
				new Dimension(800, 150), v, INDEX_ARTIST, INDEX_TITRE, INDEX_TYPE);
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
					v.get(INDEX_TYPE), isDeleted);
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
