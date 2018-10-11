/**
 * 
 */
package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.io.File;
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

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
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
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.model.ArtistModel;

/**
 * L'onglet Artiste, classement des artistes les plus cités.
 * 
 */
public class ArtistPanel extends JPanel {

	private static final long serialVersionUID = 2593372709628283573L;

	private static final Logger LOG = Logger.getLogger(ArtistPanel.class);
	public static final int INDEX_LINE_NUMBER = 0;
	public static final int INDEX_ARTIST = 1;
	public static final int INDEX_NB_TOTAL = 2;
	public static final int INDEX_DELETED = 5;
	public static final int INDEX_SCORE_TOTAL = 6;
	public static final int INDEX_SCORE_SONG = 7;
	public static final int INDEX_SCORE_ALBUM = 8;
	public static final int INDEX_SCORE_DELETED = 9;

	private MyInputText artist;
	private JComboBox<String> searchRange;
	private MyInputText publi;
	private MyInputText rangeB;
	private MyInputText rangeE;
	private MyInputText auteur;
	private JComboCheckBox cat;
	private JButton search;
	private JButton reset;
	private Integer sortedColumn;
	private SortOrder sortOrder;
	private JCheckBox deleted;

	private JTable table;

	private ArtistModel model;

	private Map<String, List<Composition>> data;
	private Map<String, List<Composition>> searchResult;

	private static final String[] title = { "#", "Artiste", "Nombre d'Occurrences", "Album", "Chanson", "Supprimés",
			"Score Total", "Score Album", "Score Chanson", "Score Supprimés" };

	private int selectedRow = -1;

	private transient Thread updateArtistThread;
	private final boolean withArtist;

	/**
	 * Génère le panel artiste.
	 * 
	 * @param withArtist if true the artist panel is displayed and the data is
	 *            calculated
	 */
	public ArtistPanel(boolean withArtist) {
		super();
		LOG.debug("Start ArtistPanel");
		this.withArtist = withArtist;
		this.setLayout(new BorderLayout());
		JPanel header = initHeader();
		initTable();
		JButton csv = initCsvBtn();
		header.add(csv);
		this.add(header, BorderLayout.PAGE_START);
		LOG.debug("End ArtistPanel");
	}

