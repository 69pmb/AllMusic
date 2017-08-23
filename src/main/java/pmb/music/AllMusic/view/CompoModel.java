/**
 * 
 */
package pmb.music.AllMusic.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront affichées.
 * @see {@link DefaultTableModel}
 * @author pmbroca
 */
public class CompoModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur.
     * @param data {@code Object[][]} les données
     * @param title {@code String[]} les entetes du tableau
     */
    public CompoModel(Object[][] data, String[] title) {
        super(data, title);
    }
    
    /**
     * Constructeur.
     * @param data {@link Vector} les données
     * @param title {@link Vector} les entetes du tableau
     */
    @SuppressWarnings("rawtypes")
    public CompoModel(Vector data, Vector title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return i1 == 4;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 3:
                return Integer.class;
            case 4:
                return Boolean.class;
            default:
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
    public List<Object> getSelected() {
        List<Object> toReturn = new ArrayList<>();
        for (int i = 0; i < dataVector.size(); i++) {
            if ((boolean) ((Vector) dataVector.elementAt(i)).elementAt(4)) {
                toReturn.add(dataVector.elementAt(i));
            }
        }
        return toReturn;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Vector) this.getDataVector().elementAt(rowIndex)).elementAt(columnIndex);
    }
}
