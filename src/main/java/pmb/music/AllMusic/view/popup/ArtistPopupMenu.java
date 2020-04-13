package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JPopupMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Contextual Menu for artist panel table.
 *
 * @see {@link JPopupMenu}
 * @author pmbroca
 */
public class ArtistPopupMenu extends PopupMenu {
    private static final Logger LOG = LogManager.getLogger(ArtistPopupMenu.class);

    /**
     * Constructor of {@link ArtistPopupMenu}.
     * @param index column index of rows
     */
    public ArtistPopupMenu(ColumnIndex index) {
        super();
        LOG.debug("Start ArtistPopupMenu");

        // Copy clipboard artist
        buildCopySelectedRowFieldMenu(KeyEvent.VK_A, index.get(Index.ARTIST), "Copier l'artiste");
        // Wikipedia search
        ComponentBuilder.buildMenuItem(menu, "Rechercher sur Wikipedia", KeyEvent.VK_W, (ActionEvent e) -> {
            String selectedArtist = (String) selectedRow.get(index.get(Index.ARTIST));
            MiscUtils.openUrl(MiscUtils.wikipediaSearch(selectedArtist, selectedArtist));
            this.setVisible(false);
        }, null);
        // Redirection to Search Panel
        ComponentBuilder.buildMenuItem(menu, "Voir l'artiste dans l'onglet Recherche", KeyEvent.VK_S, (ActionEvent e) -> {
            OngletPanel.setSelectTab(Constant.ONGLET_SEARCH);
            OngletPanel.getSearch().searchProgrammatically((String) selectedRow.get(index.get(Index.ARTIST)));
            LOG.debug("End redirect search");
        }, null);

        LOG.debug("End ArtistPopupMenu");
    }
}
