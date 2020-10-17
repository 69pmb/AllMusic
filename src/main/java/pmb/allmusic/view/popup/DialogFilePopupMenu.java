package pmb.allmusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.utils.Constant;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.dialog.DialogFileTable;
import pmb.allmusic.view.dialog.ExceptionDialog;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.my.starter.exception.MajorException;

/**
 * Contextual Menu for {@link DialogFileTable}.
 *
 * @see {@link PopupMenu}
 */
public class DialogFilePopupMenu extends PopupMenu {
    private static final Logger LOG = LogManager.getLogger(DialogFilePopupMenu.class);

    /**
     * Constructor of {@link DialogFilePopupMenu}.
     *
     * @param dialogFileTable dialog file
     * @param index column index of torws
     */
    @SuppressWarnings("unchecked")
    public DialogFilePopupMenu(DialogFileTable dialogFileTable, ColumnIndex index) {
        super();
        LOG.debug("Start DialogFilePopupMenu");

        // Open XML file
        buildOpenXmlFileMenu(KeyEvent.VK_X, index.get(Index.FILE_NAME), index.get(Index.RANK));
        // Open TXT file
        buildOpenTxtFileMenu(KeyEvent.VK_T, index);
        // Copy clipboard artist and title
        buildCopyArtistAndTitleMenu(KeyEvent.VK_C, index.get(Index.ARTIST), index.get(Index.TITLE));
        // Copy clipboard artist
        buildCopySelectedRowFieldMenu(KeyEvent.VK_A, index.get(Index.ARTIST), "Copier l'artiste");
        // Copy clipboard title
        buildCopySelectedRowFieldMenu(KeyEvent.VK_R, index.get(Index.TITLE), "Copier le titre");
        // Copy clipboard file name
        buildCopySelectedRowFieldMenu(KeyEvent.VK_C, index.get(Index.FILE_NAME), "Copier le nom du fichier");
        // Edition of selected composition
        ComponentBuilder.buildMenuItem(menu, "Modifier cette composition", KeyEvent.VK_E, (ActionEvent e) -> {
            try {
                this.setVisible(false);
                dialogFileTable.editCompositionAction((Vector<Object>) selectedRow);
            } catch (MajorException e1) {
                LOG.error("An exception has been thrown when editing composition: ", e1);
                new ExceptionDialog("An exception has been thrown when editing composition",
                        e1.getMessage(), e1).setVisible(true);
            }
            LOG.debug("End edit");
        }, null);
        // Redirection to Fichier Panel
        ComponentBuilder.buildMenuItem(menu, "Voir dans l'onglet Fichier", KeyEvent.VK_F, (ActionEvent e) -> {
            this.setVisible(false);
            OngletPanel.setSelectTab(Constant.ONGLET_FICHIER);
            OngletPanel.getFichier().searchProgrammatically((String) selectedRow.get(index.get(Index.FILE_NAME)), Collections.emptyList());
            dialogFileTable.dispose();
            LOG.debug("End redirect");
        }, null);

        LOG.debug("End DialogFilePopupMenu");
    }
}
