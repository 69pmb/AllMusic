package pmb.music.AllMusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
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
import pmb.music.AllMusic.view.dialog.ExceptionDialog;
import pmb.music.AllMusic.view.model.SearchPanelModel;
import pmb.music.AllMusic.view.popup.CompositionPopupMenu;

/**
 * Gère le panel search.
 *
 * @author pmbroca
 */
public class SearchPanel extends JPanel implements ModificationComposition {

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
                updateTable();
                deleteLabel.setText(selected.size() + " élément(s) supprimé(s)");
            } catch (MyException e1) {
                LOG.error("Error when deleting compositions in Search result", e1);
                deleteLabel.setText("<html>" + e1.getMessage() + "</html>");
            }
        });
        top.add(delete);

        // Modif Btn
        JButton modif = ComponentBuilder.buildJButton("Modifier la composition sélectionnée", 300, Constant.ICON_EDIT);
        modif.addActionListener((ActionEvent e) -> {
            try {
                modifyCompositionAction((Vector<String>) tableResult.getModel().getSelected().get(0));
            } catch (MyException e2) {
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
        csv.addActionListener((ActionEvent e) -> {
            List<String> c = Arrays
                    .asList(publi.getInput().getText(), range.getFirst().getText(), range.getSecond().getText(),
                            fileName.getText(),
                            searchMethod.getSelectedItem() == null ? "" : searchMethod.getSelectedItem().toString(),
                                    cat.getSelectedItems(), type.getSelectedItems(), titre.getText(), artist.getText(),
                                    author.getText(), "Sorted:" + Boolean.toString(sorted.isSelected()),
                                    "Deleted:" + Boolean.toString(deleted.isSelected()),
                                    "Top Ten:" + Boolean.toString(topTen.isSelected()))
                    .stream().filter(s -> !"".equals(s)).collect(Collectors.toList());
            String criteres = StringUtils.join(c, " ");
            LinkedList<String> csvHeader = new LinkedList<>(Arrays.asList(title));
            csvHeader.add("Critères: " + criteres);
            String name = CsvFile.exportCsv("search", PanelUtils.convertDataVectorToList(tableResult.getTable()), null,
                    csvHeader.toArray(new String[title.length + 1]));
            try {
                FichierUtils.openFileInExcel(name);
            } catch (MyException e1) {
                LOG.error("Erreur de l'ouverture avec excel du fichier: " + name, e1);
            }
        });
        top.add(csv);
        header.add(top);
        LOG.debug("End initButtons");
    }

    /**
     * Initialise les champs de recherche.
     *
     * @param header le header de l'onglet
     */
    @SuppressWarnings("unchecked")
    private void initSearchFields(JPanel header) {
        LOG.debug("Start initSearchFields");
        JPanel searchFields = new JPanel();
        searchFields.setLayout(new GridLayout(2, 5));

        // Artiste
        artist = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(searchFields)
                .withValues(OngletPanel.getArtistList()).withLabel("Artiste : ").withPanelWidth(300)
                .withComponentWidth(150).withLabelWidth(200).build();
        // Titre
        titre = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(searchFields)
                .withValues(OngletPanel.getTitleList()).withLabel("Titre : ").withPanelWidth(300)
                .withComponentWidth(150).withLabelWidth(180).build();
        // SearchMethod
        searchMethod = (JComboBox<String>) new ComponentBuilder<String>(JComboBox.class).withParent(searchFields)
                .withPanelWidth(200).withLabel("Méthode de recherche : ")
                .withValues(MiscUtils.getEnumValues(SearchMethod.values(), SearchMethod::getValue))
                .withComponentWidth(150).withLabelWidth(150).build();
        // Nom du fichier
        fileName = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(searchFields)
                .withLabel("Nom du fichier : ").withPanelWidth(250).withComponentWidth(150).withLabelWidth(250).build();
        // Auteur
        author = (MyInputText) new ComponentBuilder<String>(MyInputText.class).withParent(searchFields)
                .withValues(OngletPanel.getAuthorList()).withLabel("Auteur : ").withPanelWidth(150)
                .withFilterContains(true).withComponentWidth(150).withLabelWidth(140).build();
        // Type
        type = (JComboCheckBox) new ComponentBuilder<String>(JComboCheckBox.class).withParent(searchFields)
                .withValues(MiscUtils.getEnumValues(RecordType.values(), RecordType::getRecordType))
                .withLabel("Type : ").withPanelWidth(180).withComponentWidth(150).withLabelWidth(180).build();
        // Range
        range = (MyInputRange) new ComponentBuilder<String>(MyInputRange.class).withParent(searchFields)
                .withLabel("Année(s) du classement : ").withPanelWidth(250).withComponentWidth(100).withLabelWidth(200)
                .withFlowLayout(true).build();
        // Categorie
        cat = (JComboCheckBox) new ComponentBuilder<String>(JComboCheckBox.class).withParent(searchFields)
                .withValues(MiscUtils.getEnumValues(Cat.values(), Cat::getCat))
                .withLabel("Catégorie : ").withPanelWidth(180).withComponentWidth(150).withLabelWidth(150).build();
        // Publi
        publi = (JComboBoxInput<String>) new ComponentBuilder<String>(JComboBoxInput.class).withParent(searchFields)
                .withValues(MiscUtils.getEnumValues(SearchRange.values(), SearchRange::getValue))
                .withLabel("Année de publication : ").withPanelWidth(230).withComponentWidth(100).withLabelWidth(240)
                .build();
        // inFiles
        inFiles = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(searchFields)
                .withLabel("Rechercher dans les fichiers : ").withInitialValue(true).withPanelWidth(200)
                .withComponentWidth(150).withLabelWidth(150).build();
        // Sorted
        sorted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(searchFields)
                .withLabel("Trié : ").withPanelWidth(200).withComponentWidth(150).withLabelWidth(150).build();
        // Deleted
        deleted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(searchFields)
                .withLabel("Supprimé : ").withPanelWidth(200).withComponentWidth(150).withLabelWidth(150).build();
        // TopTen
        topTen = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(searchFields)
                .withLabel("Top 10 : ").withPanelWidth(200).withComponentWidth(150).withLabelWidth(150).build();
        // Nombre de résultat
        countLabel = (JLabel) new ComponentBuilder<String>(JLabel.class).withParent(searchFields).withLabel("")
                .withLabelWidth(200).withColor(new Color(8, 187, 81)).withFontSize(25).build();
        // Nombre de suppression
        deleteLabel = (JLabel) new ComponentBuilder<String>(JLabel.class).withParent(searchFields).withLabel("")
                .withFlowLayout(true).withLabelWidth(230).withColor(new Color(8, 187, 81)).withFontSize(15).build();

        header.add(searchFields);
        LOG.debug("End initSearchFields");
    }

    private void initTable() {
        LOG.debug("Start initTable");
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        try {
            tableResult = new TableBuilder().withModelAndData(null, title, SearchPanelModel.class)
                    .withRowSorterListenerDelete(SearchPanel.getIndex())
                    .withMouseClickAction(e -> {
                        Optional<Vector<String>> row = PanelUtils.getSelectedRow((JTable) e.getSource(), e.getPoint());
                        tableResult.getPopupMenu().initDataAndPosition(e, row.orElse(null));
                        if (!row.isPresent()) {
                            return;
                        }
                        if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                            LOG.debug("Start result mouse");
                            // Ouvre une popup pour afficher les fichiers de la
                            // composition sélectionnée
                            DialogFileTable pop = new DialogFileTable(null, "Fichier", true,
                                    CompositionUtils.findByUuid(compoResult, MiscUtils.stringToUuids(row.get().get(SearchPanel.getIndex().get(Index.UUID))))
                                    .map(c -> new LinkedList<>(Arrays.asList(c))).orElse(new LinkedList<>()),
                                    400, new RowSorter.SortKey(DialogFileTable.getIndex().get(Index.SCORE), SortOrder.DESCENDING));
                            pop.showDialogFileTable();
                            LOG.debug("End result mouse");
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            tableResult.getPopupMenu().show(e);
                        }
                    }).withPopupMenu(new CompositionPopupMenu(this.getClass(), SearchPanel.getIndex()))
                    .withKeyListener().build();
        } catch (MyException e1) {
            LOG.error("An error occured when init search table", e1);
            countLabel.setText(e1.getMessage());
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
            updateTable();
        }
        LOG.debug("End searchAction");
    }

    /**
     * Updates {@link SearchPanel} table data and handles sorting and rendering.
     */
    public void updateTable() {
        LOG.debug("Start updateTable");
        tableResult.getModel().setRowCount(0);
        tableResult.getModel().setDataVector(
                CompositionUtils.convertCompositionListToVector(compoResult, null, false, true, true, true, true),
                new Vector<>(Arrays.asList(title)));
        countLabel.setText(compoResult.size() + " résultats");
        if (tableResult.getSortedColumn() == null) {
            tableResult.setSortedColumn(SearchPanel.getIndex().get(Index.SCORE));
            tableResult.setSortOrder(SortOrder.DESCENDING);
        }
        tableResult.getRowSorter().setSortKeys(Collections
                .singletonList(new RowSorter.SortKey(tableResult.getSortedColumn(), tableResult.getSortOrder())));
        PanelUtils.colRenderer(tableResult.getTable(), false, SearchPanel.getIndex());
        for (int i = 0 ; i < tableResult.getRowCount() ; i++) {
            tableResult.setValueAt(i + 1, i, SearchPanel.getIndex().get(Index.LINE_NUMBER));
        }
        tableResult.setSelectedRow(-1);
        tableResult.getColumnModel().getColumn(SearchPanel.getIndex().get(Index.LINE_NUMBER)).setMinWidth(40);
        tableResult.getColumnModel().getColumn(SearchPanel.getIndex().get(Index.LINE_NUMBER)).setMaxWidth(40);
        tableResult.removeColumn(tableResult.getColumnModel().getColumn(SearchPanel.getIndex().get(Index.DECILE)));
        tableResult.removeColumn(tableResult.getColumnModel().getColumn(SearchPanel.getIndex().get(Index.DELETED) - 1));
        tableResult.removeColumn(tableResult.getColumnModel().getColumn(SearchPanel.getIndex().get(Index.UUID) - 2));
        tableResult.getModel().fireTableDataChanged();
        tableResult.getTable().repaint();
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
    public void modifyCompositionAction(Vector<String> selectedRow) throws MyException {
        if (tableResult.getModel().getSelected().size() > 1) {
            String msg = "Trop d'éléments sélectionnés";
            deleteLabel.setText(msg);
            LOG.debug(msg);
        } else {
            compoResult = PanelUtils.editCompositionAction(selectedRow, compoResult, SearchPanel.getIndex());
            updateTable();
        }
    }

    @Override
    public void splitCompositionAction(Vector<Object> selected) throws MyException {
        LOG.debug("Start splitCompositionAction");
        if (tableResult.getModel().getSelected().size() > 1) {
            String msg = "Trop d'éléments sélectionnés";
            deleteLabel.setText(msg);
            LOG.debug(msg);
        } else {
            compoResult = PanelUtils.splitCompositionAction(selected, compoResult, SearchPanel.getIndex());
            updateTable();
        }
        LOG.debug("End splitCompositionAction");
    }

    public JButton getSearch() {
        return search;
    }

    public void setSearch(JButton search) {
        this.search = search;
    }

    public JTable getTableResult() {
        return tableResult.getTable();
    }

    public List<Composition> getCompoResult() {
        return compoResult;
    }

    public void setCompoResult(List<Composition> compoResult) {
        this.compoResult = compoResult;
    }

    public static ColumnIndex getIndex() {
        return index;
    }
}
