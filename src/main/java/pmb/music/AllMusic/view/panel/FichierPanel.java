package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.swing.FontIcon;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.exception.MinorException;
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
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.ScoreUtils;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.ModificationComposition;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.dialog.ModifyFichierDialog;
import pmb.music.AllMusic.view.model.CompoFichierPanelModel;
import pmb.music.AllMusic.view.model.FichierPanelModel;
import pmb.music.AllMusic.view.popup.CompositionPopupMenu;
import pmb.music.AllMusic.view.popup.FichierPopupMenu;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs compositions. Created by PBR on 29 mai 2018.
 */
public class FichierPanel extends JPanel implements ModificationComposition {
	private static final long serialVersionUID = 8581952935884211032L;

	private static final Logger LOG = LogManager.getLogger(FichierPanel.class);

	private static final int MIN_HEIGHT_TABLE = 41;
	private static final int MAX_HEIGHT_TABLE = 82;

	private static final ColumnIndex fichierIndex = new ColumnIndex().put(Index.LINE_NUMBER, 0)
			.put(Index.AUTHOR, 1)
			.put(Index.FILE_NAME, 2)
			.put(Index.TYPE, 3)
			.put(Index.PUBLISH, 4)
			.put(Index.CAT, 5)
			.put(Index.RANGE, 6)
			.put(Index.PERCENT_DELETED, 7)
			.put(Index.CREATE_DATE, 8)
			.put(Index.SCORE, 9)
			.put(Index.SCORE_DELETED, 10)
			.put(Index.FILE_SIZE, 11)
			.put(Index.SORTED, 12);

	private static final ColumnIndex compositionIndex = new ColumnIndex().put(Index.LINE_NUMBER, 0)
			.put(Index.ARTIST, 1)
			.put(Index.TITLE, 2)
			.put(Index.TYPE, 3)
			.put(Index.RANK, 4)
			.put(Index.FILE_SIZE, 5)
			.put(Index.SCORE, 6)
			.put(Index.DECILE, 7)
			.put(Index.SELECTED, 8)
			.put(Index.DELETED, 9)
			.put(Index.UUID, 10);

	// Search components
	private MyInputText auteur;
	private MyInputText filename;
	private JComboBoxInput<String> publi;
	private MyInputRange range;
	private JComboCheckBox cat;
	private JComboCheckBox type;
	private JCheckBox sorted;
	private JCheckBox deleted;
	private JButton search;
	private JLabel resultLabel;

	// Fichier componants
	private JPanel filePanel;
	private MyTable tableFiles;
	private boolean showFichierTable = true;
	private String selectedFichierName = "";
	private Map<Fichier, List<Composition>> data;
	private Map<Fichier, List<Composition>> searchResult;

	// Composition componants
	private JPanel compoPanel;
	private MyTable tableCompo;
	private List<Composition> compositionList;
	private boolean showCompoTable = true;

	private Dimension parentSize;

	private static final String[] headerFiles = { "#", "Auteur", "Nom du fichier", "Type", "Publication", "Categorie",
			"Dates", "Supprimés", "Création", "Score", "Score Supprimés", "Taille", "Classé" };
	private static final String[] headerCompo = { "#", "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers",
			"Score", "", "", "", "" };

