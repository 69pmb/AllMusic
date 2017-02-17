/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author i2113mj
 *
 */
public class MyBooleanRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//        if (value instanceof JComboBox) {
//            return (JComboBox) value;
//        }
        if (value instanceof String) {
            JCheckBox cb = new JCheckBox();
            cb.setSelected(Boolean.valueOf((String) value));
            return cb;
        }
        if (value instanceof Boolean) {
            JCheckBox cb = new JCheckBox();
            cb.setSelected((Boolean) value);
            return cb;
        }
        if (value instanceof JCheckBox) {
            return (JCheckBox) value;
        }
        System.out.println(value.getClass());
        System.out.println(value);
        return new JTextField(value.toString());
    }
}
