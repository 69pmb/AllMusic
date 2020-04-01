/**
 *
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.FichierDialogModel;
import pmb.music.AllMusic.view.panel.OngletPanel;
import pmb.music.AllMusic.view.popup.DialogFilePopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Fichier}.
 *
 * @see {@link JDialog}
 * @author pmbroca
 */
public class DialogFileTable extends AbstractFilterDialog<Composition> {
    private static final Logger LOG = LogManager.getLogger(DialogFileTable.class);

    private static final String[] header = { "Artiste", "Oeuvre", "Type", "Auteur", "Nom du fichier",
            "Date de publication", "Categorie", "Dates", "Supprimés", "Taille", "Score", "Classement", "", "",
    "Classé" };

    private static final ColumnIndex index = new ColumnIndex()
            .put(Index.ARTIST, 0)
            .put(Index.TITLE, 1)
            .put(Index.TYPE, 2)
            .put(Index.AUTHOR, 3)
            .put(Index.FILE_NAME, 4)
            .put(Index.PUBLISH, 5)
            .put(Index.CAT, 6)
            .put(Index.DELETED, 7)
            .put(Index.PERCENT_DELETED, 8)
            .put(Index.FILE_SIZE, 9)
            .put(Index.SCORE, 10)
            .put(Index.RANK, 11)
            .put(Index.DELETED, 12)
            .put(Index.UUID, 13)
            .put(Index.SORTED, 14);

    private SortKey defaultSort;
    private MyTable fichiers;