	private JPanel initHeader() {
		LOG.debug("Start initHeader");
		JPanel header = new JPanel();
		// Artist
		JPanel artistPanel = new JPanel();
		artistPanel.setPreferredSize(new Dimension(200, PanelUtils.PANEL_HEIGHT));
		JLabel artistLabel = new JLabel("Artiste : ");
		artist = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport.install((JComboBox<?>) artist.getInput(),
				GlazedLists.eventListOf(OngletPanel.getArtistList().toArray()));
		artistPanel.add(artistLabel);
		artistPanel.add(artist);
		header.add(artistPanel);
		// Publi
		JPanel publiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(publiPanel, 230, PanelUtils.PANEL_HEIGHT);
		JLabel publiLabel = PanelUtils.createJLabel("Année de publication : ", 200);
		publi = new MyInputText(JTextField.class, 75);
		publi.getInput().addFocusListener(PanelUtils.selectAll);
		searchRange = new JComboBox<String>(
				Arrays.asList(SearchRange.values()).stream().map(v -> v.getValue()).toArray(String[]::new));
		PanelUtils.setSize(searchRange, 45, PanelUtils.COMPONENT_HEIGHT);
		publiPanel.add(publiLabel);
		publiPanel.add(searchRange);
		publiPanel.add(publi);
		header.add(publiPanel);
		// Range
		JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(rangePanel, 300, PanelUtils.PANEL_HEIGHT);
		JLabel rangeLabel = new JLabel("Année(s) du classement : ");
		PanelUtils.setSize(rangeLabel, 180, PanelUtils.COMPONENT_HEIGHT);
		rangeB = new MyInputText(JTextField.class, 90);
		rangeE = new MyInputText(JTextField.class, 90);
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
		header.add(rangePanel);
		// Auteur
		JPanel auteurPanel = new JPanel();
		auteurPanel.setPreferredSize(new Dimension(200, PanelUtils.PANEL_HEIGHT));
		JLabel auteurLabel = new JLabel("Auteur : ");
		auteur = new MyInputText(JComboBox.class, 150);
		AutoCompleteSupport<Object> install = AutoCompleteSupport.install((JComboBox<?>) auteur.getInput(),
				GlazedLists.eventListOf(OngletPanel.getAuthorList().toArray()));
		install.setFilterMode(TextMatcherEditor.CONTAINS);
		auteurPanel.add(auteurLabel);
		auteurPanel.add(auteur);
		header.add(auteurPanel);
		// Categorie
		JPanel catPanel = new JPanel();
		catPanel.setPreferredSize(new Dimension(180, PanelUtils.PANEL_HEIGHT));
		JLabel catLabel = new JLabel("Catégorie : ");
		cat = new JComboCheckBox(
				Arrays.asList(Cat.values()).stream().map(c -> c.getCat()).collect(Collectors.toList()));
		cat.setPreferredSize(new Dimension(120, PanelUtils.COMPONENT_HEIGHT));
		catPanel.add(catLabel);
		catPanel.add(cat);
		header.add(catPanel);
		// Deleted
		JPanel deletedPanel = new JPanel();
		deletedPanel.setPreferredSize(new Dimension(90, PanelUtils.PANEL_HEIGHT));
		JLabel deletedLabel = new JLabel("Supprimés: ");
		deleted = new JCheckBox();
		deleted.setPreferredSize(new Dimension(50, PanelUtils.COMPONENT_HEIGHT));
		deleted.setHorizontalAlignment(SwingConstants.CENTER);
		deletedPanel.add(deletedLabel);
		deletedPanel.add(deleted);
		header.add(deletedPanel);
		// SEARCH
		search = PanelUtils.createJButton("Rechercher", 150, Constant.ICON_SEARCH);
		search.addActionListener((ActionEvent e) -> searchAction());
		header.add(search);
		// RESET
		reset = PanelUtils.createJButton("Réinitialiser", 150, Constant.ICON_ERASE);
		reset.addActionListener((ActionEvent e) -> resetAction());
		header.add(reset);
		LOG.debug("End initHeader");
		return header;
	}

