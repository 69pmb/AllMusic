/**
 *
 */
package pmb.allmusic.view.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.panel.FichierPanel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront affichées pour l'onglet {@code FichierPanel}.
 *
 * @see {@link DefaultTableModel}
 * @author pmbroca
 */
public class FichierPanelModel extends AbstractModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de {@link FichierPanelModel}.
     *
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public FichierPanelModel(Object[][] data, String[] title) {
        super(data, title);
    }

    /**
     * Constructeur de {@link FichierPanelModel}.
     *
     * @param data {@link Vector} les données
     * @param title {@link Vector} les entetes du tableau
     */
    public FichierPanelModel(Vector<? extends Vector<?>> data, Vector<?> title) {
        super(data, title);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class<?> result;
        if (Arrays.asList(FichierPanel.getFichierindex().get(Index.PUBLISH),
                FichierPanel.getFichierindex().get(Index.FILE_SIZE),
                FichierPanel.getFichierindex().get(Index.LINE_NUMBER)).contains(col)) {
            result = Integer.class;
        } else if (col == FichierPanel.getFichierindex().get(Index.CREATE_DATE)) {
            result = LocalDateTime.class;
        } else if (Arrays.asList(FichierPanel.getFichierindex().get(Index.PERCENT_DELETED),
                FichierPanel.getFichierindex().get(Index.SCORE),
                FichierPanel.getFichierindex().get(Index.SCORE_DELETED)).contains(col)) {
            result = Double.class;
        } else {
            result = String.class;
        }
        return result;
    }

    @Override
    public List<Object> getSelected() {
        return new ArrayList<>();
    }
}
