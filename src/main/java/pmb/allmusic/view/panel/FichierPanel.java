package pmb.allmusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.swing.FontIcon;
import pmb.allmusic.file.CsvFile;
import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.SearchMethod;
import pmb.allmusic.model.SearchRange;
import pmb.allmusic.utils.CompositionUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FichierUtils;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.utils.ScoreUtils;
import pmb.allmusic.utils.SearchUtils;
import pmb.allmusic.view.ActionPanel;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.ModificationComposition;
import pmb.allmusic.view.PanelUtils;
import pmb.allmusic.view.Resize;
import pmb.allmusic.view.TableBuilder;
import pmb.allmusic.view.component.JComboBoxInput;
import pmb.allmusic.view.component.JComboCheckBox;
import pmb.allmusic.view.component.MyInputRange;
import pmb.allmusic.view.component.MyInputText;
import pmb.allmusic.view.component.MyTable;
import pmb.allmusic.view.dialog.DialogFileTable;
import pmb.allmusic.view.dialog.ModifyFichierDialog;
import pmb.allmusic.view.model.CompoFichierPanelModel;
import pmb.allmusic.view.model.FichierPanelModel;
import pmb.allmusic.view.popup.CompositionPopupMenu;
import pmb.allmusic.view.popup.FichierPopupMenu;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.VariousUtils;

/**
 * Pour rechercher des fichiers et afficher/modifier/supprimer leurs compositions. Created by PBR on
 * 29 mai 2018.
 */
public class FichierPanel extends JPanel implements ModificationComposition, ActionPanel {
  private static final long serialVersionUID = 8581952935884211032L;

  private static final Logger LOG = LogManager.getLogger(FichierPanel.class);

  private static final FontIcon HIDE_ICON = FontIcon.of(Constant.ICON_HIDE);
  private static final FontIcon SHOW_ICON = FontIcon.of(Constant.ICON_SHOW);
  private static final int MIN_HEIGHT_TABLE = 41;
  private static final int MAX_HEIGHT_TABLE = 82;

  private static final ColumnIndex fichierIndex =
      new ColumnIndex()
          .put(Index.LINE_NUMBER, 0)
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

  private static final ColumnIndex compositionIndex =
      new ColumnIndex()
          .put(Index.LINE_NUMBER, 0)
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

  private static final String[] headerFiles = {
    "#",
    "Auteur",
    "Nom du fichier",
    "Type",
    "Publication",
    "Categorie",
    "Dates",
    "Supprimés",
    "Création",
    "Score",
    "Score Supprimés",
    "Taille",
    "Classé"
  };
  private static final String[] headerCompo = {
    "#", "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers", "Score", "", "", "", ""
  };

  /** Constructeur de {@link FichierPanel}. */
  public FichierPanel() {
    super();
    LOG.debug("Start FichierPanel");
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    LOG.debug("End FichierPanel");
  }

  /** Initialise tous les composants du {@link FichierPanel}. */
  public void initPanel() {
    LOG.debug("Start initPanel");

    parentSize = this.getParent().getPreferredSize();
    initSearchBtn();
    initData();
    initFichierTable();
    initCompoTable();

    LOG.debug("End initPanel");
  }

