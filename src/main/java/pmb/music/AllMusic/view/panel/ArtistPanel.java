package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.file.CsvFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.SearchMethod;
import pmb.music.AllMusic.model.SearchRange;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.SearchUtils;
import pmb.music.AllMusic.view.ActionPanel;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.component.MyInputText;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;
import pmb.music.AllMusic.view.model.ArtistModel;
import pmb.music.AllMusic.view.popup.ArtistPopupMenu;

/**
 * L'onglet Artiste, classement des artistes les plus cités.
 *
 */
public class ArtistPanel extends JPanel implements ActionPanel{

    private static final long serialVersionUID = 2593372709628283573L;

    private static final Logger LOG = LogManager.getLogger(ArtistPanel.class);

    private static final ColumnIndex index = new ColumnIndex()
            .put(Index.LINE_NUMBER, 0)
            .put(Index.ARTIST, 1)
            .put(Index.NB_TOTAL, 2)
            .put(Index.PERCENT_DELETED, 5)
            .put(Index.SCORE_TOTAL, 6)
            .put(Index.SCORE_SONG, 7)
            .put(Index.SCORE_ALBUM, 8)
            .put(Index.SCORE_DELETED, 9);

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
     * @param withArtist if true the artist panel is displayed and the data is calculated
     */
    public ArtistPanel(boolean withArtist) {
        super();
        LOG.debug("Start ArtistPanel");
        this.withArtist = withArtist;
        if (withArtist) {
            this.setLayout(new BorderLayout());
            JPanel header = initHeader();
            initTable();
            JButton csv = ComponentBuilder.buildJButton("Télécharger la recherche en CSV", 220, Constant.ICON_DOWNLOAD);
            csv.addActionListener((ActionEvent e) -> generatesCsvFile());
            header.add(csv);
            this.add(header, BorderLayout.PAGE_START);
        }
        LOG.debug("End ArtistPanel");
    }

