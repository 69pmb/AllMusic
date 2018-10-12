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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.log4j.Logger;
import org.kordamp.ikonli.swing.FontIcon;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.XML.ExportXML;
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
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.dialog.ModifyCompositionDialog;
import pmb.music.AllMusic.view.dialog.ModifyFichierDialog;
import pmb.music.AllMusic.view.model.CompoFichierPanelModel;
import pmb.music.AllMusic.view.model.FichierPanelModel;
import pmb.music.AllMusic.view.popup.FichierPopupMenu;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs
 * compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends JPanel {
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
	public static final int INDEX_COMPO_SELECTED = 7;
	public static final int INDEX_COMPO_DELETED = 8;

	// Search components
	private MyInputText auteur;
	private MyInputText name;
	private JComboBox<String> searchRange;
	private MyInputText publi;
	private MyInputText rangeB;
	private MyInputText rangeE;
	private JComboCheckBox cat;
	private JComboCheckBox type;
	private JCheckBox sorted;
	private JCheckBox deleted;
	private JButton search;
	private JButton reset;
	private JLabel resultLabel;

	// Fichier componants
	private JPanel fichierPanel;
	private JTable tableFiles;
	private FichierPanelModel fichieModel;
	private JButton hideFileList;
	private boolean showFichierTable = true;
	private Integer sortedFichierColumn;
	private SortOrder sortFichierOrder;
	private int selectedFichierRow = -1;
	private String selectedFichierName = "";
	Map<Fichier, List<Composition>> data;
	Map<Fichier, List<Composition>> searchResult;

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

	private Dimension parentSize;

	private FichierPopupMenu popup;

	private static final String[] headerFiles = { "#", "Auteur", "Nom du fichier", "Type", "Publication", "Categorie",
			"Dates", "Supprimés", "Création", "Score", "Score Supprimés", "Taille", "Classé" };
	private static final String[] headerCompo = { "#", "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers",
			"Score", "", "" };

	/**
	 * Constructeur de {@link FichierPanel}.
	 * 
	 */
	public FichierPanel() {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		LOG.debug("End FichierPanel");
	}

	/**
	 * Initialise tous les composants du {@link FichierPanel}.
	 * 
	 * @param artistPanel le panel artiste
	 */
	public void initPanel(ArtistPanel artistPanel) {
		LOG.debug("Start initPanel");

		parentSize = this.getParent().getPreferredSize();
		initSearchBtn(artistPanel);
		initData();
		initFichierTable();
		initCompoTable(artistPanel);

		LOG.debug("End initPanel");
	}

	/**
	 * Initialise les composants de recherche.
	 * 
	 * @param artistPanel le panel artiste
	 */
	private void initSearchBtn(ArtistPanel artistPanel) {
		JPanel header = new JPanel(new GridLayout(2, 1));
		initInputs(header);
		initButtons(artistPanel, header);
		PanelUtils.setSize(header, (int) parentSize.getWidth(), Math.floorDiv(15 * (int) parentSize.getHeight(), 100));
		this.add(header);
	}

	private void initInputs(JPanel header) {
		JPanel inputs = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		// Auteur
		JPanel auteurPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(auteurPanel, 200, PanelUtils.PANEL_HEIGHT);
		JLabel auteurLabel = PanelUtils.createJLabel("Auteur : ", 150);
		auteur = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport<Object> install = AutoCompleteSupport.install((JComboBox<?>) auteur.getInput(),
				GlazedLists.eventListOf(OngletPanel.getAuthorList().toArray()));
		install.setFilterMode(TextMatcherEditor.CONTAINS);
		auteurPanel.add(auteurLabel);
		auteurPanel.add(auteur);
		inputs.add(auteurPanel);
		// Nom du fichier
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(namePanel, 250, PanelUtils.PANEL_HEIGHT);
		JLabel nameLabel = PanelUtils.createJLabel("Nom du fichier : ", 200);
		name = new MyInputText(JTextField.class, 180);
		name.getInput().addFocusListener(PanelUtils.selectAll);
		namePanel.add(nameLabel);
		namePanel.add(name);
		inputs.add(namePanel);
		// Publi
		JPanel publiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(publiPanel, 270, PanelUtils.PANEL_HEIGHT);
		JLabel publiLabel = PanelUtils.createJLabel("Année de publication : ", 230);
		publi = new MyInputText(JTextField.class, 100);
		publi.getInput().addFocusListener(PanelUtils.selectAll);
		searchRange = new JComboBox<String>(
				Arrays.asList(SearchRange.values()).stream().map(v -> v.getValue()).toArray(String[]::new));
		PanelUtils.setSize(searchRange, 45, PanelUtils.COMPONENT_HEIGHT);
		publiPanel.add(publiLabel);
		publiPanel.add(searchRange);
		publiPanel.add(publi);
		inputs.add(publiPanel);
		// Range
		JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(rangePanel, 270, PanelUtils.PANEL_HEIGHT);
		JLabel rangeLabel = PanelUtils.createJLabel("Année(s) du classement : ", 180);
		rangeB = new MyInputText(JTextField.class, 70);
		rangeE = new MyInputText(JTextField.class, 70);
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
		inputs.add(rangePanel);
		// Categorie
		JPanel catPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(catPanel, 250, PanelUtils.PANEL_HEIGHT);
		JLabel catLabel = PanelUtils.createJLabel("Catégorie : ", 180);
		cat = new JComboCheckBox(
				Arrays.asList(Cat.values()).stream().map(c -> c.getCat()).collect(Collectors.toList()));
		cat.setPreferredSize(new Dimension(120, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		inputs.add(catPanel);
		// Type
		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(typePanel, 250, PanelUtils.PANEL_HEIGHT);
		JLabel typeLabel = PanelUtils.createJLabel("Type : ", 180);
		type = new JComboCheckBox(
				Arrays.asList(RecordType.values()).stream().map(t -> t.getRecordType()).collect(Collectors.toList()));
		type.setPreferredSize(new Dimension(150, PanelUtils.COMPONENT_HEIGHT));
		typePanel.add(typeLabel);
		typePanel.add(type);
		inputs.add(typePanel);
		// Sorted
		JPanel sortedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(sortedPanel, 100, PanelUtils.PANEL_HEIGHT);
		JLabel sortedLabel = PanelUtils.createJLabel("Classé: ", 150);
		sorted = new JCheckBox();
		sorted.setPreferredSize(new Dimension(80, PanelUtils.COMPONENT_HEIGHT));
		sorted.setHorizontalAlignment(SwingConstants.CENTER);
		sortedPanel.add(sortedLabel);
		sortedPanel.add(sorted);
		inputs.add(sortedPanel);
		// Deleted
		JPanel deletedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(deletedPanel, 100, PanelUtils.PANEL_HEIGHT);
		JLabel deletedLabel = PanelUtils.createJLabel("Supprimé: ", 150);
		deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(80, PanelUtils.COMPONENT_HEIGHT));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);
		inputs.add(deletedPanel);
		header.add(inputs);
	}

	private void initButtons(ArtistPanel artistPanel, JPanel header) {
		JPanel buttons = new JPanel(new GridLayout(1, 7));
		// SEARCH
		search = PanelUtils.createJButton("Rechercher", 120, Constant.ICON_SEARCH);
		search.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		buttons.add(search);
		// RESET
		reset = PanelUtils.createJButton("Réinitialiser", 120, Constant.ICON_ERASE);
		reset.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				resetAction();
			}
		});
		buttons.add(reset);
		// hideFileList
		hideFileList = PanelUtils.createJButton("Cacher la liste des fichiers", 180, Constant.ICON_HIDE);
		hideFileList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showFichierTable = !showFichierTable;
				fichierPanel.setVisible(showFichierTable);
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
		hideCompoList = PanelUtils.createJButton("Cacher la liste des compositions", 200, Constant.ICON_HIDE);
		hideCompoList.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showCompoTable = !showCompoTable;
				compoPanel.setVisible(showCompoTable);
				if (showCompoTable) {
					hideCompoList.setText("Cacher la liste des compositions");
					hideCompoList.setIcon(FontIcon.of(Constant.ICON_HIDE));
					setTableSize(fichierPanel, MIN_HEIGHT_TABLE);
				} else {
					hideCompoList.setText("Afficher la liste des compositions");
					hideCompoList.setIcon(FontIcon.of(Constant.ICON_SHOW));
					setTableSize(fichierPanel, MAX_HEIGHT_TABLE);
				}
			}
		});
		buttons.add(hideCompoList);
		// Delete Btn
		JButton delete = PanelUtils.createJButton("<html>Supprimer les compositions sélectionnées</html>", 200,
				Constant.ICON_DELETE);
		delete.addActionListener((ActionEvent e) -> {
			PanelUtils.deleteCompositionAction(artistPanel, compositionList, compoModel.getSelected(), resultLabel,
					INDEX_COMPO_ARTIST, INDEX_COMPO_TITLE, INDEX_COMPO_TYPE);
			updateCompoTable(compositionList, selectedFichierName);
		});
		buttons.add(delete);
		// CSV
		JButton csv = PanelUtils.createJButton("<html>Télécharger la liste des fichiers en CSV</html>", 300,
				Constant.ICON_DOWNLOAD);
		csv.addActionListener((ActionEvent e) -> {
			List<String> c = Arrays
					.asList(publi.getText(), rangeB.getText(), rangeE.getText(), name.getText(), cat.getSelectedItems(),
							type.getSelectedItems(), auteur.getText(),
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
		resultLabel = PanelUtils.createJLabel("", 400);
		resultLabel.setForeground(new Color(8, 187, 81));
		Font labelFont2 = resultLabel.getFont();
		resultLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 20));
		resultPanel.add(resultLabel);
		buttons.add(resultPanel);
		header.add(buttons);
	}

	private void initFichierTable() {
		fichierPanel = new JPanel(new BorderLayout());

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
				if (column != INDEX_FILE_LINE_NUMBER)
					return true;
				else
					return false;
			};
		};
		tableFiles.setRowSorter(sorter);
		tableFiles.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForFileTable(e);
			}
		});
		popup = new FichierPopupMenu(tableFiles, INDEX_FILE_FILE_NAME, INDEX_FILE_AUTHOR);
		tableFiles.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					popup.show(e.getComponent(), (int) popup.getPoint().getX(), (int) popup.getPoint().getY());
				} else {
					selectedFichierRow = PanelUtils.keyShortcutAction(e, selectedFichierRow, sortedFichierColumn);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableFiles.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
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
		tableCompo.getTableHeader().setResizingAllowed(true);
		tableCompo.setBackground(UIManager.getColor("Label.background"));
		tableCompo.setFont(UIManager.getFont("Label.font"));
		tableCompo.setBorder(UIManager.getBorder("Label.border"));
		compoModel = new CompoFichierPanelModel(new Object[0][headerCompo.length - 1], headerCompo);
		tableCompo.setModel(compoModel);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(compoModel) {
			@Override
			public boolean isSortable(int column) {
				if (column != INDEX_COMPO_LINE_NUMBER)
					return true;
				else
					return false;
			};
		};
		tableCompo.setRowSorter(sorter);
		tableCompo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseActionForCompoTable(e, artistPanel);
			}
		});
		tableCompo.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Nothing to do
			}

			@Override
			public void keyReleased(KeyEvent e) {
				selectedCompoRow = PanelUtils.keyShortcutAction(e, selectedCompoRow, sortedCompoColumn);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Nothing to do
			}
		});
		tableCompo.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
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
							tableCompo.getRowSorter().getSortKeys().stream().forEach(s -> list.add(s));
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
			}
		});

		compoPanel.add(new JScrollPane(tableCompo), BorderLayout.CENTER);
		this.add(compoPanel);
		setTableSize(compoPanel, MIN_HEIGHT_TABLE);
	}

	private void initData() {
		LOG.debug("Start initData");
		data = new ConcurrentHashMap<>();
		ImportXML.importXML(Constant.getFinalFilePath()).parallelStream().forEach(c -> {
			c.getFiles().parallelStream().forEach(f -> {
				Optional<Entry<Fichier, List<Composition>>> entry = findFichierInMap(f.getFileName());
				if (entry.isPresent()) {
					data.get(entry.get().getKey()).add(c);
				} else {
					data.put(f, new LinkedList<>(Arrays.asList(c)));
				}
			});
		});
		searchResult = new ConcurrentHashMap<>(); // the map displays in the table
		data.entrySet().stream().forEach(e -> searchResult.put(e.getKey(),
				e.getValue().stream().map(c -> new Composition(c)).collect(Collectors.toList())));
		LOG.debug("End initData");
	}

	private Optional<Entry<Fichier, List<Composition>>> findFichierInMap(String fileName) {
		return data.entrySet().stream()
				.filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey().getFileName(), fileName)).findFirst();
	}

	private void mouseActionForFileTable(MouseEvent e) {
		LOG.debug("Start mouseActionForFileTable");
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
		popup.setPoint(e.getPoint());
		popup.setLocation(e.getLocationOnScreen());
		popup.setSelectedRow(selectedRow.orElse(null));
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
			updateCompoTable(compositionList, selectedFichierName);
			LOG.debug("End left mouse, open");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			popup.show(e.getComponent(), (int) popup.getPoint().getX(), (int) popup.getPoint().getY());
		}
		LOG.debug("End mouseActionForFileTable");
	}

	private void mouseActionForCompoTable(MouseEvent e, ArtistPanel artistPanel) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
		if (!selectedRow.isPresent()) {
			return;
		}
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start left mouse");
			// Popup pour modifier la composition
			modifyCompositionAction(artistPanel, selectedRow.get());
			LOG.debug("End left mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start right mouse");
			// Copie dans le clipboard l'artist et l'oeuvre
			StringSelection selection = new StringSelection(
					selectedRow.get().get(INDEX_COMPO_ARTIST) + " " + selectedRow.get().get(INDEX_COMPO_TITLE));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End right mouse");
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

	private void modifyCompositionAction(final ArtistPanel artistPanel, Vector<String> selected) {
		LOG.debug("Start modifyCompositionAction");
		resultLabel.setText("");
		artistPanel.interruptUpdateArtist(true);
		String label = "Élément modifié";
		Composition compoToModif;
		// On récupère la ligne selectionnée
		Vector<String> v = selected;
		List<Composition> importXML;
		importXML = ImportXML.importXML(Constant.getFinalFilePath());
		try {
			// On récupère la composition à modifier
			compoToModif = CompositionUtils.findByArtistTitreAndType(compositionList, v.get(INDEX_COMPO_ARTIST),
					v.get(INDEX_COMPO_TITLE), v.get(INDEX_COMPO_TYPE), true);
		} catch (MyException e1) {
			String log = "Erreur dans modifAction, impossible de trouver la compo à modifier";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}
		int indexOfXml = SearchUtils.indexOf(importXML, compoToModif);
		int indexOfResult = SearchUtils.indexOf(compositionList, compoToModif);
		// Lancement de la popup de modification
		ModifyCompositionDialog md = new ModifyCompositionDialog(null, "Modifier une composition", true,
				new Dimension(950, 150), v, INDEX_COMPO_ARTIST, INDEX_COMPO_TITLE, INDEX_COMPO_TYPE,
				INDEX_COMPO_DELETED);
		md.showModifyCompositionDialog();
		if (md.isSendData()) {
			// On recupère la compo si elle a bien été modifiée
			LOG.debug("Compo modifiée");
			v = md.getCompo();
		} else {
			LOG.debug("Aucune modification");
			return;
		}

		boolean isDeleted = false;
		// On modifie la composition
		compoToModif.setArtist(v.get(INDEX_COMPO_ARTIST));
		compoToModif.setTitre(v.get(INDEX_COMPO_TITLE));
		compoToModif.setRecordType(RecordType.valueOf(v.get(INDEX_COMPO_TYPE)));
		compoToModif.setDeleted(Boolean.valueOf(v.get(INDEX_COMPO_DELETED)));

		// Gestion du fichier final.xml
		importXML.remove(indexOfXml);
		compositionList.remove(indexOfResult);
		Composition compoExist = CompositionUtils.compoExist(importXML, compoToModif);
		if (compoExist == null) {
			LOG.debug("Pas de regroupement");
			importXML.add(compoToModif);
			compositionList.add(compoToModif);
		} else {
			LOG.debug("La compo existe déjà, on regroupe");
			// regroupement avec une autre composition
			isDeleted = compoToModif.isDeleted() || compoExist.isDeleted();
			compoExist.getFiles().addAll(compoToModif.getFiles());
			compoExist.setDeleted(isDeleted);
			compoToModif.setDeleted(isDeleted);
			// Liste des compositions affichées
			Composition compoExistResult = CompositionUtils.compoExist(compositionList, compoToModif);
			if (compoExistResult != null) {
				// La compo apparait bien dans les resultats de recherche
				compoExistResult.getFiles().addAll(compoToModif.getFiles());
				compoExistResult.setDeleted(isDeleted);
			}
		}
		try {
			ExportXML.exportXML(importXML, Constant.getFinalFile());
			artistPanel.updateArtistPanel();
		} catch (IOException e1) {
			String log = "Erreur lors de l'export du fichier final !!";
			LOG.error(log, e1);
			label = log;
		}

		// On modifier les fichier xml en conséquence
		try {
			CompositionUtils.modifyCompositionsInFiles(compoToModif, v.get(INDEX_COMPO_ARTIST),
					v.get(INDEX_COMPO_TITLE), v.get(INDEX_COMPO_TYPE), isDeleted);
		} catch (MyException e1) {
			String log = "Erreur lors de la modification d'une composition";
			LOG.error(log, e1);
			resultLabel.setText(log + e1);
			return;
		}

		resultLabel.setText(label);
		updateCompoTable(compositionList, selectedFichierName);
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
			searchResult = searchResult.entrySet().parallelStream().filter(e -> {
				return SearchUtils.filterFichier(SearchMethod.CONTAINS, jaro, publi.getText(),
						(String) searchRange.getSelectedItem(), name.getText(), auteur.getText(),
						cat.getSelectedItems(), rangeB.getText(), rangeE.getText(),
						sorted.isSelected() ? Boolean.TRUE.toString() : "", null, e.getKey());
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
				long scoreDeleted = entry.get().getValue().parallelStream().filter(co -> co.isDeleted())
						.map(compo -> CompositionUtils.calculateCompositionScore(
								OngletPanel.getScore().getLogMax(compo.getRecordType()),
								OngletPanel.getScore().getDoubleMedian(compo.getRecordType()), compo))
						.mapToLong(x -> x).sum();
				vector.add(INDEX_FILE_SCORE_DELETED, Math.round(100 * scoreDeleted / score.getSum()) + " %");
			} else {
				LOG.warn("Entry not found ! ");
				vector.add(INDEX_FILE_SCORE, 0);
			}
		}
		fichieModel.setDataVector(dataVector, new Vector<>(Arrays.asList(headerFiles)));
		PanelUtils.colRenderer(tableFiles, true, null, INDEX_FILE_TYPE);
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
		updateCompoTable(new ArrayList<>(), null);
		LOG.debug("Start updateFileTable");
	}

	/**
	 * Met à jour le tableau des compositions.
	 * 
	 * @param compo la liste des compositions à afficher
	 * @param selectedFile le fichier selectionné
	 */
	private void updateCompoTable(List<Composition> compo, String selectedFile) {
		LOG.debug("Start updateCompoTable");
		compoModel.setRowCount(0);
		if (selectedFile != null && !compo.isEmpty()) {
			compoModel.setDataVector(
					CompositionUtils.convertCompositionListToVector(compo, selectedFile, true, true, true, true, true),
					new Vector<>(Arrays.asList(headerCompo)));
		} else {
			compoModel.setDataVector(new Vector<Vector<Object>>(), new Vector<>(Arrays.asList(headerCompo)));
		}
		PanelUtils.colRenderer(tableCompo, false, INDEX_COMPO_DELETED, INDEX_COMPO_TYPE);
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
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(INDEX_COMPO_DELETED));
		tableCompo.repaint();
		((JScrollPane) tableCompo.getParent().getParent()).getVerticalScrollBar().setValue(0);
		LOG.debug("End updateCompoTable");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		auteur.setText(null);
		name.setText("");
		publi.setText("");
		rangeB.setText("");
		rangeE.setText("");
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
