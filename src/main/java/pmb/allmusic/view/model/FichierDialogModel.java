/**
 *
 */
package pmb.allmusic.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.dialog.DialogFileTable;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront
 * affichées pour {@code DialogFileTable}.
 *
 * @see {@link DefaultTableModel}
 */
public class FichierDialogModel extends AbstractModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de {@link FichierDialogModel}.
     *
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public FichierDialogModel(Object[][] data, String[] title) {
        super(data, title);
    }

    /**
     * Constructeur de {@link FichierDialogModel}.
     *
     * @param data {@link Vector} les données
     * @param title {@link Vector} les entetes du tableau
     */
    public FichierDialogModel(Vector<? extends Vector<?>> data, Vector<?> title) {
        super(data, title);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class<?> result;
        if (Arrays.asList(DialogFileTable.getIndex().get(Index.PUBLISH),DialogFileTable.getIndex().get(Index.FILE_SIZE),
                DialogFileTable.getIndex().get(Index.RANK)).contains(col)) {
            result = Integer.class;
        } else if (col == DialogFileTable.getIndex().get(Index.PERCENT_DELETED)) {
            result = Double.class;
        } else if (col == DialogFileTable.getIndex().get(Index.SCORE)) {
            result = Long.class;
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
