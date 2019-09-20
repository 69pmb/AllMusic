/**
 *
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.panel.SearchPanel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront affichées pour l'onglet {@code SearchPanel}.
 *
 * @see {@link AbstractModel}
 * @author pmbroca
 */
public class SearchPanelModel extends AbstractModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de {@link SearchPanelModel}.
     *
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public SearchPanelModel(Object[][] data, String[] title) {
        super(data, title);
    }

    /**
     * Constructeur de {@link SearchPanelModel}.
     *
     * @param data {@link Vector} les données
     * @param title {@link Vector} les entetes du tableau
     */
    public SearchPanelModel(Vector<? extends Vector<?>> data, Vector<?> title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return i1 == SearchPanel.getIndex().get(Index.SELECTED);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class<?> result;
        if (Arrays.asList(SearchPanel.getIndex().get(Index.FILE_SIZE),
                SearchPanel.getIndex().get(Index.SCORE), SearchPanel.getIndex().get(Index.LINE_NUMBER),
                SearchPanel.getIndex().get(Index.DECILE)).contains(col)) {
            result = Integer.class;
        } else if (SearchPanel.getIndex().get(Index.SELECTED) == col) {
            result = Boolean.class;
        } else {
            result = String.class;
        }
        return result;
    }

    @Override
    public List<Object> getSelected() {
        List<Object> toReturn = new ArrayList<>();
        for (int i = 0 ; i < dataVector.size() ; i++) {
            if ((Boolean) ((Vector<?>) dataVector.elementAt(i)).elementAt(SearchPanel.getIndex().get(Index.SELECTED))) {
                toReturn.add(dataVector.elementAt(i));
            }
        }
        return toReturn;
    }
}
