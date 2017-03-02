/**
 * 
 */
package pmb.music.AllMusic.view;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @author i2113mj
 *
 */
public class ArtistModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    public ArtistModel(Object[][] data, String[] title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 0:
                return String.class;
            default:
                return Integer.class;
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
