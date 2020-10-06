package pmb.allmusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.exception.MajorException;
import pmb.allmusic.file.CsvFile;
import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.SearchMethod;
import pmb.allmusic.model.SearchRange;
import pmb.allmusic.utils.CompositionUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.utils.MiscUtils;
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
import pmb.allmusic.view.dialog.ExceptionDialog;
import pmb.allmusic.view.model.SearchPanelModel;
import pmb.allmusic.view.popup.CompositionPopupMenu;
import pmb.allmusic.xml.ImportXML;

/**
 * Gère le panel search.
 *
 * @author pmbroca
 */
public class SearchPanel extends JPanel implements ModificationComposition, ActionPanel {

    private static final Logger LOG = LogManager.getLogger(SearchPanel.class);

    private static final long serialVersionUID = 2593372709628283573L;
    private JLabel countLabel;
    private JLabel deleteLabel;
    private JButton search;

    private JComboBoxInput<String> publi;
    private MyInputRange range;
    private MyInputText fileName;
    private JCheckBox inFiles;
    private JCheckBox sorted;
    private JCheckBox deleted;
    private JCheckBox topTen;

    private MyTable tableResult;

    private JComboCheckBox cat;
    private JComboCheckBox type;
    private JComboBox<String> searchMethod;
    private MyInputText titre;
    private MyInputText artist;
    private MyInputText author;

    private List<Composition> compoResult = new ArrayList<>();

    private static final String[] title = { "#", "Artiste", "Titre", "Type", "Nombre de fichiers", "Score", "", "",
            "", "" };

    private static final ColumnIndex index = new ColumnIndex()
            .put(Index.LINE_NUMBER, 0)
            .put(Index.ARTIST, 1)
            .put(Index.TITLE, 2)
            .put(Index.TYPE, 3)
            .put(Index.FILE_SIZE, 4)
            .put(Index.SCORE, 5)
            .put(Index.DECILE, 6)
            .put(Index.SELECTED, 7)
            .put(Index.DELETED, 8)
            .put(Index.UUID, 9);

    /**
     * Génère le panel search.
     */
    public SearchPanel() {
        super();
        LOG.debug("Start SearchPanel");
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        initButtons(header);
        initSearchFields(header);
        this.add(header);
        initTable();
        LOG.debug("End SearchPanel");
    }