    /**
     * Constructeur de {@link DialogFileTable}.
     *
     * @param header {@link String} les entetes de la popup
     * @param compoList {@code List<Composition>} la liste des compositions dont les
     *            fichiers seront affichés
     * @param height la hauteur de la popup
     * @param defaultSort sorted column at start
     */
    public DialogFileTable(String header, List<Composition> compoList, int height, SortKey defaultSort) {
        super(header, new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height), true, compoList,
                Predicate.not(Composition::isDeleted),
                list -> FichierUtils.convertCompositionListToFichierVector(list, true, false),
                "Filtrer les compositions supprimées");
        LOG.debug("Start DialogFileTable");
        this.defaultSort = defaultSort;
        initComposants();
        LOG.debug("End DialogFileTable");
    }

    @Override
    protected void initComposants() {
        LOG.debug("Start initComponent");
        try {
            fichiers = new TableBuilder().withModelAndData(null, header, FichierDialogModel.class)
                    .withColumnIndex(index)
                    .withDefaultRowSorterListener().withMouseClickAction(e -> {
                        Optional<Vector<String>> selectedRow = PanelUtils.getSelectedRowByPoint((JTable) e.getSource(),
                                e.getPoint());
                        fichiers.getPopupMenu().initDataAndPosition(e, selectedRow.orElse(null));
                        if (!selectedRow.isPresent()) {
                            return;
                        }
                        if (SwingUtilities.isRightMouseButton(e)) {
                            fichiers.getPopupMenu().show(e);
                        } else if (e.getClickCount() == 2 && (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
                            LOG.debug("Start double right mouse");
                            // Double click gauche -> Ouvre une popup pour afficher les compositions du
                            // fichier sélectionné
                            String fileName = selectedRow.get().get(DialogFileTable.getIndex().get(Index.FILE_NAME));
                            List<Composition> compo = ImportXML.importXML(Constant.getFinalFilePath()).stream()
                                    .filter(c -> c.getFiles().stream().map(Fichier::getFileName)
                                            .anyMatch(f -> StringUtils.contains(f, fileName)))
                                    .collect(Collectors.toList());
                            List<String> title = new ArrayList<>();
                            if (!compo.isEmpty()) {
                                Fichier file = compo.get(0).getFiles().get(0);
                                title = Arrays.asList("FileName:", file.getFileName(), "PublishYear:",
                                        String.valueOf(file.getPublishYear()), "Categorie:",
                                        file.getCategorie().getValue(), "RangeDateBegin:",
                                        String.valueOf(file.getRangeDateBegin()), "RangeDateEnd:",
                                        String.valueOf(file.getRangeDateEnd()), "Sorted:",
                                        String.valueOf(file.getSorted()), "Size:", String.valueOf(file.getSize()));
                            } else {
                                LOG.warn("{} empty !", fileName);
                            }
                            DialogCompoTable pop = new DialogCompoTable(getDialog(), StringUtils.join(title, " / "),
                                    compo, fileName, 800);
                            pop.show();
                            LOG.debug("End double right mouse");
                        }
                    }).withPopupMenu(new DialogFilePopupMenu(this, DialogFileTable.getIndex())).withKeyListener()
                    .build();
            updateTableData();

            getDialog().setLayout(new BorderLayout());
            getDialog().add(new JScrollPane(fichiers.getTable()), BorderLayout.CENTER);
        } catch (MajorException e1) {
            LOG.error("An error occured when init Dialog File table", e1);
            return;
        }

        LOG.debug("End initComponent");
    }

    @Override
    public void updateTable(Vector<Vector<Object>> list) {
        fichiers.getModel().setRowCount(0);
        fichiers.getModel().setDataVector(new Vector<>(list), new Vector<>(Arrays.asList(header)));
        fichiers.getRowSorter().setSortKeys(Collections.singletonList(defaultSort));
        ((TableRowSorter<?>) fichiers.getRowSorter())
        .setComparator(DialogFileTable.getIndex().get(Index.PERCENT_DELETED), MiscUtils.comparePercentage);
        PanelUtils.colRenderer(fichiers.getTable(), true, DialogFileTable.getIndex());
        fichiers.removeColumn(fichiers.getColumnModel().getColumn(DialogFileTable.getIndex().get(Index.DELETED)));
        fichiers.removeColumn(fichiers.getColumnModel().getColumn(DialogFileTable.getIndex().get(Index.UUID) - 1));
    }

    /**
     * Launchs a dialog to edit the selected composition.
     *
     * @param selected the selected row representing a composition
     * @throws MajorException if something went wrong
     */
    public void editCompositionAction(Vector<Object> selected) throws MajorException {
        LOG.debug("Start editCompositionAction");
        OngletPanel.getArtist().interruptUpdateArtist(true);
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        // On récupère la composition à modifier
        String fileName = (String) selected.get(DialogFileTable.getIndex().get(Index.FILE_NAME));
        List<String> uuid = MiscUtils.stringToUuids((String) selected.get(DialogFileTable.getIndex().get(Index.UUID)));
        List<Composition> xmlFile = ImportXML.importXML(FilesUtils.buildXmlFilePath(fileName)
                .orElseThrow(() -> new MajorException("File doesn't exist: " + fileName)));
        Composition edited = CompositionUtils.findByUuid(xmlFile, uuid)
                .orElseThrow(() -> new MajorException("Can't find edited composition: " + selected));
        List<Fichier> files = CompositionUtils.findByUuid(importXML, uuid).map(Composition::getFiles)
                .orElse(new ArrayList<Fichier>());
        // Lancement de la popup de modification
        ModifyCompositionDialog md = new ModifyCompositionDialog(
                selected.stream().map(Object::toString).collect(Collectors.toCollection(Vector::new)),
                DialogFileTable.getIndex());
        md.show();
        Vector<String> editedRow;
        if (BooleanUtils.isTrue(md.getSendData())) {
            // On recupère la compo si elle a bien été modifiée
            LOG.debug("Composition modifiée");
            editedRow = md.getCompo();
        } else {
            LOG.debug("Aucune modification");
            return;
        }

        // Update composition from dialog
        edited.setArtist(editedRow.get(DialogFileTable.getIndex().get(Index.ARTIST)));
        edited.setTitre(editedRow.get(DialogFileTable.getIndex().get(Index.TITLE)));
        edited.setRecordType(RecordType.valueOf(editedRow.get(DialogFileTable.getIndex().get(Index.TYPE))));
        edited.setDeleted(Boolean.valueOf(editedRow.get(DialogFileTable.getIndex().get(Index.DELETED))));

        // Remove edited fichier in final file
        PanelUtils.removeFichierFromComposition(importXML, uuid,
                FichierUtils.filterFichier(selected, DialogFileTable.getIndex()));
        // Remove edited fichier in displayed list
        PanelUtils.removeFichierFromComposition(getData(), uuid,
                FichierUtils.filterFichier(selected, DialogFileTable.getIndex()));

        Optional<Composition> compoExist = ImportXML.findAndMergeComposition(importXML, edited, true);
        if (compoExist.isPresent()) {
            LOG.debug("La compo existe déjà, on regroupe");
            // Liste des compositions affichées
            ImportXML.findAndMergeComposition(getData(), edited, false);
        }

        if (OngletPanel.getOnglets().getSelectedIndex() == 0) {
            LOG.debug("Updates search panel data");
            List<Composition> searchPanelCompo = OngletPanel.getSearch().getCompoResult();
            if (CompositionUtils.findByUuid(searchPanelCompo, uuid).isPresent()) {
                PanelUtils.removeFichierFromComposition(searchPanelCompo, uuid,
                        FichierUtils.filterFichier(selected, DialogFileTable.getIndex()));
            }
            ImportXML.findAndMergeComposition(searchPanelCompo, edited, true);
            OngletPanel.getSearch().updateTable();
        }

        // Updates fichier panel data
        compoExist.ifPresent(c -> files.addAll(c.getFiles()));
        OngletPanel.getFichier().reprocessSpecificFichier(importXML, files);

        try {
            ExportXML.exportXML(importXML, Constant.getFinalFile());
            OngletPanel.getArtist().updateArtistPanel();
        } catch (MajorException e1) {
            LOG.error("Erreur lors de l'export du fichier final !!", e1);
        }

        try {
            ExportXML.exportXML(xmlFile, fileName);
        } catch (MajorException e) {
            LOG.error("Erreur lors de la modification d'une composition dans le fichier: {}", fileName, e);
        }

        // Update dialog
        updateData(getData());
        updateTableData();
        LOG.debug("End editCompositionAction");
    }

    public static ColumnIndex getIndex() {
        return index;
    }
}
