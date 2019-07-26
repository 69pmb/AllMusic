/**
 * 
 */
package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.model.ArtistModel;

/**
 * L'onglet Artiste, classement des artistes les plus cités.
 * 
 * @author pmbroca
 */
public class ArtistPanel extends JPanel {

	private static final long serialVersionUID = 2593372709628283573L;

	private static final Logger LOG = LogManager.getLogger(ArtistPanel.class);
	public static final int INDEX_LINE_NUMBER = 0;
	public static final int INDEX_ARTIST = 1;
	public static final int INDEX_NB_TOTAL = 2;
	public static final int INDEX_DELETED = 5;
	public static final int INDEX_SCORE_TOTAL = 6;
	public static final int INDEX_SCORE_SONG = 7;
	public static final int INDEX_SCORE_ALBUM = 8;
	public static final int INDEX_SCORE_DELETED = 9;

	// Search components
	private MyInputText artist;
	private JComboBoxInput<String> publi;
	private MyInputRange range;
	private MyInputText auteur;
	private JComboCheckBox cat;
	private JButton search;
	private JCheckBox deleted;

	private MyTable table;
	private Map<String, List<Composition>> data;
	private Map<String, List<Composition>> searchResult;

	private static final String[] title = { "#", "Artiste", "Nombre d'Occurrences", "Album", "Chanson", "Supprimés",
			"Score Total", "Score Album", "Score Chanson", "Score Supprimés" };

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
		if (withArtist) {
			this.setLayout(new BorderLayout());
			JPanel header = initHeader();
			initTable();
			JButton csv = initCsvBtn();
			header.add(csv);
			this.add(header, BorderLayout.PAGE_START);
		}
		LOG.debug("End ArtistPanel");
	}

	@SuppressWarnings("unchecked")
	private JPanel initHeader() {
		LOG.debug("Start initHeader");
		JPanel header = new JPanel();
		// Artist
		artist = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(header)
				.withValues(OngletPanel.getArtistList()).withLabel("Artiste : ").withPanelWidth(200)
				.withComponentWidth(150).withLabelWidth(150).build();
		// Publi
		publi = (JComboBoxInput<String>) new ComponentBuilder<String>(JComboBoxInput.class).withParent(header)
				.withValues(MiscUtils.getEnumValues(SearchRange.values(), SearchRange::getValue))
				.withLabel("Année de publication : ").withPanelWidth(230).withComponentWidth(75).withLabelWidth(200)
				.build();
		// Range
		range = (MyInputRange) new ComponentBuilder<String>(MyInputRange.class).withParent(header)
				.withLabel("Année(s) du classement : ").withPanelWidth(300).withComponentWidth(180).withLabelWidth(180)
				.withFlowLayout(true).build();
		// Auteur
		auteur = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(header)
				.withValues(OngletPanel.getAuthorList()).withLabel("Auteur : ").withPanelWidth(200)
				.withFilterContains(true).withComponentWidth(150).withLabelWidth(150).build();
		// Categorie
		cat = (JComboCheckBox) new ComponentBuilder<String>(JComboCheckBox.class).withParent(header)
				.withValues(MiscUtils.getEnumValues(Cat.values(), Cat::getCat))
				.withLabel("Catégorie : ").withPanelWidth(180).withComponentWidth(120).withLabelWidth(150).build();
		// Deleted
		deleted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(header)
				.withLabel("Supprimés : ").withPanelWidth(90).withComponentWidth(50).withLabelWidth(60).build();
		// SEARCH
		search = ComponentBuilder.buildJButton("Rechercher", 150, Constant.ICON_SEARCH);
		search.addActionListener((ActionEvent e) -> searchAction());
		header.add(search);
		// RESET
		JButton reset = ComponentBuilder.buildJButton("Réinitialiser", 150, Constant.ICON_ERASE);
		reset.addActionListener((ActionEvent e) -> resetAction());
		header.add(reset);
		LOG.debug("End initHeader");
		return header;
	}

	private void initTable() {
		LOG.debug("Start initTable");

		try {
			table = new TableBuilder().withModelAndData(null, title, ArtistModel.class)
					.withDefaultRowSorterListener(INDEX_LINE_NUMBER).withMouseClickAction(e -> {
						Optional<Vector<String>> row = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
						if (!row.isPresent()) {
							return;
						}
						LOG.debug(row);
						if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
							LOG.debug("Start artist mouse");
							// Affiche tous les fichiers de l'artiste double cliqué
							Optional<String> key = CompositionUtils.findArtistKey(searchResult,
									new JaroWinklerDistance(), row.get().get(INDEX_ARTIST));
							if (!key.isPresent()) {
								LOG.error("Error when searching: " + row.get().get(INDEX_ARTIST) + " in data table");
							} else {
								DialogFileTable pop = new DialogFileTable(null, "Fichier", true,
										searchResult.get(key.get()), 600, DialogFileTable.INDEX_TITLE);
								pop.showDialogFileTable();
							}
							LOG.debug("End artist mouse");
						} else if (SwingUtilities.isRightMouseButton(e)) {
							LOG.debug("Start artist right mouse");
							// Copie dans le presse papier le nom de l'artiste
							MiscUtils.clipBoardAction(row.get().get(INDEX_ARTIST));
							LOG.debug("End artist right mouse");
						}
					}).withKeyListener().build();
			PanelUtils.colRenderer(table.getTable(), false, null, null, null, null, null, null, null);
			updateArtistPanel();
			this.add(new JScrollPane(table.getTable()), BorderLayout.CENTER);
		} catch (MyException e1) {
			LOG.error("An error occured when init artist table", e1);
			return;
		}

		LOG.debug("End initTable");
	}

	private JButton initCsvBtn() {
		LOG.debug("Start initCsvBtn");
		// CSV
		JButton csv = ComponentBuilder.buildJButton("Télécharger la recherche en CSV", 220, Constant.ICON_DOWNLOAD);
		csv.addActionListener((ActionEvent e) -> {
			LOG.debug("Start Csv");
			List<String> c = Arrays
					.asList(artist.getText(), publi.getInput().getText(), range.getFirst().getText(),
							range.getSecond().getText(), auteur.getText(), cat.getSelectedItems())
					.stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
			String criteres = StringUtils.join(c, " ");
			LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(title));
			csvHeader.add("Critères: " + criteres);
			String name = CsvFile.exportCsv("artist", PanelUtils.convertDataVectorToList(table.getTable()), null,
					csvHeader.toArray(new String[title.length + 1]));
			try {
				FichierUtils.openFileInExcel(name);
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
		if (deleteJsonFile && new File(Constant.ARTIST_PANEL_RESULT_FILE).exists()) {
			try {
				Files.delete(Paths.get(Constant.ARTIST_PANEL_RESULT_FILE));
			} catch (IOException e) {
				LOG.warn(Constant.ARTIST_PANEL_RESULT_FILE + " n'a pas pu etre supprimé", e);
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
				fillSearchResultAndFilterDeleted();
				updateTable(searchResult);
			});
			LOG.debug("End ThreadUpdateArtist");
		});
		updateArtistThread.setDaemon(true);
		updateArtistThread.setPriority(Thread.MIN_PRIORITY);
		updateArtistThread.start();
	}

	/**
	 * Fills search result (the displayed data) with imported or calculated data.
	 * Filters on whether show deleted compositions or not.
	 */
	private void fillSearchResultAndFilterDeleted() {
		searchResult = new HashMap<>(); // the data displays in the table
		for (Map.Entry<String, List<Composition>> entry : data.entrySet()) {
			for (Composition c : entry.getValue()) {
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
	}

	private void updateTable(Map<String, List<Composition>> donnee) {
		LOG.debug("Start updateTable");
		// Updates table data
		table.getModel().setRowCount(0);
		table.getModel().setDataVector(CompositionUtils.convertArtistPanelResultToVector(donnee, true),
				new Vector<>(Arrays.asList(title)));
		// Applies stored sorting
		if (table.getSortedColumn() == null) {
			table.setSortedColumn(INDEX_NB_TOTAL);
			table.setSortOrder(SortOrder.DESCENDING);
		}
		table.getRowSorter().setSortKeys(
				Collections.singletonList(new RowSorter.SortKey(table.getSortedColumn(), table.getSortOrder())));
		((TableRowSorter<?>) table.getRowSorter()).setComparator(INDEX_DELETED, MiscUtils.comparePercentage);
		((TableRowSorter<?>) table.getRowSorter()).setComparator(INDEX_SCORE_DELETED, MiscUtils.comparePercentage);
		// Fills column "line number"
		for (int i = 0; i < table.getRowCount(); i++) {
			table.setValueAt(i + 1, i, INDEX_LINE_NUMBER);
		}
		if (!deleted.isSelected()) {
			table.removeColumn(table.getColumnModel().getColumn(INDEX_DELETED));
			table.removeColumn(table.getColumnModel().getColumn(INDEX_SCORE_DELETED - 1));
		}
		PanelUtils.colRenderer(table.getTable(), true, null, null, null, null, null, null, null);
		table.getColumnModel().getColumn(INDEX_LINE_NUMBER).setMinWidth(40);
		table.getColumnModel().getColumn(INDEX_LINE_NUMBER).setMaxWidth(40);
		// Update GUI
		table.getModel().fireTableDataChanged();
		table.getTable().repaint();
		table.setSelectedRow(-1);
		LOG.debug("End updateTable");
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
					Map<String, String> criteria = new HashMap<>();
					criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, publi.getInput().getText());
					criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE,
							(String) publi.getComboBox().getSelectedItem());
					criteria.put(SearchUtils.CRITERIA_AUTHOR, auteur.getText());
					criteria.put(SearchUtils.CRITERIA_CAT, cat.getSelectedItems());
					criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, range.getFirst().getText());
					criteria.put(SearchUtils.CRITERIA_DATE_END, range.getSecond().getText());
					List<Fichier> files = c.getFiles().stream()
							.filter(f -> SearchUtils.filterFichier(SearchMethod.WHOLE_WORD, jaro, criteria, f))
							.collect(Collectors.toList());
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
		range.getFirst().setText("");
		range.getSecond().setText("");
		auteur.setText("");
		if (publi.getInput() != null) {
			publi.getInput().setText("");
		}
		cat.clearSelection();
		deleted.setSelected(false);
		LOG.debug("End resetAction");
	}

	public JTable getTable() {
		return table.getTable();
	}

	public JButton getSearch() {
		return search;
	}

}