    /**
     * Insert les boutons du panel search en haut.
     *
     * @param header le header de l'onglet
     */
    @SuppressWarnings("unchecked")
    private void initButtons(JPanel header) {
        LOG.debug("Start initButtons");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        AbstractAction searchAction = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                searchAction();
            }
        };

        search = ComponentBuilder.buildJButton("Rechercher", 220, Constant.ICON_SEARCH);
        search.addActionListener(searchAction);
        search.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "Enter_pressed");
        search.getActionMap().put("Enter_pressed", searchAction);
        search.setEnabled(false);
        OngletPanel.getAsyncList().whenCompleteAsync((v, e) -> search.setEnabled(true));
        top.add(search);

        // Clear Btn
        JButton clear = ComponentBuilder.buildJButton("Réinitialiser recherche", 200, Constant.ICON_ERASE);
        clear.addActionListener((ActionEvent e) -> cleanAction());
        top.add(clear);

        // Delete Btn
        JButton delete = ComponentBuilder.buildJButton("Supprimer les compositions sélectionnées", 300,
                Constant.ICON_DELETE);
        delete.addActionListener((ActionEvent e) -> {
            List<Object> selected = tableResult.getModel().getSelected();
            try {
                PanelUtils.deleteCompositionAction(compoResult, selected.stream().map(v -> MiscUtils.stringToUuids(((Vector<String>) v).get(SearchPanel.getIndex().get(Index.UUID))).get(0)).collect(Collectors.toList()));
                updateTable(false);
                PanelUtils.setWrappedLabel(deleteLabel, selected.size() + " élément(s) supprimé(s)");
            } catch (MajorException e1) {
                LOG.error("Error when deleting compositions in Search result", e1);
                PanelUtils.setWrappedLabel(deleteLabel, e1.getMessage());
            }
        });
        top.add(delete);

        // Modif Btn
        JButton modif = ComponentBuilder.buildJButton("Modifier la composition sélectionnée", 300, Constant.ICON_EDIT);
        modif.addActionListener((ActionEvent e) -> {
            try {
                modifyCompositionAction((Vector<String>) tableResult.getModel().getSelected().get(0));
            } catch (MajorException e2) {
                LOG.error("An exception has been thrown when editing composition: ", e2);
                ExceptionDialog ed = new ExceptionDialog("An exception has been thrown when editing composition",
                        e2.getMessage(), e2);
                ed.setVisible(true);
            }
        });
        top.add(modif);

        // CSV
        JButton csv = ComponentBuilder.buildJButton("Télécharger le résultat de la recherche en CSV", 300,
                Constant.ICON_DOWNLOAD);
        csv.addActionListener(e -> generatesCsvFile());
        top.add(csv);
        header.add(new JScrollPane(top));
        LOG.debug("End initButtons");
    }

    private void generatesCsvFile() {
        LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(title));
        csvHeader
        .add("Critères: " + Arrays
                .asList(publi.getInput().getText(), range.getFirst().getText(), range.getSecond().getText(),
                        fileName.getText(),
                        Optional.ofNullable(searchMethod.getSelectedItem()).map(Object::toString)
                        .orElse(""),
                        cat.getSelectedItems(), type.getSelectedItems(), titre.getText(), artist.getText(),
                        author.getText(), "Sorted:" + Boolean.toString(sorted.isSelected()),
                        "Deleted:" + Boolean.toString(deleted.isSelected()),
                        "Top Ten:" + Boolean.toString(topTen.isSelected()))
        .stream().filter(StringUtils::isBlank).collect(Collectors.joining(" ")));
        if (deleted.isSelected()) {
            csvHeader.set(index.get(Index.DELETED), "Supprimé");
        }
        csvHeader.set(index.get(Index.DECILE), "Decile");
        String name = CsvFile.exportCsv("search", PanelUtils.convertDataVectorToList(tableResult.getTable()), null,
                csvHeader.toArray(new String[title.length + 1]));
        try {
            FilesUtils.openFileInExcel(name);
        } catch (MajorException e1) {
            LOG.error("Erreur de l'ouverture avec excel du fichier: {}", name, e1);
        }
    }

    /**
     * Initialise les champs de recherche.
     *
     * @param header le header de l'onglet
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initSearchFields(JPanel header) {
        LOG.debug("Start initSearchFields");

        JPanel topFields = new JPanel();
        PanelUtils.setFlowLayout(topFields);
        Resize topResize = new Resize(7);

        // Artiste
        artist = new ComponentBuilder<MyInputText, String>(MyInputText.class).withParent(topFields)
                .withAsyncValues(OngletPanel::getArtistList, OngletPanel.getAsyncList()).withLabel("Artiste : ")
                .withResize(topResize).build();
        // Titre
        titre = new ComponentBuilder<MyInputText, String>(MyInputText.class).withParent(topFields)
                .withAsyncValues(OngletPanel::getTitleList, OngletPanel.getAsyncList()).withLabel("Titre : ")
                .withResize(topResize).build();
        // SearchMethod
        searchMethod = new ComponentBuilder<JComboBox, String>(JComboBox.class).withParent(topFields)
                .withLabel("Méthode de recherche : ")
                .withValues(MiscUtils.getEnumValues(SearchMethod.values(), SearchMethod::getValue))
                .withResize(topResize).build();
        // Nom du fichier
        fileName = new ComponentBuilder<MyInputText, String>(MyInputText.class).withParent(topFields)
                .withLabel("Nom du fichier : ").withResize(topResize).build();
        // Auteur
        author = new ComponentBuilder<MyInputText, String>(MyInputText.class).withParent(topFields)
                .withAsyncValues(OngletPanel::getAuthorList, OngletPanel.getAsyncList()).withLabel("Auteur : ")
                .withResize(topResize).build();
        // Type
        type = new ComponentBuilder<JComboCheckBox, String>(JComboCheckBox.class).withParent(topFields)
                .withValues(MiscUtils.getEnumValues(RecordType.values(), RecordType::getRecordType))
                .withLabel("Type : ").withResize(topResize).build();
        // Range
        range = new ComponentBuilder<MyInputRange, String>(MyInputRange.class).withParent(topFields)
                .withLabel("Année(s) du classement : ").withResize(topResize.addPanelWidth(50)).build();

        JPanel bottomFields = new JPanel();
        PanelUtils.setFlowLayout(bottomFields);
        Resize bottomResize = new Resize(8);

        // Categorie
        cat = new ComponentBuilder<JComboCheckBox, String>(JComboCheckBox.class).withParent(bottomFields)
                .withValues(MiscUtils.getEnumValues(Cat.values(), Cat::getValue)).withLabel("Catégorie : ")
                .withResize(bottomResize).build();
        // Publi
        publi = new ComponentBuilder<JComboBoxInput, String>(JComboBoxInput.class).withParent(bottomFields)
                .withValues(MiscUtils.getEnumValues(SearchRange.values(), SearchRange::getValue))
                .withLabel("Année de publication : ").withResize(bottomResize).build();
        // inFiles
        inFiles = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(bottomFields)
                .withLabel("<html>Rechercher dans les fichiers : </html>").withInitialValue(true)
                .withResize(bottomResize).build();
        // Sorted
        sorted = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(bottomFields)
                .withLabel("Trié : ").withResize(bottomResize).build();
        // Deleted
        deleted = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(bottomFields)
                .withLabel("Supprimé : ").withResize(bottomResize).build();
        // TopTen
        topTen = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(bottomFields)
                .withLabel("Top 10 : ").withResize(bottomResize).build();
        // Nombre de résultat
        countLabel = new ComponentBuilder<JLabel, String>(JLabel.class).withParent(bottomFields).withLabel("")
                .withResize(bottomResize).withColor(new Color(8, 187, 81)).withHeight(ComponentBuilder.PANEL_HEIGHT)
                .withFontSize(25).build();
        // Nombre de suppression
        deleteLabel = new ComponentBuilder<JLabel, String>(JLabel.class).withParent(bottomFields).withLabel("")
                .withResize(bottomResize).withColor(new Color(8, 187, 81)).withHeight(ComponentBuilder.PANEL_HEIGHT)
                .withFontSize(15).build();

        header.add(topFields);
        header.add(bottomFields);
        LOG.debug("End initSearchFields");
    }

    private void initTable() {
        LOG.debug("Start initTable");
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        try {
            tableResult = new TableBuilder().withModelAndData(null, title, SearchPanelModel.class)
                    .withColumnIndex(index).withRowSorterListenerDelete().withMouseClickedActions((e, selectedRow) -> {
                        LOG.debug("Start result mouse");
                        // Ouvre une popup pour afficher les fichiers de la
                        // composition sélectionnée
                        DialogFileTable pop = new DialogFileTable("Fichier",
                                CompositionUtils
                                .findByUuid(compoResult,
                                        MiscUtils.stringToUuids(
                                                selectedRow.get(SearchPanel.getIndex().get(Index.UUID))))
                                .map(c -> new LinkedList<>(Arrays.asList(c))).orElse(new LinkedList<>()),
                                400, new RowSorter.SortKey(DialogFileTable.getIndex().get(Index.SCORE),
                                        SortOrder.DESCENDING));
                        pop.show();
                        LOG.debug("End result mouse");
                    }, true).withPopupMenu(new CompositionPopupMenu(this.getClass(), null, SearchPanel.getIndex()))
                    .withKeyListener().build();
        } catch (MajorException e1) {
            LOG.error("An error occured when init search table", e1);
            PanelUtils.setWrappedLabel(countLabel, e1.getMessage());
            return;
        }

        bottom.add(new JScrollPane(tableResult.getTable()), BorderLayout.CENTER);
        this.add(bottom);
        LOG.debug("Start initTable");
    }

    private void searchAction() {
        LOG.debug("Start searchAction");
        deleteLabel.setText("");
        List<Composition> allCompo = ImportXML.importXML(Constant.getFinalFilePath());
        if (!allCompo.isEmpty()) {
            Map<String, String> criteria = new HashMap<>();
            criteria.put(SearchUtils.CRITERIA_ARTIST, artist.getText());
            criteria.put(SearchUtils.CRITERIA_TITRE, titre.getText());
            criteria.put(SearchUtils.CRITERIA_RECORD_TYPE, type.getSelectedItems());
            criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR, publi.getInput().getText());
            criteria.put(SearchUtils.CRITERIA_PUBLISH_YEAR_RANGE, (String) publi.getComboBox().getSelectedItem());
            criteria.put(SearchUtils.CRITERIA_FILENAME, fileName.getText());
            criteria.put(SearchUtils.CRITERIA_AUTHOR, author.getText());
            criteria.put(SearchUtils.CRITERIA_CAT, cat.getSelectedItems());
            criteria.put(SearchUtils.CRITERIA_DATE_BEGIN, range.getFirst().getText());
            criteria.put(SearchUtils.CRITERIA_DATE_END, range.getSecond().getText());
            if (sorted.isSelected()) {
                criteria.put(SearchUtils.CRITERIA_SORTED, Boolean.TRUE.toString());
            }
            if (topTen.isSelected()) {
                criteria.put(SearchUtils.CRITERIA_TOP, Boolean.TRUE.toString());
            }

            compoResult = new ArrayList<>();
            compoResult.addAll(SearchUtils.search(allCompo, criteria, inFiles.isSelected(),
                    SearchMethod.getByValue((String) searchMethod.getSelectedItem()), deleted.isSelected(), true));
            updateTable(true);
        }
        LOG.debug("End searchAction");
    }

    /**
     * Updates {@link SearchPanel} table data and handles sorting and rendering.
     * @param scrollTop if scroll at the top of the table once updated
     */
    public void updateTable(boolean scrollTop) {
        LOG.debug("Start updateTable");
        tableResult.updateTable(CompositionUtils.convertCompositionListToVector(compoResult, null, false, true, true, true, true),
                new SortKey(index.get(Index.SCORE), SortOrder.DESCENDING), scrollTop);
        PanelUtils.setWrappedLabel(countLabel, compoResult.size() + " résultats");
        LOG.debug("Start updateTable");
    }

    private void cleanAction() {
        LOG.debug("Start cleanAction");
        artist.setText(null);
        titre.setText(null);
        type.clearSelection();
        searchMethod.setSelectedItem(SearchMethod.CONTAINS.getValue());
        publi.getInput().setText("");
        fileName.setText("");
        author.setText(null);
        cat.clearSelection();
        sorted.setSelected(false);
        deleted.setSelected(false);
        topTen.setSelected(false);
        range.getFirst().setText("");
        range.getSecond().setText("");
        deleteLabel.setText("");
        countLabel.setText("");
        LOG.debug("End cleanAction");
    }

    @Override
    public void modifyCompositionAction(Vector<String> selectedRow) throws MajorException {
        if (tableResult.getModel().getSelected().size() > 1) {
            String msg = "Trop d'éléments sélectionnés";
            deleteLabel.setText(msg);
            LOG.debug(msg);
        } else {
            compoResult = PanelUtils.editCompositionAction(selectedRow, compoResult, SearchPanel.getIndex());
            updateTable(false);
        }
    }

    @Override
    public void splitCompositionAction(Vector<Object> selected) {
        LOG.debug("Start splitCompositionAction");
        if (tableResult.getModel().getSelected().size() > 1) {
            String msg = "Trop d'éléments sélectionnés";
            deleteLabel.setText(msg);
            LOG.debug(msg);
        } else {
            compoResult = PanelUtils.splitCompositionAction(selected, compoResult, SearchPanel.getIndex());
            updateTable(false);
        }
        LOG.debug("End splitCompositionAction");
    }

    /**
     * Pretends to make a search by artist.
     *
     * @param artist to search
     */
    public void searchProgrammatically(String artist) {
        LOG.debug("Start searchProgrammatically");
        cleanAction();
        this.artist.setText(artist);
        searchAction();
        updateTable(true);
        LOG.debug("End searchProgrammatically");
    }

    public JTable getTableResult() {
        return tableResult.getTable();
    }

    public List<Composition> getCompoResult() {
        return compoResult;
    }

    public static ColumnIndex getIndex() {
        return index;
    }

    @Override
    public JButton getActionButton() {
        return search;
    }
}
