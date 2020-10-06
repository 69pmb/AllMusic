package pmb.allmusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.exception.MajorException;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.panel.FichierPanel;

/**
 * Contextual Menu for {@link FichierPanel}.
 *
 * @see {@link JPopupMenu}
 * @author pmbroca
 */
public class FichierPopupMenu extends PopupMenu {
    private static final Logger LOG = LogManager.getLogger(FichierPopupMenu.class);

    /**
     * Constructor of {@link FichierPopupMenu}.
     *
     * @param index {@link ColumnIndex} in rows
     */
    @SuppressWarnings("unchecked")
    public FichierPopupMenu(ColumnIndex index) {
        super();
        LOG.debug("Start FichierPopupMenu");

        // Open XML file
        buildOpenXmlFileMenu(KeyEvent.VK_X, index.get(Index.FILE_NAME), null);
        // Open TXT file
        buildOpenTxtFileMenu(KeyEvent.VK_T, index);
        // Copy clipboard file name
        buildCopySelectedRowFieldMenu(KeyEvent.VK_C, index.get(Index.FILE_NAME), "Copier le nom du fichier");
        // Modify file
        ComponentBuilder.buildMenuItem(menu, "Modifier le fichier", KeyEvent.VK_E, (ActionEvent e) -> {
            LOG.debug("Start modifFile");
            try {
                ((FichierPanel) SwingUtilities.getAncestorOfClass(FichierPanel.class, getTable()))
                .modifyFichierAction((Vector<String>) selectedRow);
            } catch (MajorException e1) {
                LOG.error("Error when editing file: {}", selectedRow, e1);
            }
            this.setVisible(false);
            LOG.debug("End modifFile");
        }, null);

        LOG.debug("End FichierPopupMenu");
    }
}
