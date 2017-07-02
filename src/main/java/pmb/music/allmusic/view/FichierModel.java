/**
 * 
 */
package pmb.music.allmusic.view;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @author i2113mj
 *
 */
public class FichierModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    public FichierModel(Object[][] data, String[] title) {
        super(data, title);
    }
    
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
        if (col < 2 || col == 8 || col == 3 || col == 4) {
            return String.class;
        } else if (col == 2 || col == 6 || col == 7) {
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

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
    }
}
