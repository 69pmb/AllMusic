/** */
package pmb.allmusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.JScrollPane;
import javax.swing.RowSorter.SortKey;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.utils.CompositionUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FichierUtils;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.PanelUtils;
import pmb.allmusic.view.TableBuilder;
import pmb.allmusic.view.model.FichierDialogModel;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.allmusic.view.popup.DialogFilePopupMenu;
import pmb.allmusic.xml.ExportXML;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.VariousUtils;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Fichier}.
 *
 * @see {@link JDialog}
 */
public class DialogFileTable extends AbstractFilterDialog<Composition> {
  private static final Logger LOG = LogManager.getLogger(DialogFileTable.class);

  private static final String[] header = {
    "Artiste",
    "Oeuvre",
    "Type",
    "Auteur",
    "Nom du fichier",
    "Date de publication",
    "Categorie",
    "Dates",
    "Supprimés",
    "Taille",
    "Score",
    "Classement",
    "",
    "",
    "Classé"
  };

  private static final ColumnIndex index =
      new ColumnIndex()
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

  /**
   * Constructeur de {@link DialogFileTable}.
   *
   * @param header {@link String} les entetes de la popup
   * @param compoList {@code List<Composition>} la liste des compositions dont les fichiers seront
   *     affichés
   * @param height la hauteur de la popup
   * @param defaultSort sorted column at start
   */
  public DialogFileTable(
      String header, List<Composition> compoList, int height, SortKey defaultSort) {
    super(
        header,
        new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height),
        compoList,
        Predicate.not(Composition::isDeleted),
        list -> FichierUtils.convertCompositionListToFichierVector(list, true, false),
        defaultSort,
        "Filtrer les compositions supprimées");
    LOG.debug("Start DialogFileTable");
    initComposants();
    LOG.debug("End DialogFileTable");
  }

  @Override
  protected void initComposants() {
    LOG.debug("Start initComponent");
    try {
      setMyTable(
          new TableBuilder()
              .withModelAndData(null, header, FichierDialogModel.class)
              .withColumnIndex(index)
              .withDefaultRowSorterListener()
              .withMouseClickedActions((e, selectedRow) -> leftMouseClickAction(selectedRow), true)
              .withPopupMenu(new DialogFilePopupMenu(this, DialogFileTable.getIndex()))
              .withKeyListener()
              .build());
      updateTableData();

      getDialog().setLayout(new BorderLayout());
      getDialog().add(new JScrollPane(getMyTable().getTable()), BorderLayout.CENTER);
    } catch (MajorException e1) {
      LOG.error("An error occured when init Dialog File table", e1);
      return;
    }
    LOG.debug("End initComponent");
  }

  private void leftMouseClickAction(Vector<String> selectedRow) {
    LOG.debug("Start leftMouseClickAction");
    // Double click gauche -> Ouvre une popup pour afficher les compositions du
    // fichier sélectionné
    String fileName = selectedRow.get(DialogFileTable.getIndex().get(Index.FILE_NAME));
    List<Composition> compo =
        ImportXML.importXML(Constant.getFinalFilePath()).stream()
            .filter(
                c ->
                    c.getFiles().stream()
                        .map(Fichier::getFileName)
                        .anyMatch(f -> StringUtils.contains(f, fileName)))
            .collect(Collectors.toList());
    List<String> title = new ArrayList<>();
    if (!compo.isEmpty()) {
      Fichier file = compo.get(0).getFiles().get(0);
      title =
          Arrays.asList(
              "FileName:",
              file.getFileName(),
              "PublishYear:",
              String.valueOf(file.getPublishYear()),
              "Categorie:",
              file.getCategorie().getValue(),
              "RangeDateBegin:",
              String.valueOf(file.getRangeDateBegin()),
              "RangeDateEnd:",
              String.valueOf(file.getRangeDateEnd()),
              "Sorted:",
              String.valueOf(file.getSorted()),
              "Size:",
              String.valueOf(file.getSize()));
    } else {
      LOG.warn("{} empty !", fileName);
    }
    new DialogCompoTable(getDialog(), StringUtils.join(title, " / "), compo, fileName, 800).show();
    LOG.debug("End leftMouseClickAction");
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
    List<String> uuid =
        VariousUtils.stringToUuids(
            (String) selected.get(DialogFileTable.getIndex().get(Index.UUID)));
    List<Composition> xmlFile =
        ImportXML.importXML(
            FilesUtils.buildXmlFilePath(fileName)
                .orElseThrow(() -> new MajorException("File doesn't exist: " + fileName)));
    Composition edited =
        CompositionUtils.findByUuid(xmlFile, uuid)
            .orElseThrow(() -> new MajorException("Can't find edited composition: " + selected));
    List<Fichier> files =
        CompositionUtils.findByUuid(importXML, uuid)
            .map(Composition::getFiles)
            .orElse(new ArrayList<>());
    // Lancement de la popup de modification
    ModifyCompositionDialog md =
        new ModifyCompositionDialog(
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
    edited.setRecordType(
        RecordType.valueOf(editedRow.get(DialogFileTable.getIndex().get(Index.TYPE))));
    edited.setDeleted(
        Boolean.valueOf(editedRow.get(DialogFileTable.getIndex().get(Index.DELETED))));

    // Remove edited fichier in final file
    PanelUtils.removeFichierFromComposition(
        importXML, uuid, FichierUtils.filterFichier(selected, DialogFileTable.getIndex()));
    // Remove edited fichier in displayed list
    PanelUtils.removeFichierFromComposition(
        getData(), uuid, FichierUtils.filterFichier(selected, DialogFileTable.getIndex()));

    Optional<Composition> compoExist = ImportXML.findAndMergeComposition(importXML, edited, true);
    if (compoExist.isPresent()) {
      LOG.debug("La compo existe déjà, on regroupe");
      // Liste des compositions affichées
      ImportXML.findAndMergeComposition(getData(), edited, false);
    }

    if (StringUtils.equals(OngletPanel.getSelectTabTitle(), Constant.ONGLET_SEARCH)) {
      LOG.debug("Updates search panel data");
      List<Composition> searchPanelCompo = OngletPanel.getSearch().getCompoResult();
      if (CompositionUtils.findByUuid(searchPanelCompo, uuid).isPresent()) {
        PanelUtils.removeFichierFromComposition(
            searchPanelCompo,
            uuid,
            FichierUtils.filterFichier(selected, DialogFileTable.getIndex()));
      }
      ImportXML.findAndMergeComposition(searchPanelCompo, edited, true);
      OngletPanel.getSearch().updateTable(false);
    }

    // Updates fichier panel data
    compoExist.ifPresent(c -> files.addAll(c.getFiles()));
    OngletPanel.getFichier().reprocessSpecificFichier(importXML, files);

    try {
      ExportXML.exportXML(importXML, Constant.getFinalFile());
      OngletPanel.getArtist().updateArtistData();
    } catch (MajorException e1) {
      LOG.error("Erreur lors de l'export du fichier final !!", e1);
    }

    try {
      ExportXML.exportXML(xmlFile, fileName);
    } catch (MajorException e) {
      LOG.error(
          "Erreur lors de la modification d'une composition dans le fichier: {}", fileName, e);
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
