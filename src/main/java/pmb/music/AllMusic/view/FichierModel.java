/**
 * 
 */
package pmb.music.AllMusic.view;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront affichées.
 * @see {@link DefaultTableModel}
 * @author pmbroca
 */
public class FichierModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur.
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public FichierModel(Object[][] data, String[] title) {
        super(data, title);
    }
    
    /**
     * Constructeur.
     * @param data {@link Vector} les données
     * @param title {@link Vector} les entetes du tableau
     */
    @SuppressWarnings("rawtypes")
    public FichierModel(Vector data, Vector title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getColumnClass(int col) {
        if (col == 2 || col == 6 || col == 7) {
            return Integer.class;
        } else {
            return String.class;
        }
    }

    @Override
    public int getRowCount() {
        if (this.getDataVector() != null) {
            return this.getDataVector().size();
        } else {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return this.columnIdentifiers.size();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Vector) this.getDataVector().elementAt(rowIndex)).elementAt(columnIndex);
    }
}
