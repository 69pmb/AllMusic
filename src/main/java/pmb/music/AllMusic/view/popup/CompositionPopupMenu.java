package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.ModificationComposition;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;

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
     * @param index column index of rows
     */
    @SuppressWarnings("unchecked")
    public CompositionPopupMenu(Class<? extends ModificationComposition> type, ColumnIndex index) {
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
                } catch (MyException e1) {
                    LOG.error("An exception has been thrown when editing composition: ", e1);
                    ExceptionDialog ed = new ExceptionDialog("An exception has been thrown when editing composition",
                            e1.getMessage(), e1);
                    ed.setVisible(true);
                }
                LOG.debug("End modifComposition");
            }, null);
        }

        LOG.debug("End CompositionPopupMenu");
    }
}