	/**
	 * Constructeur de {@link FichierPanel}.
	 */
	public FichierPanel() {
		super();
		LOG.debug("Start FichierPanel");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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

	@SuppressWarnings("unchecked")
	private void initInputs(JPanel header) {
		JPanel inputs = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		// Auteur
		auteur = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(inputs)
				.withValues(OngletPanel.getAuthorList()).withLabel("Auteur : ").withPanelWidth(190)
				.withFilterContains(true).withFlowLayout(true).withComponentWidth(150).withLabelWidth(140).build();
		// Nom du fichier
		filename = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(inputs)
				.withLabel("Nom du fichier : ").withFlowLayout(true).withPanelWidth(240).withComponentWidth(180)
				.withLabelWidth(190).build();
		// Publi
		publi = (JComboBoxInput<String>) new ComponentBuilder<String>(JComboBoxInput.class).withParent(inputs)
				.withValues(MiscUtils.getEnumValues(SearchRange.values(), SearchRange::getValue))
				.withLabel("Année de publication : ").withPanelWidth(250).withComponentWidth(100).withLabelWidth(210)
				.build();
		// Range
		range = (MyInputRange) new ComponentBuilder<String>(MyInputRange.class).withParent(inputs)
				.withLabel("Année(s) du classement : ").withPanelWidth(270).withComponentWidth(140).withLabelWidth(180)
				.withFlowLayout(true).build();
		// Type
		type = (JComboCheckBox) new ComponentBuilder<String>(JComboCheckBox.class).withParent(inputs)
				.withValues(MiscUtils.getEnumValues(RecordType.values(), RecordType::getRecordType))
				.withFlowLayout(true).withLabel("Type : ").withPanelWidth(200).withComponentWidth(150)
				.withLabelWidth(150).build();
		// Categorie
		cat = (JComboCheckBox) new ComponentBuilder<String>(JComboCheckBox.class).withParent(inputs)
				.withValues(MiscUtils.getEnumValues(Cat.values(), Cat::getCat))
				.withFlowLayout(true).withLabel("Catégorie : ").withPanelWidth(200).withComponentWidth(120)
				.withLabelWidth(150).build();
		// Deleted
		deleted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(inputs)
				.withLabel("Supprimés : ").withFlowLayout(true).withInitialValue(true).withPanelWidth(100)
				.withComponentWidth(80).withLabelWidth(150).build();
		// Sorted
		sorted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(inputs).withLabel("Classé : ")
				.withFlowLayout(true).withPanelWidth(100).withComponentWidth(80).withLabelWidth(150).build();
		header.add(inputs);
	}

	@SuppressWarnings("unchecked")
	private void initButtons(JPanel header) {
		JPanel buttons = new JPanel(new GridLayout(1, 7));
		// SEARCH
		search = ComponentBuilder.buildJButton("Rechercher", 120, Constant.ICON_SEARCH);
		search.addActionListener((ActionEvent e) -> searchAction());
		buttons.add(search);
		// RESET
		JButton reset = ComponentBuilder.buildJButton("Réinitialiser", 120, Constant.ICON_ERASE);
		reset.addActionListener((ActionEvent e) -> resetAction());
		buttons.add(reset);
		// hideFileList
		JButton hideFileList = ComponentBuilder.buildJButton("Cacher la liste des fichiers", 180, Constant.ICON_HIDE);
		hideFileList.addActionListener((ActionEvent e) -> {
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
		});
		buttons.add(hideFileList);
		// hideCompoList
		JButton hideCompoList = ComponentBuilder.buildJButton("Cacher la liste des compositions", 200,
				Constant.ICON_HIDE);
		hideCompoList.addActionListener((ActionEvent e) -> {
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
		});
		buttons.add(hideCompoList);
		// Delete Btn
		JButton delete = ComponentBuilder.buildJButton("<html>Supprimer les compositions sélectionnées</html>", 200,
				Constant.ICON_DELETE);
		delete.addActionListener((ActionEvent e) -> {
			List<Object> selected = tableCompo.getModel().getSelected();
			try {
				PanelUtils.deleteCompositionAction(compositionList, selected.stream().map(v -> MiscUtils.stringToUuids(((Vector<String>) v).get(compositionIndex.get(Index.UUID))).get(0)).collect(Collectors.toList()));
				updateCompoTable(selectedFichierName, false);
				resultLabel.setText(selected.size() + " élément(s) supprimé(s)");
			} catch (MajorException e1) {
				LOG.error("Error when deleting compositions in Fichier result", e1);
				resultLabel.setText("<html>" + e1.getMessage() + "</html>");
			}
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
			String name = CsvFile.exportCsv("files", PanelUtils.convertDataVectorToList(tableFiles.getTable()), null,
					csvHeader.toArray(new String[headerFiles.length + 1]));
			try {
				FilesUtils.openFileInExcel(name);
			} catch (MajorException e1) {
				LOG.error("Erreur de l'ouverture avec excel du fichier: {}", name, e1);
			}
		});
		buttons.add(csv);
		// Label pour afficher les resultats
		resultLabel = (JLabel) new ComponentBuilder<String>(JLabel.class).withParent(buttons).withLabel("")
				.withLabelWidth(200).withColor(new Color(8, 187, 81)).withFontSize(16).build();
		header.add(buttons);
	}

	private void initFichierTable() {
		filePanel = new JPanel(new BorderLayout());

		try {
			tableFiles = new TableBuilder().withModelAndData(null, headerFiles, FichierPanelModel.class)
					.withDefaultRowSorterListener(fichierIndex.get(Index.LINE_NUMBER)).withMouseClickAction(e -> {
						LOG.debug("Start mouseActionForFileTable");
						Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(),
								e.getPoint());
						tableFiles.getPopupMenu().initDataAndPosition(e, selectedRow.orElse(null));
						if (!selectedRow.isPresent()) {
							return;
						}
						selectedFichierName = selectedRow.get().get(fichierIndex.get(Index.FILE_NAME));
						if (e.getClickCount() == 1 && (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
							LOG.debug("Start left mouse, open");
							// Affiche les compositions du fichier sélectionné
							compositionList = findFichierInMap(selectedRow.get().get(fichierIndex.get(Index.FILE_NAME))).get()
									.getValue();
							if (!deleted.isSelected()) {
								compositionList = compositionList.stream().filter(c -> !c.isDeleted())
										.collect(Collectors.toList());
							}
							updateCompoTable(selectedFichierName, true);
							LOG.debug("End left mouse, open");
						} else if (SwingUtilities.isRightMouseButton(e)) {
							tableFiles.getPopupMenu().show(e);
						}
						LOG.debug("End mouseActionForFileTable");
					}).withPopupMenu(new FichierPopupMenu(fichierIndex)).withKeyListener()
					.build();
		} catch (MajorException e1) {
			LOG.error("An error occured when init fichier table", e1);
			resultLabel.setText(e1.getMessage());
			return;
		}

		filePanel.add(new JScrollPane(tableFiles.getTable()), BorderLayout.CENTER);
		this.add(filePanel);
		setTableSize(filePanel, MIN_HEIGHT_TABLE);
	}

	private void initCompoTable() {
		compoPanel = new JPanel(new BorderLayout());

		try {
			tableCompo = new TableBuilder().withModelAndData(null, headerCompo, CompoFichierPanelModel.class)
					.withRowSorterListenerDelete(compositionIndex)
					.withMouseClickAction(e -> {
						Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRow((JTable) e.getSource(),
								e.getPoint());
						tableCompo.getPopupMenu().initDataAndPosition(e, selectedRow.orElse(null));
						if (!selectedRow.isPresent()) {
							return;
						}
						if (e.getClickCount() == 2 && (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
							LOG.debug("Start left mouse");
							// Ouvre une popup pour afficher les fichiers de la
							// composition sélectionnée
							DialogFileTable pop = new DialogFileTable(null, "Fichier", true,
									CompositionUtils.findByUuid(compositionList, MiscUtils.stringToUuids(selectedRow.get().get(compositionIndex.get(Index.UUID))))
									.map(c -> new LinkedList<>(Arrays.asList(c))).orElse(new LinkedList<>()),
									400, new RowSorter.SortKey(DialogFileTable.getIndex().get(Index.SCORE), SortOrder.DESCENDING));
							pop.show();
							LOG.debug("End left mouse");
						} else if (SwingUtilities.isRightMouseButton(e)) {
							tableCompo.getPopupMenu().show(e);
						}
					}).withPopupMenu(new CompositionPopupMenu(this.getClass(), null, compositionIndex))
					.withKeyListener().build();
		} catch (MajorException e1) {
			LOG.error("An error occured when init composition table", e1);
			resultLabel.setText(e1.getMessage());
			return;
		}

		compoPanel.add(new JScrollPane(tableCompo.getTable()), BorderLayout.CENTER);
		this.add(compoPanel);
		setTableSize(compoPanel, MIN_HEIGHT_TABLE);
	}

	private void initData() {
		LOG.debug("Start initData");
		new Thread(() -> {
			data = new ConcurrentHashMap<>();
			ImportXML.importXML(Constant.getFinalFilePath()).parallelStream()
			.forEach(c -> c.getFiles().parallelStream().forEach(f -> {
				Optional<Entry<Fichier, List<Composition>>> entry = findFichierInMap(f.getFileName());
				Composition copy = new Composition();
				CompositionUtils.copy(c, copy);
				if (entry.isPresent()) {
					data.get(entry.get().getKey()).add(copy);
				} else {
					data.put(f, new LinkedList<>(Arrays.asList(copy)));
				}
			}));
			copyDataInSearchResult();
			LOG.debug("Data calculated");
		}).start();
		LOG.debug("End initData");
	}

	private void copyDataInSearchResult() {
		searchResult = new ConcurrentHashMap<>(); // the map displays in the table
		data.entrySet().stream().forEach(e -> searchResult.put(e.getKey(),
				e.getValue().stream().map(Composition::new).collect(Collectors.toList())));
	}

	private Optional<Entry<Fichier, List<Composition>>> findFichierInMap(String fileName) {
		return data.entrySet().stream()
				.filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey().getFileName(), fileName)).findFirst();
	}

