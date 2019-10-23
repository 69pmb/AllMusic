package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.ModificationComposition;
import pmb.music.AllMusic.view.dialog.DialogCompoTable;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;
import pmb.music.AllMusic.view.panel.OngletPanel;

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
    public CompositionPopupMenu(Class<? extends ModificationComposition> type, DialogCompoTable dialogCompoTable, ColumnIndex index) {
        super();
        LOG.debug("Start CompositionPopupMenu");

        // Copy clipboard artist and title
        buildCopyArtistAndTitleMenu(KeyEvent.VK_C, index.get(Index.ARTIST), index.get(Index.TITLE));
        // Copy clipboard artist
        buildCopySelectedRowFieldMenu(KeyEvent.VK_A, index.get(Index.ARTIST), "Copier l'artiste");
        // Copy clipboard title
        buildCopySelectedRowFieldMenu(KeyEvent.VK_T, index.get(Index.TITLE), "Copier le titre");
        if (type != null) {
            // Modify composition
            ComponentBuilder.buildMenuItem(menu, "Modifier la composition", KeyEvent.VK_E, (ActionEvent e) -> {
                LOG.debug("Start modifComposition");
                try {
                    this.setVisible(false);
                    type.cast(SwingUtilities.getAncestorOfClass(type, getTable()))
                    .modifyCompositionAction((Vector<String>) selectedRow);
                } catch (MajorException e1) {
                    LOG.error("An exception has been thrown when editing composition: ", e1);
                    new ExceptionDialog("An exception has been thrown when editing composition",
                            e1.getMessage(), e1).setVisible(true);
                }
                LOG.debug("End modifComposition");
            }, null);
            // Split selected composition
            ComponentBuilder.buildMenuItem(menu, "Diviser cette composition", KeyEvent.VK_D, (ActionEvent e) -> {
                LOG.debug("Start split");
                try {
                    this.setVisible(false);
                    type.cast(SwingUtilities.getAncestorOfClass(type, getTable())).splitCompositionAction((Vector<Object>) selectedRow);
                } catch (MajorException e1) {
                    LOG.error("An exception has been thrown when splitting composition: ", e1);
                    new ExceptionDialog("An exception has been thrown when splitting composition",
                            e1.getMessage(), e1).setVisible(true);
                }
                LOG.debug("End split");
            }, null);
        }
        // Redirection to Fichier Panel
        if (dialogCompoTable != null) {
            ComponentBuilder.buildMenuItem(menu, "Voir dans l'onglet Fichier", KeyEvent.VK_F, (ActionEvent e) -> {
                OngletPanel.getOnglets().setSelectedIndex(OngletPanel.getTabIndex(Constant.ONGLET_FICHIER));
                OngletPanel.getFichier().searchProgrammatically(dialogCompoTable.getFileName(), MiscUtils.stringToUuids((String) selectedRow.get(index.get(Index.UUID))));
                dialogCompoTable.getParent().dispose();
                dialogCompoTable.dispose();
                LOG.debug("End redirect");
            }, null);
        }

        LOG.debug("End CompositionPopupMenu");
    }
}
