package pmb.allmusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.MiscUtils;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.ModificationComposition;
import pmb.allmusic.view.dialog.DialogCompoTable;
import pmb.allmusic.view.dialog.ExceptionDialog;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.VariousUtils;

/**
 * Contextual Menu for compositions table.
 *
 * @see {@link JPopupMenu}
 */
public class CompositionPopupMenu extends PopupMenu {
  private static final Logger LOG = LogManager.getLogger(CompositionPopupMenu.class);

  /**
   * Constructor of {@link CompositionPopupMenu}.
   *
   * @param type Class type implementing {@link ModificationComposition}
   * @param dialogCompoTable to redirect to Fichier Panel
   * @param index column index of rows
   */
  @SuppressWarnings("unchecked")
  public CompositionPopupMenu(
      Class<? extends ModificationComposition> type,
      DialogCompoTable dialogCompoTable,
      ColumnIndex index) {
    super();
    LOG.debug("Start CompositionPopupMenu");

    // Copy clipboard artist and title
    buildCopyArtistAndTitleMenu(KeyEvent.VK_C, index.get(Index.ARTIST), index.get(Index.TITLE));
    // Copy clipboard artist
    buildCopySelectedRowFieldMenu(KeyEvent.VK_A, index.get(Index.ARTIST), "Copier l'artiste");
    // Copy clipboard title
    buildCopySelectedRowFieldMenu(KeyEvent.VK_T, index.get(Index.TITLE), "Copier le titre");
    ComponentBuilder.buildMenuItem(
        menu,
        "Rechercher sur Wikipedia",
        KeyEvent.VK_W,
        (ActionEvent e) -> {
          VariousUtils.openUrl(
              MiscUtils.wikipediaSearch(
                  (String) selectedRow.get(index.get(Index.TITLE)),
                  (String) selectedRow.get(index.get(Index.ARTIST))));
          this.setVisible(false);
        },
        null);
    if (type != null) {
      // Modify composition
      ComponentBuilder.buildMenuItem(
          menu,
          "Modifier la composition",
          KeyEvent.VK_E,
          (ActionEvent e) -> {
            LOG.debug("Start modifComposition");
            try {
              this.setVisible(false);
              type.cast(SwingUtilities.getAncestorOfClass(type, getTable()))
                  .modifyCompositionAction((Vector<String>) selectedRow);
            } catch (MajorException e1) {
              LOG.error("An exception has been thrown when editing composition: ", e1);
              new ExceptionDialog(
                      "An exception has been thrown when editing composition", e1.getMessage(), e1)
                  .setVisible(true);
            }
            LOG.debug("End modifComposition");
          },
          null);
      // Split selected composition
      ComponentBuilder.buildMenuItem(
          menu,
          "Diviser cette composition",
          KeyEvent.VK_D,
          (ActionEvent e) -> {
            LOG.debug("Start split");
            this.setVisible(false);
            type.cast(SwingUtilities.getAncestorOfClass(type, getTable()))
                .splitCompositionAction((Vector<Object>) selectedRow);
            LOG.debug("End split");
          },
          null);
    }
    // Redirection to Fichier Panel
    if (dialogCompoTable != null) {
      ComponentBuilder.buildMenuItem(
          menu,
          "Voir dans l'onglet Fichier",
          KeyEvent.VK_F,
          (ActionEvent e) -> {
            OngletPanel.setSelectTab(Constant.ONGLET_FICHIER);
            OngletPanel.getFichier()
                .searchProgrammatically(
                    dialogCompoTable.getFileName(),
                    VariousUtils.stringToUuids((String) selectedRow.get(index.get(Index.UUID))));
            dialogCompoTable.getParent().dispose();
            dialogCompoTable.dispose();
            LOG.debug("End redirect fichier");
          },
          null);
    }
    ComponentBuilder.buildMenuItem(
        menu,
        "Voir l'artiste dans l'onglet Recherche",
        KeyEvent.VK_S,
        (ActionEvent e) -> {
          OngletPanel.setSelectTab(Constant.ONGLET_SEARCH);
          OngletPanel.getSearch()
              .searchProgrammatically((String) selectedRow.get(index.get(Index.ARTIST)));
          LOG.debug("End redirect search");
        },
        null);

    LOG.debug("End CompositionPopupMenu");
  }
}