	/**
	 * Recalculates data for given files from given list.
	 *
	 * @param list new data
	 * @param files the files to reprocess
	 */
	public void reprocessSpecificFichier(List<Composition> list, List<Fichier> files) {
		files.parallelStream().forEach(f -> setCompoListFromData(f, list.parallelStream()
				.filter(c -> c.getFiles().stream().map(Fichier::getFileName).anyMatch(name -> StringUtils.equalsIgnoreCase(name, f.getFileName())))
				.collect(Collectors.toList())));
	}

	/**
	 * Launchs a dialog to modify the selected {@link Fichier}.
	 *
	 * @param selected the selected row representing a fichier
	 * @throws MajorException if file can't be found
	 */
	public void modifyFichierAction(Vector<String> selected) throws MajorException {
		LOG.debug("Start modifyFichierAction");
		resultLabel.setText("");
		String fileName = selected.get(fichierIndex.get(Index.FILE_NAME));
		// Index du fichier dans le tableau
		Fichier key = findFichierInMap(fileName).map(Entry::getKey).orElseThrow(() -> new MajorException("Can't find Fichier in map: " + fileName));
		// Lancement de la popup de modification
		ModifyFichierDialog md = new ModifyFichierDialog(null, "Modifier un fichier", true, selected);
		md.show();
		Vector<String> newFichier;
		if (md.getSendData()) {
			// On recupère le fichier si il a bien été modifié
			LOG.debug("Fichier modifiée");
			newFichier = md.getFichier();
		} else {
			LOG.debug("Aucune modification");
			return;
		}
		// Retire les caractères interdits pour windows
		if (Arrays.asList(Constant.getForbiddenCharactersFilename()).stream()
				.anyMatch(s -> newFichier.get(fichierIndex.get(Index.FILE_NAME)).contains(s))) {
			Arrays.asList(Constant.getForbiddenCharactersFilename()).stream().forEach(
					s -> newFichier.set(fichierIndex.get(Index.FILE_NAME), newFichier.get(fichierIndex.get(Index.FILE_NAME)).replaceAll(s, "")));
		}
		Fichier modifiedFichier = null;
		try {
			// Modification du fichier
			modifiedFichier = FichierUtils.modifyFichier(fileName, newFichier.get(fichierIndex.get(Index.FILE_NAME)),
					newFichier.get(fichierIndex.get(Index.PUBLISH)), newFichier.get(fichierIndex.get(Index.RANGE)),
					newFichier.get(fichierIndex.get(Index.CAT)), newFichier.get(fichierIndex.get(Index.FILE_SIZE)), newFichier.get(fichierIndex.get(Index.SORTED)));
		} catch (MajorException e) {
			String log = "Erreur pendant FichierUtils.modifyFichier";
			LOG.error(log, e);
			resultLabel.setText(log + e);
			return;
		}
		// Mise à jour du JTable
		List<Composition> list = data.get(key);
		list.stream().forEach(FichierUtils.modifyOneFichier(fileName, newFichier.get(fichierIndex.get(Index.FILE_NAME)),
				newFichier.get(fichierIndex.get(Index.PUBLISH)), newFichier.get(fichierIndex.get(Index.RANGE)), newFichier.get(fichierIndex.get(Index.CAT)),
				newFichier.get(fichierIndex.get(Index.FILE_SIZE)), newFichier.get(fichierIndex.get(Index.SORTED))));
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
	 * @throws MajorException if edition fails
	 */
	@Override
	public void modifyCompositionAction(Vector<String> selected) throws MajorException {
		LOG.debug("Start modifyCompositionAction");
		compositionList = PanelUtils.editCompositionAction(selected, compositionList, compositionIndex);
		updateCompoTable(selectedFichierName, false);
		LOG.debug("End modifyCompositionAction");
	}

	@Override
	public void splitCompositionAction(Vector<Object> selected) throws MajorException {
		LOG.debug("Start splitCompositionAction");
		compositionList = PanelUtils.splitCompositionAction(selected, compositionList, FichierPanel.getCompositionindex());
		updateCompoTable(selectedFichierName, false);
		LOG.debug("End splitCompositionAction");
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
			Optional.of(criteria.entrySet().stream().filter(e -> StringUtils.isNotBlank(e.getValue()))
					.map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ")))
			.ifPresent(crit -> LOG.debug("Criteria: {}", crit));
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
		tableFiles.getModel().setRowCount(0);
		Vector<Vector<Object>> dataVector = FichierUtils.convertCompositionListToFichierVector(Arrays.asList(c), false,
				true);
		// Calculates score by getting the average of the score of each compositions
		// Calculates the percentage of deleted composition by the score
		for (int i = 0 ; i < dataVector.size() ; i++) {
			Vector<Object> vector = dataVector.get(i);
			findFichierInMap((String) vector.get(fichierIndex.get(Index.FILE_NAME))).ifPresentOrElse(entry -> {
				LongSummaryStatistics score = entry.getValue().parallelStream()
						.map(compo -> ScoreUtils.getCompositionScore(
								OngletPanel.getScore().getLogMax(compo.getRecordType()),
								OngletPanel.getScore().getDoubleMedian(compo.getRecordType()), compo))
						.mapToLong(x -> x).summaryStatistics();
				vector.add(fichierIndex.get(Index.SCORE), Math.round(score.getAverage()));
				long scoreDeleted = entry.getValue().parallelStream().filter(Composition::isDeleted)
						.map(compo -> ScoreUtils.getCompositionScore(
								OngletPanel.getScore().getLogMax(compo.getRecordType()),
								OngletPanel.getScore().getDoubleMedian(compo.getRecordType()), compo))
						.mapToLong(x -> x).sum();
				vector.add(fichierIndex.get(Index.SCORE_DELETED), Math.round(100 * (double) scoreDeleted / score.getSum()) + " %");
			}, ()->  {
				LOG.warn("Entry not found ! ");
				vector.add(fichierIndex.get(Index.SCORE), 0);
			});
		}
		tableFiles.getModel().setDataVector(dataVector, new Vector<>(Arrays.asList(headerFiles)));
		PanelUtils.colRenderer(tableFiles.getTable(), true, fichierIndex);
		if (tableFiles.getSortedColumn() == null) {
			tableFiles.setSortedColumn(fichierIndex.get(Index.FILE_NAME));
			tableFiles.setSortOrder(SortOrder.ASCENDING);
		}
		tableFiles.getRowSorter().setSortKeys(Collections
				.singletonList(new RowSorter.SortKey(tableFiles.getSortedColumn(), tableFiles.getSortOrder())));
		((TableRowSorter<?>) tableFiles.getRowSorter()).setComparator(fichierIndex.get(Index.PERCENT_DELETED),
				MiscUtils.comparePercentage);
		((TableRowSorter<?>) tableFiles.getRowSorter()).setComparator(fichierIndex.get(Index.SCORE_DELETED),
				MiscUtils.comparePercentage);
		for (int i = 0 ; i < tableFiles.getRowCount() ; i++) {
			tableFiles.setValueAt(i + 1, i, fichierIndex.get(Index.LINE_NUMBER));
		}
		tableFiles.getColumnModel().getColumn(fichierIndex.get(Index.LINE_NUMBER)).setMinWidth(30);
		tableFiles.getColumnModel().getColumn(fichierIndex.get(Index.LINE_NUMBER)).setMaxWidth(30);
		tableFiles.setSelectedRow(-1);
		tableFiles.getModel().fireTableDataChanged();
		tableFiles.getTable().repaint();
		compositionList = new ArrayList<>();
		updateCompoTable(null, true);
		LOG.debug("Start updateFileTable");
	}

	/**
	 * Met à jour le tableau des compositions.
	 *
	 * @param scrollTop {@code boolean} true scroll to the top of the table
	 *
	 * @param {@link String} selectedFile le fichier selectionné
	 */
	private void updateCompoTable(String selectedFile, boolean scrollTop) {
		LOG.debug("Start updateCompoTable");
		tableCompo.getModel().setRowCount(0);
		if (selectedFile != null && !compositionList.isEmpty()) {
			tableCompo.getModel().setDataVector(
					CompositionUtils.convertCompositionListToVector(compositionList, selectedFile, true, true, true, true, true),
					new Vector<>(Arrays.asList(headerCompo)));
		} else {
			tableCompo.getModel().setDataVector(new Vector<Vector<Object>>(), new Vector<>(Arrays.asList(headerCompo)));
		}
		PanelUtils.colRenderer(tableCompo.getTable(), false, compositionIndex);
		tableCompo.getModel().fireTableDataChanged();
		if (tableCompo.getSortedColumn() == null) {
			tableCompo.setSortedColumn(compositionIndex.get(Index.RANK));
			tableCompo.setSortOrder(SortOrder.ASCENDING);
		}
		tableCompo.getRowSorter().setSortKeys(Collections
				.singletonList(new RowSorter.SortKey(tableCompo.getSortedColumn(), tableCompo.getSortOrder())));
		tableCompo.setSelectedRow(-1);
		tableCompo.getColumnModel().getColumn(compositionIndex.get(Index.LINE_NUMBER)).setMinWidth(30);
		tableCompo.getColumnModel().getColumn(compositionIndex.get(Index.LINE_NUMBER)).setMaxWidth(30);
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(compositionIndex.get(Index.DECILE)));
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(compositionIndex.get(Index.DELETED) - 1));
		tableCompo.removeColumn(tableCompo.getColumnModel().getColumn(compositionIndex.get(Index.UUID) - 2));
		tableCompo.getTable().repaint();
		if (scrollTop) {
			((JScrollPane) tableCompo.getTable().getParent().getParent()).getVerticalScrollBar().setValue(0);
		}
		LOG.debug("End updateCompoTable");
	}