    private void generatesCsvFile() {
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
            FilesUtils.openFileInExcel(name);
        } catch (MajorException e1) {
            LOG.error("Erreur de l'ouverture avec excel du fichier: {}", name, e1);
        }
        LOG.debug("End Csv");
    }

    @SuppressWarnings("unchecked")
    private JPanel initHeader() {
        LOG.debug("Start initHeader");
        JPanel header = new JPanel();
        // Artist
        artist = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(header)
                .withAsyncValues(OngletPanel::getArtistList, OngletPanel.getAsyncList()).withLabel("Artiste : ")
                .withPanelWidth(200).withComponentWidth(150).withLabelWidth(150).build();
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
                .withAsyncValues(OngletPanel::getAuthorList, OngletPanel.getAsyncList()).withLabel("Auteur : ")
                .withPanelWidth(200).withFilterContains(true).withComponentWidth(150).withLabelWidth(150).build();
        // Categorie
        cat = (JComboCheckBox) new ComponentBuilder<String>(JComboCheckBox.class).withParent(header)
                .withValues(MiscUtils.getEnumValues(Cat.values(), Cat::getValue))
                .withLabel("Catégorie : ").withPanelWidth(180).withComponentWidth(120).withLabelWidth(150).build();
        // Deleted
        deleted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(header).withInitialValue(true)
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
            table = new TableBuilder().withModelAndData(null, title, ArtistModel.class).withColumnIndex(index)
                    .withDefaultRowSorterListener().withMouseClickedActions((e, selectedRow) -> {
                        LOG.debug("Start artist mouse");
                        LOG.debug(selectedRow);
                        // Affiche tous les fichiers de l'artiste double cliqué
                        CompositionUtils
                                .findArtistKey(searchResult, new JaroWinklerDistance(),
                                        selectedRow.get(ArtistPanel.getIndex().get(Index.ARTIST)))
                                .ifPresentOrElse(
                                        key -> new DialogFileTable("Fichier", searchResult.get(key), 600,
                                                new RowSorter.SortKey(DialogFileTable.getIndex().get(Index.SCORE),
                                                        SortOrder.ASCENDING)).show(),
                                        () -> new ExceptionDialog("Error when searching: "
                                                + selectedRow.get(ArtistPanel.getIndex().get(Index.ARTIST))
                                                + " in data table", "", null).setVisible(true));
                        LOG.debug("End artist mouse");
                    }, true).withKeyListener().withPopupMenu(new ArtistPopupMenu(index)).build();
            updateArtistPanel();
            this.add(new JScrollPane(table.getTable()), BorderLayout.CENTER);
        } catch (MajorException e1) {
            LOG.error("An error occured when init artist table", e1);
            return;
        }

        LOG.debug("End initTable");
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
                LOG.warn("{} n'a pas pu etre supprimé", Constant.ARTIST_PANEL_RESULT_FILE, e);
            }
        }
        if (updateArtistThread != null) {
            updateArtistThread.interrupt();
        }
    }

    private void startUpdateArtistThread() {
        if (!new File(Constant.getOutputDir()).exists()) {
            LOG.error("Output directory doesn't exist");
            return;
        }
        updateArtistThread = new Thread(() -> {
            LOG.debug("Start ThreadUpdateArtist");
            if (!new File(Constant.ARTIST_PANEL_RESULT_FILE).exists()) {
                data = CompositionUtils.groupCompositionByArtist(ImportXML.importXML(Constant.getFinalFilePath()));
                FilesUtils.exportJsonInFile(data, Constant.ARTIST_PANEL_RESULT_FILE);
            } else {
                try {
                    data = MiscUtils
                            .readValueAsMapOfList(FilesUtils.readFirstLine(Constant.ARTIST_PANEL_RESULT_FILE));
                } catch (IOException e1) {
                    LOG.error("Error when reading artist json file", e1);
                }
            }
            SwingUtilities.invokeLater(() -> {
                // Called when data are finally calculated
                resetAction();
                searchResult = copyAndFilterDeletedAndArtist();
                OngletPanel.getAsyncList().whenCompleteAsync((v, e) -> updateTable(searchResult));
            });
            LOG.debug("End ThreadUpdateArtist");
        });
        updateArtistThread.setDaemon(true);
        updateArtistThread.setPriority(Thread.MIN_PRIORITY);
        updateArtistThread.start();
    }

    /**
     * Copy with imported or calculated data and filters on whether to show deleted compositions or not.
     * @return an HashMap
     */
    private Map<String, List<Composition>> copyAndFilterDeletedAndArtist() {
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        return data.entrySet().stream()
                .filter(e -> SearchUtils.compareString(artist.getText(), e.getKey(), SearchMethod.CONTAINS, jaro)
                        || StringUtils.isBlank(artist.getText()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                        .filter(c -> deleted.isSelected() || !c.isDeleted()).collect(Collectors.toList())));
    }

    private void updateTable(Map<String, List<Composition>> donnee) {
        LOG.debug("Start updateTable");
        table.updateTable(CompositionUtils.convertArtistPanelResultToVector(donnee, true),
                new SortKey(index.get(Index.NB_TOTAL), SortOrder.DESCENDING), true);
        if (!deleted.isSelected()) {
            table.removeColumn(Index.PERCENT_DELETED);
            table.removeColumn(Index.SCORE_DELETED);
        } else {
            table.getDeletedColumnIndex().getAndAdd(-2);
        }
        LOG.debug("End updateTable");
    }

    private void searchAction() {
        LOG.debug("Start search");
        if (MapUtils.isNotEmpty(data)) {
            searchResult = copyAndFilterDeletedAndArtist();
            Map<String, String> criteria = new HashMap<>();
            criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, publi.getInput().getText());
            criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE, (String) publi.getComboBox().getSelectedItem());
            criteria.put(SearchUtils.CRITERIA_AUTHOR, auteur.getText());
            criteria.put(SearchUtils.CRITERIA_CAT, cat.getSelectedItems());
            criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, range.getFirst().getText());
            criteria.put(SearchUtils.CRITERIA_DATE_END, range.getSecond().getText());
            JaroWinklerDistance jaro = new JaroWinklerDistance();
            searchResult = searchResult.entrySet().stream().map(e -> {
                e.setValue(e.getValue().stream().map(c -> {
                    Composition copy = new Composition(c);
                    copy.setFiles(copy.getFiles().stream()
                            .filter(f -> SearchUtils.filterFichier(SearchMethod.WHOLE_WORD, jaro, SearchUtils.cleanCriteria(criteria), f))
                            .collect(Collectors.toList()));
                    return copy;
                }).collect(Collectors.toList()));
                return e;
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        updateTable(searchResult);
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
        deleted.setSelected(true);
        LOG.debug("End resetAction");
    }

    public JTable getTable() {
        return table.getTable();
    }

    public static ColumnIndex getIndex() {
        return index;
    }

    @Override
    public JButton getActionButton() {
        return search;
    }
}
