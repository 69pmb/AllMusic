/**
 *
 */
package pmb.allmusic.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.BooleanUtils;

import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.panel.FichierPanel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront affichées pour l'onglet {@code FichierPanel}.
 *
 * @see {@link AbstractModel}
 * @author pmbroca
 */
public class CompoFichierPanelModel extends AbstractModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de {@link CompoFichierPanelModel}.
     *
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public CompoFichierPanelModel(Object[][] data, String[] title) {
        super(data, title);
    }

    /**
     * Constructeur de {@link CompoFichierPanelModel}.
     *
     * @param data {@link Vector} les données
     * @param title {@link Vector} les entetes du tableau
     */
    public CompoFichierPanelModel(Vector<? extends Vector<?>> data, Vector<?> title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return i1 == FichierPanel.getCompositionindex().get(Index.SELECTED);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class<?> result;
        if (Arrays.asList(FichierPanel.getCompositionindex().get(Index.RANK),
                FichierPanel.getCompositionindex().get(Index.FILE_SIZE), FichierPanel.getCompositionindex().get(Index.LINE_NUMBER)).contains(col)) {
            result = Integer.class;
        } else if (FichierPanel.getCompositionindex().get(Index.SCORE) == col) {
            result = Long.class;
        } else if (FichierPanel.getCompositionindex().get(Index.SELECTED) == col) {
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
            if (BooleanUtils.isTrue((Boolean) ((Vector<?>) dataVector.elementAt(i))
                    .elementAt(FichierPanel.getCompositionindex().get(Index.SELECTED)))) {
                toReturn.add(dataVector.elementAt(i));
            }
        }
        return toReturn;
    }
}
