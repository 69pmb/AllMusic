/**
 * 
 */
package pmb.music.AllMusic.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @author i2113mj
 *
 */
public class CompoModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    public CompoModel(Object[][] data, String[] title) {
        super(data, title);
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return i1 == 4;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Class getColumnClass(int col) {
        if (col == 4) {
            return Boolean.class;
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

    public List<Object> getSelected() {
        List<Object> toReturn = new ArrayList<Object>();
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

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
    }
}