	/**
	 * Refreshs fichier panel data.
	 */
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

	/**
	 * Gets the compositions for the given file.
	 *
	 * @param file the file requested
	 * @return a list of compositions
	 */
	public List<Composition> getCompoListFromData(Fichier file) {
		return getCompoList(file).getValue();
	}

	/**
	 * Replaces with the given compositions the given file.
	 *
	 * @param file the file to edit the compositions
	 * @param list the compositions to set
	 */
	private void setCompoListFromData(Fichier file, List<Composition> list) {
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
						&& e.getKey().getCategorie() == file.getCategorie()
						&& e.getKey().getSorted().equals(file.getSorted()))
				.findFirst().orElseThrow(() -> new MinorException("Can't find fichier " + file));
	}

	/**
	 * Pretends to make a search by filename and opens to a specific composition.
	 *
	 * @param fileName name of file
	 * @param uuids uuids of the composition
	 */
	public void searchProgrammatically(String fileName, List<String> uuids) {
		LOG.debug("Start searchProgrammatically");
		this.filename.setText(fileName);
		searchAction();
		compositionList = findFichierInMap(fileName).map(Entry::getValue).orElse(new ArrayList<Composition>());
		updateCompoTable(fileName, false);
		if (!CollectionUtils.isEmpty(uuids)) {
			setSelectedRow(uuids);
		}
		LOG.debug("End searchProgrammatically");
	}

	private void setSelectedRow(List<String> uuids) {
		int indexOf = tableCompo.getTable().getRowSorter()
				.convertRowIndexToView((tableCompo.getModel().getDataVector()).stream()
						.map(v -> MiscUtils.stringToUuids((String) v.get(compositionIndex.get(Index.UUID))))
						.collect(Collectors.toList()).indexOf(uuids));
		tableCompo.getTable().setRowSelectionInterval(indexOf, indexOf);
	}

	public JTable getTableFiles() {
		return tableFiles.getTable();
	}

	public JTable getTableCompo() {
		return tableCompo.getTable();
	}

	public JButton getSearch() {
		return search;
	}

	public static ColumnIndex getFichierindex() {
		return fichierIndex;
	}

	public static ColumnIndex getCompositionindex() {
		return compositionIndex;
	}
}
