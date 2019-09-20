/**
 *
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.panel.ArtistPanel;

/**
 * Décrit la façon dont les données de l'onglet artiste seront affichées.
 *
 * @see {@link AbstractModel}
 */
public class ArtistModel extends AbstractModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur.
     *
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public ArtistModel(Object[][] data, String[] title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == ArtistPanel.getIndex().get(Index.ARTIST)) {
            return String.class;
        } else if (Arrays.asList(ArtistPanel.getIndex().get(Index.SCORE_TOTAL),ArtistPanel.getIndex().get(Index.SCORE_SONG),
                ArtistPanel.getIndex().get(Index.SCORE_ALBUM),ArtistPanel.getIndex().get(Index.SCORE_DELETED),
                ArtistPanel.getIndex().get(Index.DELETED)).contains(col)) {
            return Long.class;
        } else {
            return Integer.class;
        }
    }

    @Override
    public List<Object> getSelected() {
        return new ArrayList<>();
    }
}