	private void initTable() {
		LOG.debug("Start initTable");
		table = new JTable();
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(30);
		table.setFillsViewportHeight(true);
		table.getTableHeader().setResizingAllowed(true);
		table.setBackground(UIManager.getColor("Label.background"));
		table.setFont(UIManager.getFont("Label.font"));
		table.setBorder(UIManager.getBorder("Label.border"));
		model = new ArtistModel(new Object[0][title.length], title);
		table.setModel(model);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
			@Override
			public boolean isSortable(int column) {
				if (column != INDEX_LINE_NUMBER)
					return true;
				else
					return false;
			};
		};
		table.setRowSorter(sorter);
		table.getRowSorter().addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				List<? extends SortKey> sortKeys = ((RowSorter<?>) e.getSource()).getSortKeys();
				if (!sortKeys.isEmpty()) {
					if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
						// Store sorted column and order
						sortedColumn = sortKeys.get(0).getColumn();
						sortOrder = sortKeys.get(0).getSortOrder();
					}
					// Handling of line numbers
					for (int i = 0; i < table.getRowCount(); i++) {
						table.setValueAt(i + 1, i, INDEX_LINE_NUMBER);
					}
				}
			}
		});
		PanelUtils.colRenderer(table, false, null, null);

		updateArtistPanel();

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mouseClickAction(e);
			}
		});
		table.addKeyListener(new KeyListener() {
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

		this.add(new JScrollPane(table), BorderLayout.CENTER);
		LOG.debug("End initTable");
	}

	private JButton initCsvBtn() {
		LOG.debug("Start initCsvBtn");
		// CSV
		JButton csv = PanelUtils.createJButton("Télécharger la recherche en CSV", 220, Constant.ICON_DOWNLOAD);
		csv.addActionListener((ActionEvent e) -> {
			LOG.debug("Start Csv");
			List<String> c = Arrays.asList(artist.getText(), publi.getText(), rangeB.getText(), rangeE.getText(),
					auteur.getText(), cat.getSelectedItems()).stream().filter(s -> !"".equals(s))
					.collect(Collectors.toList());
			String criteres = StringUtils.join(c, " ");
			LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(title));
			csvHeader.add("Critères: " + criteres);
			String name = CsvFile.exportCsv("artist", PanelUtils.convertDataVectorToList(table), null,
					csvHeader.toArray(new String[title.length + 1]));
			try {
				FichierUtils.openFileInExcel(Optional.of(name));
			} catch (MyException e1) {
				LOG.error("Erreur de l'ouverture avec excel du fichier: " + name, e1);
			}
			LOG.debug("End Csv");
		});
		LOG.debug("End initCsvBtn");
		return csv;
	}

	/**
	 * Met à jour le panel artiste à l'aide du fichier final.
	 */
	public void updateArtistPanel() {
		LOG.debug("Start updateArtistPanel");
		if (!withArtist) {
			return;
		}
		if (updateArtistThread == null || !updateArtistThread.isAlive()) {
			startUpdateArtistThread();
		} else {
			interruptUpdateArtist(false);
			try {
				updateArtistThread.join();
			} catch (InterruptedException e) {
				LOG.error("", e);
				Thread.currentThread().interrupt();
			}
			startUpdateArtistThread();
		}
		LOG.debug("End updateArtistPanel");
	}

	/**
	 * Stops the thread that calculates data of artist panel.
	 * 
	 * @param deleteJsonFile if true, the json file containing data is deleted
	 */
	public void interruptUpdateArtist(boolean deleteJsonFile) {
		if (!withArtist) {
			return;
		}
		if (deleteJsonFile) {
			if (!new File(Constant.ARTIST_PANEL_RESULT_FILE).delete()) {
				LOG.warn(Constant.ARTIST_PANEL_RESULT_FILE + " n'a pas pu etre supprimé");
			}
		}
		updateArtistThread.interrupt();
	}

	private void startUpdateArtistThread() {
		updateArtistThread = new Thread(() -> {
			LOG.debug("Start ThreadUpdateArtist");
			if (!new File(Constant.ARTIST_PANEL_RESULT_FILE).exists()) {
				data = CompositionUtils.groupCompositionByArtist(ImportXML.importXML(Constant.getFinalFilePath()));
				FichierUtils.exportJsonInFile(data, Constant.ARTIST_PANEL_RESULT_FILE);
			} else {
				try {
					data = MiscUtils
							.readValueAsMapOfList(FichierUtils.readFirstLine(Constant.ARTIST_PANEL_RESULT_FILE).get());
				} catch (IOException e1) {
					LOG.error("Error when reading artist json file", e1);
				}
			}
			SwingUtilities.invokeLater(() -> {
				// Called when data are finally calculated
				resetAction();
				searchResult = new HashMap<>(); // the data displays in the table
				for (Map.Entry<String, List<Composition>> entry : data.entrySet()) {
					for (Composition c : entry.getValue()) {
						// Filters on whether show deleted compositions or not
						if (deleted.isSelected() || !c.isDeleted()) {
							Composition newCompo = new Composition(c);
							newCompo.setFiles(c.getFiles());
							if (!searchResult.containsKey(entry.getKey())) {
								searchResult.put(entry.getKey(), new ArrayList<>(Arrays.asList(newCompo)));
							} else {
								searchResult.get(entry.getKey()).add(newCompo);
							}
						}
					}
				}
				updateTable(searchResult);
			});
			LOG.debug("End ThreadUpdateArtist");
		});
		updateArtistThread.setDaemon(true);
		updateArtistThread.setPriority(Thread.MIN_PRIORITY);
		updateArtistThread.start();
	}

	private void updateTable(Map<String, List<Composition>> donnee) {
		LOG.debug("Start updateTable");
		// Updates table data
		model.setRowCount(0);
		model.setDataVector(CompositionUtils.convertArtistPanelResultToVector(donnee, true),
				new Vector<>(Arrays.asList(title)));
		// Applies stored sorting
		if (sortedColumn == null) {
			sortedColumn = INDEX_NB_TOTAL;
			sortOrder = SortOrder.DESCENDING;
		}
		table.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedColumn, sortOrder)));
		((TableRowSorter<?>) table.getRowSorter()).setComparator(INDEX_DELETED, MiscUtils.comparePercentage);
		((TableRowSorter<?>) table.getRowSorter()).setComparator(INDEX_SCORE_DELETED, MiscUtils.comparePercentage);
		// Fills column "line number"
		for (int i = 0; i < table.getRowCount(); i++) {
			table.setValueAt(i + 1, i, INDEX_LINE_NUMBER);
		}
		if (!deleted.isSelected()) {
			table.removeColumn(table.getColumnModel().getColumn(INDEX_DELETED));
			table.removeColumn(table.getColumnModel().getColumn(INDEX_SCORE_DELETED-1));
		}
		PanelUtils.colRenderer(table, true, null, null);
		table.getColumnModel().getColumn(INDEX_LINE_NUMBER).setMinWidth(40);
		table.getColumnModel().getColumn(INDEX_LINE_NUMBER).setMaxWidth(40);
		// Update GUI
		model.fireTableDataChanged();
		table.repaint();
		selectedRow = -1;
		LOG.debug("End updateTable");
	}

	private void mouseClickAction(MouseEvent e) {
		Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow(e);
		if (!selectedRow.isPresent()) {
			return;
		}
		LOG.debug(selectedRow);
		if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
			LOG.debug("Start artist mouse");
			// Affiche tous les fichiers de l'artiste double cliqué
			Optional<String> key = CompositionUtils.findArtistKey(searchResult, new JaroWinklerDistance(),
					selectedRow.get().get(INDEX_ARTIST));
			if (!key.isPresent()) {
				LOG.error("Error when searching: " + selectedRow.get().get(INDEX_ARTIST) + " in data table");
			} else {
				DialogFileTable pop = new DialogFileTable(null, "Fichier", true, searchResult.get(key.get()),
						new Dimension(1500, 600), DialogFileTable.INDEX_TITLE);
				pop.showDialogFileTable();
			}
			LOG.debug("End artist mouse");
		} else if (SwingUtilities.isRightMouseButton(e)) {
			LOG.debug("Start artist right mouse");
			// Copie dans le presse papier le nom de l'artiste
			StringSelection selection = new StringSelection(selectedRow.get().get(INDEX_ARTIST));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			LOG.debug("End artist right mouse");
		}
	}

	private void searchAction() {
		LOG.debug("Start search");
		JaroWinklerDistance jaro = new JaroWinklerDistance();
		if (data != null && !data.isEmpty()) {
			searchResult = new HashMap<>();
			for (Map.Entry<String, List<Composition>> entry : data.entrySet()) {
				for (Composition c : entry.getValue()) {
					if ((!deleted.isSelected() && c.isDeleted())
							|| (StringUtils.isNotBlank(artist.getText()) && !SearchUtils.compareString(artist.getText(),
									c.getArtist(), SearchMethod.CONTAINS, jaro))) {
						continue;
					}
					List<Fichier> files = c.getFiles().stream().filter(f -> {
						return SearchUtils.filterFichier(SearchMethod.WHOLE_WORD, jaro, publi.getText(),
								(String) searchRange.getSelectedItem(), null, auteur.getText(), cat.getSelectedItems(),
								rangeB.getText(), rangeE.getText(), null, null, f);
					}).collect(Collectors.toList());
					if (!files.isEmpty()) {
						Composition newCompo = new Composition(c);
						newCompo.setFiles(files);
						if (!searchResult.containsKey(entry.getKey())) {
							searchResult.put(entry.getKey(), new ArrayList<>(Arrays.asList(newCompo)));
						} else {
							searchResult.get(entry.getKey()).add(newCompo);
						}
					}
				}
			}
			updateTable(searchResult);
		}
		LOG.debug("End search");
	}

	private void resetAction() {
		LOG.debug("Start resetAction");
		artist.setText("");
		rangeB.setText("");
		rangeE.setText("");
		auteur.setText("");
		publi.setText("");
		cat.clearSelection();
		deleted.setSelected(false);
		LOG.debug("End resetAction");
	}

	public JTable getTable() {
		return table;
	}

	public JButton getSearch() {
		return search;
	}

}
