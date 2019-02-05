package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
import org.kordamp.ikonli.swing.FontIcon;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.ModificationComposition;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.dialog.ModifyFichierDialog;
import pmb.music.AllMusic.view.model.CompoFichierPanelModel;
import pmb.music.AllMusic.view.model.FichierPanelModel;
import pmb.music.AllMusic.view.popup.CompositionPopupMenu;
import pmb.music.AllMusic.view.popup.FichierPopupMenu;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs
 * compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends JPanel implements ModificationComposition {
	private static final long serialVersionUID = 8581952935884211032L;

	private static final Logger LOG = Logger.getLogger(FichierPanel.class);

	private static final int MIN_HEIGHT_TABLE = 41;
	private static final int MAX_HEIGHT_TABLE = 82;

	public static final int INDEX_FILE_LINE_NUMBER = 0;
	public static final int INDEX_FILE_AUTHOR = 1;
	public static final int INDEX_FILE_FILE_NAME = 2;
	public static final int INDEX_FILE_TYPE = 3;
	public static final int INDEX_FILE_PUBLISH = 4;
	public static final int INDEX_FILE_CAT = 5;
	public static final int INDEX_FILE_RANGE = 6;
	public static final int INDEX_PERCENT_DELETED = 7;
	public static final int INDEX_CREATE_DATE = 8;
	public static final int INDEX_FILE_SCORE = 9;
	public static final int INDEX_FILE_SCORE_DELETED = 10;
	public static final int INDEX_FILE_SIZE = 11;
	public static final int INDEX_FILE_SORTED = 12;

	public static final int INDEX_COMPO_LINE_NUMBER = 0;
	public static final int INDEX_COMPO_ARTIST = 1;
	public static final int INDEX_COMPO_TITLE = 2;
	public static final int INDEX_COMPO_TYPE = 3;
	public static final int INDEX_COMPO_RANK = 4;
	public static final int INDEX_COMPO_FILE_SIZE = 5;
	public static final int INDEX_COMPO_SCORE = 6;
	public static final int INDEX_COMPO_DECILE = 7;
	public static final int INDEX_COMPO_SELECTED = 8;
	public static final int INDEX_COMPO_DELETED = 9;

	// Search components
	private MyInputText auteur;
	private MyInputText filename;
	private JComboBoxInput publi;
	private MyInputRange range;
	private JComboCheckBox cat;
	private JComboCheckBox type;
	private JCheckBox sorted;
	private JCheckBox deleted;
	private JButton search;
	private JLabel resultLabel;

	// Fichier componants
	private JPanel filePanel;
	private JTable tableFiles;
	private FichierPanelModel fichieModel;
	private JButton hideFileList;
	private boolean showFichierTable = true;
	private Integer sortedFichierColumn;
	private SortOrder sortFichierOrder;
	private int selectedFichierRow = -1;
	private String selectedFichierName = "";
	private Map<Fichier, List<Composition>> data;
	private Map<Fichier, List<Composition>> searchResult;
	private FichierPopupMenu popupFichier;

	// Composition componants
	private JPanel compoPanel;
	private JTable tableCompo;
	private CompoFichierPanelModel compoModel;
	private List<Composition> compositionList;
	private JButton hideCompoList;
	private boolean showCompoTable = true;
	private Integer sortedCompoColumn;
	private SortOrder sortCompoOrder;
	private SortOrder sortCompoDeletedOrder = SortOrder.ASCENDING;
	private int selectedCompoRow = -1;
	private CompositionPopupMenu popupComposition;

	private Dimension parentSize;
	private final ArtistPanel artistPanel;

	private static final String[] headerFiles = { "#", "Auteur", "Nom du fichier", "Type", "Publication", "Categorie",
			"Dates", "Supprimés", "Création", "Score", "Score Supprimés", "Taille", "Classé" };
	private static final String[] headerCompo = { "#", "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers",
			"Score", "", "", "" };

	/**
	 * Constructeur de {@link FichierPanel}.
	 * 
	 * @param artistPanel pour arreter ou redemarrer la calcul des données
	 * 
	 */
	public FichierPanel(ArtistPanel artistPanel) {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.artistPanel = artistPanel;

		LOG.debug("End FichierPanel");
	}

	/**
	 * Initialise tous les composants du {@link FichierPanel}.
	 */
	public void initPanel() {
		LOG.debug("Start initPanel");

		parentSize = this.getParent().getPreferredSize();
		initSearchBtn();
		initData();
		initFichierTable();
		initCompoTable();

		LOG.debug("End initPanel");
	}

	/**
	 * Initialise les composants de recherche.
	 */
	private void initSearchBtn() {
		JPanel header = new JPanel(new GridLayout(2, 1));
		initInputs(header);
		initButtons(header);
		PanelUtils.setSize(header, (int) parentSize.getWidth(), Math.floorDiv(15 * (int) parentSize.getHeight(), 100));
		this.add(header);
	}

	private void initInputs(JPanel header) {
		JPanel inputs = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		// Auteur
		auteur = (MyInputText) new ComponentBuilder(MyInputText.class).withParent(inputs)
				.withValues(OngletPanel.getAuthorList()).withLabel("Auteur : ").withPanelWidth(190)
				.withFilterContains(true).withFlowLayout(true).withComponentWidth(150).withLabelWidth(140).build();
		// Nom du fichier
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(namePanel, 240, ComponentBuilder.PANEL_HEIGHT);
		JLabel nameLabel = ComponentBuilder.buildJLabel("Nom du fichier : ", 190);
		filename = new MyInputText(JTextField.class, 180);
		filename.getInput().addFocusListener(PanelUtils.selectAll);
		namePanel.add(nameLabel);
		namePanel.add(filename);
		inputs.add(namePanel);
		// Publi
		publi = (JComboBoxInput) new ComponentBuilder(JComboBoxInput.class).withParent(inputs)
				.withValues(Arrays.asList(SearchRange.values()).stream().map(SearchRange::getValue)
						.collect(Collectors.toList()))
				.withLabel("Année de publication : ").withPanelWidth(250).withComponentWidth(100).withLabelWidth(210)
				.build();
		// Range
		range = (MyInputRange) new ComponentBuilder(MyInputRange.class).withParent(inputs)
				.withLabel("Année(s) du classement : ").withPanelWidth(270).withComponentWidth(140).withLabelWidth(180)
				.withFlowLayout(true).build();
		// Categorie
		cat = (JComboCheckBox) new ComponentBuilder(JComboCheckBox.class).withParent(inputs)
				.withValues(Arrays.asList(Cat.values()).stream().map(Cat::getCat).collect(Collectors.toList()))
				.withFlowLayout(true).withLabel("Catégorie : ").withPanelWidth(200).withComponentWidth(120)
				.withLabelWidth(150).build();
		// Type
		type = (JComboCheckBox) new ComponentBuilder(JComboCheckBox.class).withParent(inputs)
				.withValues(Arrays.asList(RecordType.values()).stream().map(RecordType::getRecordType)
						.collect(Collectors.toList()))
				.withFlowLayout(true).withLabel("Type : ").withPanelWidth(200).withComponentWidth(150)
				.withLabelWidth(150).build();
		// Sorted
		JPanel sortedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(sortedPanel, 100, ComponentBuilder.PANEL_HEIGHT);
		JLabel sortedLabel = ComponentBuilder.buildJLabel("Classé: ", 150);
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(80, ComponentBuilder.COMPONENT_HEIGHT));
		sorted.setHorizontalAlignment(SwingConstants.CENTER);
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		inputs.add(sortedPanel);
		// Deleted
		JPanel deletedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(deletedPanel, 100, ComponentBuilder.PANEL_HEIGHT);
		JLabel deletedLabel = ComponentBuilder.buildJLabel("Supprimé: ", 150);
		deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(80, ComponentBuilder.COMPONENT_HEIGHT));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);
		inputs.add(deletedPanel);
		header.add(inputs);
	}

	private void initButtons(JPanel header) {
		JPanel buttons = new JPanel(new GridLayout(1, 7));
		// SEARCH
		search = ComponentBuilder.buildJButton("Rechercher", 120, Constant.ICON_SEARCH);
		search.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		buttons.add(search);
		// RESET
		JButton reset = ComponentBuilder.buildJButton("Réinitialiser", 120, Constant.ICON_ERASE);
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		buttons.add(reset);
		// hideFileList
		hideFileList = ComponentBuilder.buildJButton("Cacher la liste des fichiers", 180, Constant.ICON_HIDE);
		hideFileList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showFichierTable = !showFichierTable;
				filePanel.setVisible(showFichierTable);
				if (showFichierTable) {
					hideFileList.setText("Cacher la liste des fichiers");
					hideFileList.setIcon(FontIcon.of(Constant.ICON_HIDE));
					setTableSize(compoPanel, MIN_HEIGHT_TABLE);
				} else {
					hideFileList.setText("Afficher la liste des fichiers");
					hideFileList.setIcon(FontIcon.of(Constant.ICON_SHOW));
					setTableSize(compoPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		buttons.add(hideFileList);
		// hideCompoList
		hideCompoList = ComponentBuilder.buildJButton("Cacher la liste des compositions", 200, Constant.ICON_HIDE);
		hideCompoList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showCompoTable = !showCompoTable;
				compoPanel.setVisible(showCompoTable);
				if (showCompoTable) {
					hideCompoList.setText("Cacher la liste des compositions");
					hideCompoList.setIcon(FontIcon.of(Constant.ICON_HIDE));
					setTableSize(filePanel, MIN_HEIGHT_TABLE);
				} else {
					hideCompoList.setText("Afficher la liste des compositions");
					hideCompoList.setIcon(FontIcon.of(Constant.ICON_SHOW));
					setTableSize(filePanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		buttons.add(hideCompoList);
		// Delete Btn
		JButton delete = ComponentBuilder.buildJButton("<html>Supprimer les compositions sélectionnées</html>", 200,
				Constant.ICON_DELETE);
		delete.addActionListener((ActionEvent e) -> {
			PanelUtils.deleteCompositionAction(artistPanel, this, compositionList, compoModel.getSelected(),
					resultLabel, INDEX_COMPO_ARTIST, INDEX_COMPO_TITLE, INDEX_COMPO_TYPE);
			updateCompoTable(compositionList, selectedFichierName, false);
		});
		buttons.add(delete);
		// CSV
		JButton csv = ComponentBuilder.buildJButton("<html>Télécharger la liste des fichiers en CSV</html>", 300,
				Constant.ICON_DOWNLOAD);
		csv.addActionListener((ActionEvent e) -> {
			List<String> c = Arrays
					.asList(publi.getInput().getText(), range.getFirst().getText(), range.getSecond().getText(),
							filename.getText(), cat.getSelectedItems(), type.getSelectedItems(), auteur.getText(),
							"Sorted:" + Boolean.toString(sorted.isSelected()),
							"Deleted:" + Boolean.toString(deleted.isSelected()))
					.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
			String criteres = StringUtils.join(c, " ");
			LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(headerFiles));
			csvHeader.add("Critères: " + criteres);
			String name = CsvFile.exportCsv("files", PanelUtils.convertDataVectorToList(tableFiles), null,
					csvHeader.toArray(new String[headerFiles.length + 1]));
			try {
				FichierUtils.openFileInExcel(Optional.of(name));
			} catch (MyException e1) {
				LOG.error("Erreur de l'ouverture avec excel du fichier: " + name, e1);
			}
		});
		buttons.add(csv);
		// Label pour afficher les resultats
		JPanel resultPanel = new JPanel();
		resultLabel = ComponentBuilder.buildJLabel("", 400);
		resultLabel.setForeground(new Color(8, 187, 81));
		Font labelFont2 = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 20));
		resultPanel.add(resultLabel);
		buttons.add(resultPanel);
		header.add(buttons);
	}

	private void initFichierTable() {
		filePanel = new JPanel(new BorderLayout());

		tableFiles = new JTable();
		tableFiles.setAutoCreateRowSorter(true);
		tableFiles.setRowHeight(30);
		tableFiles.setFillsViewportHeight(true);
		tableFiles.getTableHeader().setResizingAllowed(true);
		tableFiles.setBackground(UIManager.getColor("Label.background"));
		tableFiles.setFont(UIManager.getFont("Label.font"));
		tableFiles.setBorder(UIManager.getBorder("Label.border"));
		fichieModel = new FichierPanelModel(new Object[0][headerFiles.length], headerFiles);
		tableFiles.setModel(fichieModel);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(fichieModel) {
			@Override
			public boolean isSortable(int column) {
				return column != INDEX_FILE_LINE_NUMBER;
			}
		};
		tableFiles.setRowSorter(sorter);
		tableFiles.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForFileTable(e);
			}
		});
		popupFichier = new FichierPopupMenu(tableFiles, INDEX_FILE_FILE_NAME, INDEX_FILE_AUTHOR);
		tableFiles.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					popupFichier.show(e);
				} else {
					selectedFichierRow = PanelUtils.keyShortcutAction(e, selectedFichierRow, sortedFichierColumn);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableFiles.getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
			List<? extends SortKey> sortKeys = ((RowSorter<?>) e.getSource()).getSortKeys();
			if (!sortKeys.isEmpty()) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					// Store sorted column and order
					sortedFichierColumn = sortKeys.get(0).getColumn();
					sortFichierOrder = sortKeys.get(0).getSortOrder();
				}
				// Handling of line numbers
				for (int i = 0; i < tableFiles.getRowCount(); i++) {
					tableFiles.setValueAt(i + 1, i, INDEX_FILE_LINE_NUMBER);
				}
			}
		});

		filePanel.add(new JScrollPane(tableFiles), BorderLayout.CENTER);
		this.add(filePanel);
		setTableSize(filePanel, MIN_HEIGHT_TABLE);
	}

	private void initCompoTable() {
		compoPanel = new JPanel(new BorderLayout());

		tableCompo = new JTable();
		tableCompo.setAutoCreateRowSorter(true);
		tableCompo.setRowHeight(30);
		tableCompo.setFillsViewportHeight(true);
		tableCompo.getTableHeader().setResizingAllowed(true);
		tableCompo.setBackground(UIManager.getColor("Label.background"));
		tableCompo.setFont(UIManager.getFont("Label.font"));
		tableCompo.setBorder(UIManager.getBorder("Label.border"));
		compoModel = new CompoFichierPanelModel(new Object[0][headerCompo.length - 1], headerCompo);
		tableCompo.setModel(compoModel);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(compoModel) {
			@Override
			public boolean isSortable(int column) {
				return (column != INDEX_COMPO_LINE_NUMBER);
			}
		};
		tableCompo.setRowSorter(sorter);
		tableCompo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForCompoTable(e);
			}
		});
		popupComposition = new CompositionPopupMenu(tableCompo, this.getClass(), INDEX_COMPO_ARTIST, INDEX_COMPO_TITLE);
		tableCompo.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					popupComposition.show(e);
				} else {
					selectedCompoRow = PanelUtils.keyShortcutAction(e, selectedCompoRow, sortedCompoColumn);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableCompo.getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
			List<? extends SortKey> sortKeys = ((RowSorter<?>) e.getSource()).getSortKeys();
			if (!sortKeys.isEmpty()) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					// Sort of deleted column and store sorted column and order
					int column = sortKeys.get(0).getColumn();
					if (column == INDEX_COMPO_SELECTED) {
						sortedCompoColumn = INDEX_COMPO_DELETED;
						sortCompoDeletedOrder = sortCompoDeletedOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING
								: SortOrder.ASCENDING;
						sortCompoOrder = sortCompoDeletedOrder;
						List<SortKey> list = new LinkedList<>(
								Arrays.asList(new RowSorter.SortKey(sortedCompoColumn, sortCompoDeletedOrder)));
						tableCompo.getRowSorter().getSortKeys().stream().forEach(list::add);
						tableCompo.getRowSorter().setSortKeys(list);
					} else {
						sortCompoOrder = sortKeys.get(0).getSortOrder();
						sortedCompoColumn = column;
					}
				}
				// Handling of line numbers
				for (int i = 0; i < tableCompo.getRowCount(); i++) {
					tableCompo.setValueAt(i + 1, i, INDEX_COMPO_LINE_NUMBER);
				}
			}
		});

		compoPanel.add(new JScrollPane(tableCompo), BorderLayout.CENTER);
		this.add(compoPanel);
		setTableSize(compoPanel, MIN_HEIGHT_TABLE);
	}

	private void initData() {
		LOG.debug("Start initData");
		data = new ConcurrentHashMap<>();
		ImportXML.importXML(Constant.getFinalFilePath()).parallelStream()
				.forEach(c -> c.getFiles().parallelStream().forEach(f -> {
					Optional<Entry<Fichier, List<Composition>>> entry = findFichierInMap(f.getFileName());
					if (entry.isPresent()) {
						data.get(entry.get().getKey()).add(c);
					} else {
						data.put(f, new LinkedList<>(Arrays.asList(c)));
					}
				}));
		searchResult = new ConcurrentHashMap<>(); // the map displays in the table
		data.entrySet().stream().forEach(e -> searchResult.put(e.getKey(),
				e.getValue().stream().map(Composition::new).collect(Collectors.toList())));
		LOG.debug("End initData");
	}

	private Optional<Entry<Fichier, List<Composition>>> findFichierInMap(String fileName) {
		return data.entrySet().stream()
				.filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey().getFileName(), fileName)).findFirst();
	}

	private void mouseActionForFileTable(MouseEvent e) {
		LOG.debug("Start mouseActionForFileTable");
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
		popupFichier.initDataAndPosition(e, selectedRow);
		if (!selectedRow.isPresent()) {
			return;
		}
		selectedFichierName = selectedRow.get().get(INDEX_FILE_FILE_NAME);
		if (e.getClickCount() == 1 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse, open");
			// Affiche les compositions du fichier sélectionné
			compositionList = findFichierInMap(selectedRow.get().get(INDEX_FILE_FILE_NAME)).get().getValue();
			if (!deleted.isSelected()) {
				compositionList = compositionList.stream().filter(c -> !c.isDeleted()).collect(Collectors.toList());
			}
			updateCompoTable(compositionList, selectedFichierName, true);
			LOG.debug("End left mouse, open");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			popupFichier.show(e);
		}
		LOG.debug("End mouseActionForFileTable");
	}

	private void mouseActionForCompoTable(MouseEvent e) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
		popupComposition.initDataAndPosition(e, selectedRow);
		if (!selectedRow.isPresent()) {
			return;
		}
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse");
			// Ouvre une popup pour afficher les fichiers de la
			// composition sélectionnée
			try {
				DialogFileTable pop = new DialogFileTable(null, "Fichier", true,
						Arrays.asList(CompositionUtils.findByArtistTitreAndType(compositionList,
								selectedRow.get().get(INDEX_COMPO_ARTIST), selectedRow.get().get(INDEX_COMPO_TITLE),
								selectedRow.get().get(INDEX_COMPO_TYPE), true)),
						400, DialogFileTable.INDEX_AUTEUR);
				pop.showDialogFileTable();
			} catch (MyException e1) {
				LOG.error("Ereur lors de l'affichage des fichier d'une compo", e1);
			}
			LOG.debug("End left mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			popupComposition.show(e);
		}
	}

	/**
	 * Launchs a dialog to modify the selected {@link Fichier}.
	 * 
	 * @param selected the selected row representing a fichier
	 */
	public void modifyFichierAction(Vector<String> selected) {
		LOG.debug("Start modifyFichierAction");
		resultLabel.setText("");
		String fileName = selected.get(INDEX_FILE_FILE_NAME);
		// Index du fichier dans le tableau
		Fichier key = findFichierInMap(fileName).get().getKey();
		// Lancement de la popup de modification
		ModifyFichierDialog md = new ModifyFichierDialog(null, "Modifier un fichier", true, selected);
		md.showModifyFichierDialog();
		Vector<String> newFichier;
		if (md.isSendData()) {
			// On recupère le fichier si il a bien été modifié
			LOG.debug("Fichier modifiée");
			newFichier = md.getFichier();
		} else {
			LOG.debug("Aucune modification");
			return;
		}
		// Retire les caractères interdits pour windows
		if (Arrays.asList(Constant.getForbiddenCharactersFilename()).stream()
				.anyMatch(s -> newFichier.get(INDEX_FILE_FILE_NAME).contains(s))) {
			Arrays.asList(Constant.getForbiddenCharactersFilename()).stream().forEach(
					s -> newFichier.set(INDEX_FILE_FILE_NAME, newFichier.get(INDEX_FILE_FILE_NAME).replaceAll(s, "")));
		}
		Fichier modifiedFichier = null;
		try {
			// Modification du fichier
			modifiedFichier = FichierUtils.modifyFichier(fileName, newFichier.get(INDEX_FILE_FILE_NAME),
					newFichier.get(INDEX_FILE_PUBLISH), newFichier.get(INDEX_FILE_RANGE),
					newFichier.get(INDEX_FILE_CAT), newFichier.get(INDEX_FILE_SIZE), newFichier.get(INDEX_FILE_SORTED));
		} catch (MyException e) {
			String log = "Erreur pendant FichierUtils.modifyFichier";
			LOG.error(log, e);
			resultLabel.setText(log + e);
			return;
		}
		// Mise à jour du JTable
		List<Composition> list = data.get(key);
		list.stream().forEach(FichierUtils.modifyOneFile(fileName, newFichier.get(INDEX_FILE_FILE_NAME),
				newFichier.get(INDEX_FILE_PUBLISH), newFichier.get(INDEX_FILE_RANGE), newFichier.get(INDEX_FILE_CAT),
				newFichier.get(INDEX_FILE_SIZE), newFichier.get(INDEX_FILE_SORTED)));
		data.remove(key);
		data.put(modifiedFichier, list);
		updateFileTable();
		resultLabel.setText("Fichier modifié");
		LOG.debug("End modifyFichierAction");

	}

	/**
	 * Launchs a dialog to modify the selected composition.
	 * 
	 * @param selected the selected row representing a composition
	 */
	@Override
	public void modifyCompositionAction(Vector<String> selected) {
		LOG.debug("Start modifyCompositionAction");
		PanelUtils.modificationCompositionAction(artistPanel, this, selected, compositionList, INDEX_COMPO_ARTIST,
				INDEX_COMPO_TITLE, INDEX_COMPO_TYPE, INDEX_COMPO_DELETED, resultLabel);
		updateCompoTable(compositionList, selectedFichierName, false);
		LOG.debug("End modifyCompositionAction");
	}

	private void searchAction() {
		LOG.debug("Start searchAction");
		resultLabel.setText("");
		// Filters on record type
		searchResult = data.entrySet().parallelStream().filter(e -> {
			if (StringUtils.isNotBlank(type.getSelectedItems())) {
				return e.getValue().stream()
						.anyMatch(c -> Arrays.asList(StringUtils.split(type.getSelectedItems(), ";")).stream()
								.anyMatch((t -> c.getRecordType() == RecordType.getByValue(t))));
			} else {
				return true;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		if (!searchResult.keySet().isEmpty()) {
			// Filter on the files list with the others criteria
			JaroWinklerDistance jaro = new JaroWinklerDistance();
			Map<String, String> criteria = new HashMap<>();
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, publi.getInput().getText());
			criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE, (String) publi.getComboBox().getSelectedItem());
			criteria.put(SearchUtils.CRITERIA_FILENAME, filename.getText());
			criteria.put(SearchUtils.CRITERIA_AUTHOR, auteur.getText());
			criteria.put(SearchUtils.CRITERIA_CAT, cat.getSelectedItems());
			criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, range.getFirst().getText());
			criteria.put(SearchUtils.CRITERIA_DATE_END, range.getSecond().getText());
			criteria.put(SearchUtils.CRITERIA_SORTED, sorted.isSelected() ? Boolean.TRUE.toString() : "");
			searchResult = searchResult.entrySet().parallelStream()
					.filter(e -> SearchUtils.filterFichier(SearchMethod.CONTAINS, jaro, criteria, e.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			// update files table
			updateFileTable();
		}
		resultLabel.setText(searchResult.keySet().size() + " fichiers trouvé(s) ");
		LOG.debug("End searchAction");
	}

	private void updateFileTable() {
		LOG.debug("Start updateFileTable");
		Composition c = new Composition();
		c.setFiles(new ArrayList<>(searchResult.keySet()));
		fichieModel.setRowCount(0);
		Vector<Vector<Object>> dataVector = FichierUtils.convertCompositionListToFichierVector(Arrays.asList(c), false,
				true);
		// Calculates score by getting the average of the score of each compositions
		// Calculates the percentage of deleted composition by the score
		for (int i = 0; i < dataVector.size(); i++) {
			Vector<Object> vector = dataVector.get(i);
			Optional<Entry<Fichier, List<Composition>>> entry = findFichierInMap(
					(String) vector.get(INDEX_FILE_FILE_NAME));
			if (entry.isPresent()) {
				LongSummaryStatistics score = entry.get().getValue().parallelStream()
						.map(compo -> CompositionUtils.calculateCompositionScore(
								OngletPanel.getScore().getLogMax(compo.getRecordType()),
								OngletPanel.getScore().getDoubleMedian(compo.getRecordType()), compo))
						.mapToLong(x -> x).summaryStatistics();
				vector.add(INDEX_FILE_SCORE, Math.round(score.getAverage()));
				long scoreDeleted = entry.get().getValue().parallelStream().filter(Composition::isDeleted)
						.map(compo -> CompositionUtils.calculateCompositionScore(
								OngletPanel.getScore().getLogMax(compo.getRecordType()),
								OngletPanel.getScore().getDoubleMedian(compo.getRecordType()), compo))
						.mapToLong(x -> x).sum();
				vector.add(INDEX_FILE_SCORE_DELETED, Math.round(100 * (double) scoreDeleted / score.getSum()) + " %");
			} else {
				LOG.warn("Entry not found ! ");
				vector.add(INDEX_FILE_SCORE, 0);
			}
		}
		fichieModel.setDataVector(dataVector, new Vector<>(Arrays.asList(headerFiles)));
		PanelUtils.colRenderer(tableFiles, true, null, INDEX_FILE_TYPE, INDEX_FILE_CAT, null, null, INDEX_FILE_SORTED,
				null);
		if (sortedFichierColumn == null) {
			sortedFichierColumn = INDEX_FILE_FILE_NAME;
			sortFichierOrder = SortOrder.ASCENDING;
		}
		tableFiles.getRowSorter()
				.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedFichierColumn, sortFichierOrder)));
		((TableRowSorter<?>) tableFiles.getRowSorter()).setComparator(INDEX_PERCENT_DELETED,
				MiscUtils.comparePercentage);
		((TableRowSorter<?>) tableFiles.getRowSorter()).setComparator(INDEX_FILE_SCORE_DELETED,
				MiscUtils.comparePercentage);
		for (int i = 0; i < tableFiles.getRowCount(); i++) {
			tableFiles.setValueAt(i + 1, i, INDEX_FILE_LINE_NUMBER);
		}
		tableFiles.getColumnModel().getColumn(INDEX_FILE_LINE_NUMBER).setMinWidth(30);
		tableFiles.getColumnModel().getColumn(INDEX_FILE_LINE_NUMBER).setMaxWidth(30);
		selectedFichierRow = -1;
		fichieModel.fireTableDataChanged();
		tableFiles.repaint();
		updateCompoTable(new ArrayList<>(), null, true);
		LOG.debug("Start updateFileTable");
	}

	/**
	 * Met à jour le tableau des compositions.
	 * 
	 * @param compo la liste des compositions à afficher
	 * @param selectedFile le fichier selectionné
	 * @param scrollTop true scroll to the top of the table
	 */
	private void updateCompoTable(List<Composition> compo, String selectedFile, boolean scrollTop) {
		LOG.debug("Start updateCompoTable");
		compoModel.setRowCount(0);
		if (selectedFile != null && !compo.isEmpty()) {
			compoModel.setDataVector(
					CompositionUtils.convertCompositionListToVector(compo, selectedFile, true, true, true, true, true),
					new Vector<>(Arrays.asList(headerCompo)));
		} else {
			compoModel.setDataVector(new Vector<Vector<Object>>(), new Vector<>(Arrays.asList(headerCompo)));
		}
		PanelUtils.colRenderer(tableCompo, false, INDEX_COMPO_DELETED, INDEX_COMPO_TYPE, null, INDEX_COMPO_DECILE,
				INDEX_COMPO_SCORE, null, null);
		compoModel.fireTableDataChanged();
		if (sortedCompoColumn == null) {
			sortedCompoColumn = INDEX_COMPO_RANK;
			sortCompoOrder = SortOrder.ASCENDING;
		}
		tableCompo.getRowSorter()
				.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedCompoColumn, sortCompoOrder)));
		selectedCompoRow = -1;
		tableCompo.getColumnModel().getColumn(INDEX_COMPO_LINE_NUMBER).setMinWidth(30);
		tableCompo.getColumnModel().getColumn(INDEX_COMPO_LINE_NUMBER).setMaxWidth(30);
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(INDEX_COMPO_DECILE));
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(INDEX_COMPO_DELETED - 1));
		tableCompo.repaint();
		if (scrollTop) {
			((JScrollPane) tableCompo.getParent().getParent()).getVerticalScrollBar().setValue(0);
		}
		LOG.debug("End updateCompoTable");
	}

	public void updateData() {
		new Thread(() -> {
			initData();
			searchAction();
		}).start();
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		auteur.setText(null);
		filename.setText("");
		publi.getInput().setText("");
		range.getFirst().setText("");
		range.getSecond().setText("");
		cat.clearSelection();
		type.clearSelection();
		sorted.setSelected(false);
		deleted.setSelected(false);
		LOG.debug("End resetAction");
	}

	private void setTableSize(JPanel panel, int height) {
		PanelUtils.setSize(panel, (int) parentSize.getWidth(),
				Math.floorDiv(height * (int) parentSize.getHeight(), 100));
	}

	public List<Composition> getCompoListFromData(Fichier file) {
		return getCompoList(file).getValue();
	}

	public void setCompoListFromData(Fichier file, List<Composition> list) {
		getCompoList(file).setValue(list);
	}

	private Entry<Fichier, List<Composition>> getCompoList(Fichier file) {
		return this.data.entrySet().stream()
				.filter(e -> StringUtils.equalsIgnoreCase(e.getKey().getAuthor(), file.getAuthor())
						&& StringUtils.equalsIgnoreCase(e.getKey().getFileName(), file.getFileName())
						&& NumberUtils.compare(e.getKey().getPublishYear(), file.getPublishYear()) == 0
						&& e.getKey().getSize().equals(file.getSize())
						&& e.getKey().getRangeDateBegin().equals(file.getRangeDateBegin())
						&& e.getKey().getRangeDateEnd().equals(file.getRangeDateEnd())
						&& e.getKey().getCategorie().equals(file.getCategorie())
						&& e.getKey().getSorted().equals(file.getSorted()))
				.findFirst().get();
	}

	public JTable getTableFiles() {
		return tableFiles;
	}

	public JTable getTableCompo() {
		return tableCompo;
	}

	public JButton getSearch() {
		return search;
	}
}