  /** Initialise les composants de recherche. */
  private void initSearchBtn() {
    JPanel header = new JPanel(new GridLayout(2, 1));
    initInputs(header);
    initButtons(header);
    PanelUtils.setSize(
        header, (int) parentSize.getWidth(), Math.floorDiv(15 * (int) parentSize.getHeight(), 100));
    this.add(header);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void initInputs(JPanel header) {
    JPanel inputs = new JPanel();
    PanelUtils.setFlowLayout(inputs);
    Resize resize = new Resize(8);
    // Auteur
    auteur =
        new ComponentBuilder<MyInputText, String>(MyInputText.class)
            .withParent(inputs)
            .withAsyncValues(OngletPanel::getAuthorList, OngletPanel.getAsyncList())
            .withLabel("Auteur : ")
            .withResize(resize)
            .withFilterContains(true)
            .withFlowLayout()
            .build();
    // Nom du fichier
    filename =
        new ComponentBuilder<MyInputText, String>(MyInputText.class)
            .withParent(inputs)
            .withLabel("Nom du fichier : ")
            .withFlowLayout()
            .withResize(resize)
            .build();
    // Publi
    publi =
        new ComponentBuilder<JComboBoxInput, String>(JComboBoxInput.class)
            .withParent(inputs)
            .withValues(VariousUtils.getEnumValues(SearchRange.values(), SearchRange::getValue))
            .withLabel("Année de publication : ")
            .withFlowLayout()
            .withResize(resize)
            .build();
    // Range
    range =
        new ComponentBuilder<MyInputRange, String>(MyInputRange.class)
            .withParent(inputs)
            .withLabel("Année(s) du classement : ")
            .withResize(resize)
            .withFlowLayout()
            .build();
    // Type
    type =
        new ComponentBuilder<JComboCheckBox, String>(JComboCheckBox.class)
            .withParent(inputs)
            .withValues(VariousUtils.getEnumValues(RecordType.values(), RecordType::getRecordType))
            .withFlowLayout()
            .withLabel("Type : ")
            .withResize(resize)
            .build();
    // Categorie
    cat =
        new ComponentBuilder<JComboCheckBox, String>(JComboCheckBox.class)
            .withParent(inputs)
            .withValues(VariousUtils.getEnumValues(Cat.values(), Cat::getValue))
            .withFlowLayout()
            .withLabel("Catégorie : ")
            .withResize(resize)
            .build();
    // Deleted
    deleted =
        new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class)
            .withParent(inputs)
            .withLabel("Supprimés : ")
            .withFlowLayout()
            .withInitialValue(true)
            .withResize(resize)
            .build();
    // Sorted
    sorted =
        new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class)
            .withParent(inputs)
            .withLabel("Classé : ")
            .withFlowLayout()
            .withResize(resize)
            .build();
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
    JButton hideFileList =
        ComponentBuilder.buildJButton("Cacher la liste des fichiers", 180, Constant.ICON_HIDE);
    hideFileList.addActionListener(
        (ActionEvent e) -> {
          showFichierTable = !showFichierTable;
          filePanel.setVisible(showFichierTable);
          if (showFichierTable) {
            hideFileList.setText("Cacher la liste des fichiers");
            hideFileList.setIcon(HIDE_ICON);
            setTableSize(compoPanel, MIN_HEIGHT_TABLE);
          } else {
            hideFileList.setText("Afficher la liste des fichiers");
            hideFileList.setIcon(SHOW_ICON);
            setTableSize(compoPanel, MAX_HEIGHT_TABLE);
          }
        });
    buttons.add(hideFileList);
    // hideCompoList
    JButton hideCompoList =
        ComponentBuilder.buildJButton("Cacher la liste des compositions", 200, Constant.ICON_HIDE);
    hideCompoList.addActionListener(
        (ActionEvent e) -> {
          showCompoTable = !showCompoTable;
          compoPanel.setVisible(showCompoTable);
          if (showCompoTable) {
            hideCompoList.setText("Cacher la liste des compositions");
            hideCompoList.setIcon(HIDE_ICON);
            setTableSize(filePanel, MIN_HEIGHT_TABLE);
          } else {
            hideCompoList.setText("Afficher la liste des compositions");
            hideCompoList.setIcon(SHOW_ICON);
            setTableSize(filePanel, MAX_HEIGHT_TABLE);
          }
        });
    buttons.add(hideCompoList);
    // Delete Btn
    JButton delete =
        ComponentBuilder.buildJButton(
            "<html>Supprimer les compositions sélectionnées</html>", 200, Constant.ICON_DELETE);
    delete.addActionListener(
        (ActionEvent e) -> {
          List<Object> selected = tableCompo.getModel().getSelected();
          try {
            PanelUtils.deleteCompositionAction(
                compositionList,
                selected.stream()
                    .map(
                        v ->
                            VariousUtils.stringToUuids(
                                    ((Vector<String>) v).get(compositionIndex.get(Index.UUID)))
                                .get(0))
                    .collect(Collectors.toList()));
            updateCompoTable(selectedFichierName, false);
            resultLabel.setText(selected.size() + " élément(s) supprimé(s)");
          } catch (MajorException e1) {
            LOG.error("Error when deleting compositions in Fichier result", e1);
            PanelUtils.setWrappedLabel(resultLabel, e1.getMessage());
          }
        });
    buttons.add(delete);
    // CSV
    JButton csv =
        ComponentBuilder.buildJButton(
            "<html>Télécharger la liste des fichiers en CSV</html>", 300, Constant.ICON_DOWNLOAD);
    csv.addActionListener(e -> generatesCsvFile());
    buttons.add(csv);
    // Label pour afficher les resultats
    resultLabel =
        new ComponentBuilder<JLabel, String>(JLabel.class)
            .withParent(buttons)
            .withLabel("")
            .withHeight(ComponentBuilder.PANEL_HEIGHT)
            .withColor(new Color(8, 187, 81))
            .withFontSize(16)
            .build();
    header.add(buttons);
  }

  private void generatesCsvFile() {
    LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(headerFiles));
    csvHeader.add(
        "Critères: "
            + Arrays.asList(
                    publi.getInput().getText(),
                    range.getFirst().getText(),
                    range.getSecond().getText(),
                    filename.getText(),
                    cat.getSelectedItems(),
                    type.getSelectedItems(),
                    auteur.getText(),
                    "Sorted:" + Boolean.toString(sorted.isSelected()),
                    "Deleted:" + Boolean.toString(deleted.isSelected()))
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" ")));
    String name =
        CsvFile.exportCsv(
            "files",
            PanelUtils.convertDataVectorToList(tableFiles.getTable()),
            null,
            csvHeader.toArray(new String[headerFiles.length + 1]));
    try {
      FilesUtils.openFileInExcel(name);
    } catch (MajorException e1) {
      LOG.error("Erreur de l'ouverture avec excel du fichier: {}", name, e1);
    }
  }

  private void initFichierTable() {
    filePanel = new JPanel(new BorderLayout());

    try {
      tableFiles =
          new TableBuilder()
              .withModelAndData(null, headerFiles, FichierPanelModel.class)
              .withColumnIndex(fichierIndex)
              .withDefaultRowSorterListener()
              .withMouseClickedAction(
                  e ->
                      selectedFichierName =
                          PanelUtils.getSelectedRowByPoint((JTable) e.getSource(), e.getPoint())
                              .map(row -> row.get(fichierIndex.get(Index.FILE_NAME)))
                              .orElse(""))
              .withMouseClickedActions(
                  (e, selectedRow) -> {
                    LOG.debug("Start left mouse, open");
                    // Affiche les compositions du fichier sélectionné
                    // TODO
                    compositionList =
                        findFichierInMap(selectedRow.get(fichierIndex.get(Index.FILE_NAME)))
                            .get()
                            .getValue();
                    if (!deleted.isSelected()) {
                      compositionList =
                          compositionList.stream()
                              .filter(c -> !c.isDeleted())
                              .collect(Collectors.toList());
                    }
                    updateCompoTable(selectedFichierName, true);
                    LOG.debug("End left mouse, open");
                  },
                  false)
              .withPopupMenu(new FichierPopupMenu(fichierIndex))
              .withKeyListener()
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
      tableCompo =
          new TableBuilder()
              .withModelAndData(null, headerCompo, CompoFichierPanelModel.class)
              .withColumnIndex(compositionIndex)
              .withRowSorterListenerDelete()
              .withMouseClickedActions(
                  (e, selectedRow) -> {
                    LOG.debug("Start left mouse");
                    // Ouvre une popup pour afficher les fichiers de la
                    // composition sélectionnée
                    DialogFileTable pop =
                        new DialogFileTable(
                            "Fichier",
                            CompositionUtils.findByUuid(
                                    compositionList,
                                    VariousUtils.stringToUuids(
                                        selectedRow.get(compositionIndex.get(Index.UUID))))
                                .map(c -> new LinkedList<>(Arrays.asList(c)))
                                .orElse(new LinkedList<>()),
                            400,
                            new RowSorter.SortKey(
                                DialogFileTable.getIndex().get(Index.SCORE), SortOrder.DESCENDING));
                    pop.show();
                    LOG.debug("End left mouse");
                  },
                  true)
              .withPopupMenu(new CompositionPopupMenu(this.getClass(), null, compositionIndex))
              .withKeyListener()
              .build();
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
    new Thread(
            () -> {
              data = new ConcurrentHashMap<>();
              ImportXML.importXML(Constant.getFinalFilePath()).parallelStream()
                  .forEach(
                      c ->
                          c.getFiles().parallelStream()
                              .forEach(
                                  f -> {
                                    Composition copy = new Composition();
                                    CompositionUtils.copy(c, copy);
                                    findFichierInMap(f.getFileName())
                                        .ifPresentOrElse(
                                            entry -> data.get(entry.getKey()).add(copy),
                                            () -> {
                                              ArrayList<Composition> list = new ArrayList<>();
                                              list.add(copy);
                                              data.put(f, list);
                                            });
                                  }));
              copyDataInSearchResult();
              LOG.debug("Data calculated");
            })
        .start();
    LOG.debug("End initData");
  }

  private void copyDataInSearchResult() {
    searchResult = new ConcurrentHashMap<>(); // the map displays in the table
    data.entrySet().stream()
        .forEach(
            e ->
                searchResult.put(
                    e.getKey(),
                    e.getValue().stream().map(Composition::new).collect(Collectors.toList())));
  }

  private Optional<Entry<Fichier, List<Composition>>> findFichierInMap(String fileName) {
    return data.entrySet().stream()
        .filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey().getFileName(), fileName))
        .findFirst();
  }

  /**
   * Recalculates data for given files from given list.
   *
   * @param list new data
   * @param files the files to reprocess
   */
  public void reprocessSpecificFichier(List<Composition> list, List<Fichier> files) {
    files.parallelStream()
        .forEach(
            f ->
                setCompoListFromData(
                    f,
                    list.parallelStream()
                        .filter(
                            c ->
                                c.getFiles().stream()
                                    .map(Fichier::getFileName)
                                    .anyMatch(
                                        name ->
                                            StringUtils.equalsIgnoreCase(name, f.getFileName())))
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
    Fichier key =
        findFichierInMap(fileName)
            .map(Entry::getKey)
            .orElseThrow(() -> new MajorException("Can't find Fichier in map: " + fileName));
    // Lancement de la popup de modification
    ModifyFichierDialog md = new ModifyFichierDialog(null, "Modifier un fichier", true, selected);
    md.show();
    Vector<String> newFichier;
    if (BooleanUtils.isTrue(md.getSendData())) {
      // On recupère le fichier si il a bien été modifié
      LOG.debug("Fichier modifiée");
      newFichier = md.getFichier();
    } else {
      LOG.debug("Aucune modification");
      return;
    }
    // Retire les caractères interdits pour windows
    if (Arrays.stream(MyConstant.getForbiddenCharactersFilename())
        .anyMatch(s -> newFichier.get(fichierIndex.get(Index.FILE_NAME)).contains(s))) {
      Arrays.stream(MyConstant.getForbiddenCharactersFilename())
          .forEach(
              s ->
                  newFichier.set(
                      fichierIndex.get(Index.FILE_NAME),
                      newFichier.get(fichierIndex.get(Index.FILE_NAME)).replaceAll(s, "")));
    }
    Fichier modifiedFichier = null;
    try {
      // Modification du fichier
      modifiedFichier =
          FichierUtils.modifyFichier(
              fileName,
              newFichier.get(fichierIndex.get(Index.FILE_NAME)),
              newFichier.get(fichierIndex.get(Index.PUBLISH)),
              newFichier.get(fichierIndex.get(Index.RANGE)),
              newFichier.get(fichierIndex.get(Index.CAT)),
              newFichier.get(fichierIndex.get(Index.FILE_SIZE)),
              newFichier.get(fichierIndex.get(Index.SORTED)));
    } catch (MajorException e) {
      String log = "Erreur pendant FichierUtils.modifyFichier";
      LOG.error(log, e);
      resultLabel.setText(log + e);
      return;
    }
    // Mise à jour du JTable
    List<Composition> list = data.get(key);
    list.stream()
        .forEach(
            FichierUtils.modifyOneFichier(
                fileName,
                newFichier.get(fichierIndex.get(Index.FILE_NAME)),
                newFichier.get(fichierIndex.get(Index.PUBLISH)),
                newFichier.get(fichierIndex.get(Index.RANGE)),
                newFichier.get(fichierIndex.get(Index.CAT)),
                newFichier.get(fichierIndex.get(Index.FILE_SIZE)),
                newFichier.get(fichierIndex.get(Index.SORTED))));
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
  public void splitCompositionAction(Vector<Object> selected) {
    LOG.debug("Start splitCompositionAction");
    compositionList =
        PanelUtils.splitCompositionAction(
            selected, compositionList, FichierPanel.getCompositionindex());
    updateCompoTable(selectedFichierName, false);
    LOG.debug("End splitCompositionAction");
  }

  private void searchAction() {
    LOG.debug("Start searchAction");
    resultLabel.setText("");
    // Filters on record type
    searchResult =
        data.entrySet().parallelStream()
            .filter(
                e -> {
                  if (StringUtils.isNotBlank(type.getSelectedItems())) {
                    return e.getValue().stream()
                        .anyMatch(
                            c ->
                                Arrays.stream(StringUtils.split(type.getSelectedItems(), ";"))
                                    .anyMatch(
                                        (t -> c.getRecordType() == RecordType.getByValue(t))));
                  } else {
                    return true;
                  }
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (!searchResult.keySet().isEmpty()) {
      // Filter on the files list with the others criteria
      Map<String, String> criteria =
          Map.of(
              SearchUtils.CRITERIA_PUBLISH_YEAR,
              publi.getInput().getText(),
              SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE,
              (String) publi.getComboBox().getSelectedItem(),
              SearchUtils.CRITERIA_FILENAME,
              filename.getText(),
              SearchUtils.CRITERIA_AUTHOR,
              auteur.getText(),
              SearchUtils.CRITERIA_CAT,
              cat.getSelectedItems(),
              SearchUtils.CRITERIA_DATE_BEGIN,
              range.getFirst().getText(),
              SearchUtils.CRITERIA_DATE_END,
              range.getSecond().getText(),
              SearchUtils.CRITERIA_SORTED,
              sorted.isSelected() ? Boolean.TRUE.toString() : "");
      Optional.of(
              criteria.entrySet().stream()
                  .filter(e -> StringUtils.isNotBlank(e.getValue()))
                  .map(e -> e.getKey() + ": " + e.getValue())
                  .collect(Collectors.joining(", ")))
          .ifPresent(crit -> LOG.debug("Criteria: {}", crit));
      searchResult =
          searchResult.entrySet().parallelStream()
              .filter(
                  e ->
                      SearchUtils.filterFichier(
                          SearchMethod.CONTAINS,
                          new JaroWinklerDistance(),
                          SearchUtils.cleanCriteria(criteria),
                          e.getKey()))
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
    tableFiles.updateTable(
        buildFichierScore(
            FichierUtils.convertCompositionListToFichierVector(Arrays.asList(c), false, true)),
        new SortKey(fichierIndex.get(Index.FILE_NAME), SortOrder.ASCENDING),
        true);
    compositionList = new ArrayList<>();
    updateCompoTable(null, true);
    LOG.debug("Start updateFileTable");
  }

  private Vector<Vector<Object>> buildFichierScore(Vector<Vector<Object>> dataVector) {
    // Calculates score by getting the average of the score of each compositions
    // Calculates the percentage of deleted composition by the score
    for (Vector<Object> vector : dataVector) {
      findFichierInMap((String) vector.get(fichierIndex.get(Index.FILE_NAME)))
          .ifPresentOrElse(
              entry -> {
                LongSummaryStatistics score =
                    entry.getValue().parallelStream()
                        .map(ScoreUtils::getCompositionScore)
                        .mapToLong(x -> x)
                        .summaryStatistics();
                vector.add(fichierIndex.get(Index.SCORE), Math.round(score.getAverage()));
                long scoreDeleted =
                    entry.getValue().parallelStream()
                        .filter(Composition::isDeleted)
                        .map(ScoreUtils::getCompositionScore)
                        .mapToLong(x -> x)
                        .sum();
                vector.add(
                    fichierIndex.get(Index.SCORE_DELETED),
                    Math.round(100 * (double) scoreDeleted / score.getSum()) + " %");
              },
              () -> {
                LOG.warn("Entry not found ! ");
                vector.add(fichierIndex.get(Index.SCORE), 0);
              });
    }
    return dataVector;
  }

  /**
   * Met à jour le tableau des compositions.
   *
   * @param scrollTop {@code boolean} true scroll to the top of the table
   * @param {@link String} selectedFile le fichier selectionné
   */
  private void updateCompoTable(String selectedFile, boolean scrollTop) {
    LOG.debug("Start updateCompoTable");
    tableCompo.updateTable(
        selectedFile != null && !compositionList.isEmpty()
            ? CompositionUtils.convertCompositionListToVector(
                compositionList, selectedFile, true, true, true, true, true)
            : new Vector<>(),
        new SortKey(compositionIndex.get(Index.RANK), SortOrder.ASCENDING),
        scrollTop);
    LOG.debug("End updateCompoTable");
  }

  /** Refreshs fichier panel data. */
  public void updateData() {
    new Thread(
            () -> {
              initData();
              searchAction();
            })
        .start();
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
    PanelUtils.setSize(
        panel,
        (int) parentSize.getWidth(),
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
        .filter(
            e ->
                StringUtils.equalsIgnoreCase(e.getKey().getAuthor(), file.getAuthor())
                    && StringUtils.equalsIgnoreCase(e.getKey().getFileName(), file.getFileName())
                    && NumberUtils.compare(e.getKey().getPublishYear(), file.getPublishYear()) == 0
                    && e.getKey().getSize().equals(file.getSize())
                    && e.getKey().getRangeDateBegin().equals(file.getRangeDateBegin())
                    && e.getKey().getRangeDateEnd().equals(file.getRangeDateEnd())
                    && e.getKey().getCategorie() == file.getCategorie()
                    && e.getKey().getSorted().equals(file.getSorted()))
        .findFirst()
        .orElseThrow(() -> new MinorException("Can't find fichier " + file));
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
    compositionList = findFichierInMap(fileName).map(Entry::getValue).orElse(new ArrayList<>());
    updateCompoTable(fileName, false);
    if (!CollectionUtils.isEmpty(uuids)) {
      setSelectedRow(uuids);
    }
    LOG.debug("End searchProgrammatically");
  }

  private void setSelectedRow(List<String> uuids) {
    int indexOf =
        tableCompo
            .getTable()
            .getRowSorter()
            .convertRowIndexToView(
                (tableCompo.getModel().getDataVector())
                    .stream()
                        .map(
                            v ->
                                VariousUtils.stringToUuids(
                                    (String) v.get(compositionIndex.get(Index.UUID))))
                        .collect(Collectors.toList())
                        .indexOf(uuids));
    tableCompo.getTable().setRowSelectionInterval(indexOf, indexOf);
  }

  public JTable getTableFiles() {
    return tableFiles.getTable();
  }

  public JTable getTableCompo() {
    return tableCompo.getTable();
  }

  public static ColumnIndex getFichierindex() {
    return fichierIndex;
  }

  public static ColumnIndex getCompositionindex() {
    return compositionIndex;
  }

  public Map<Fichier, List<Composition>> getData() {
    return data;
  }

  @Override
  public JButton getActionButton() {
    return search;
  }
}
