/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Classe decrivant le style des tableaux de l'application.
 * Les couleurs des lignes sur 2.
 * @author pmbroca
 */
public class EvenOddRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    private static final long serialVersionUID = 7366600520755781512L;

    public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color foreground;
        Color background;
        Color blue = new Color(47, 129, 210);
        Color darkBlue = new Color(33, 93, 153);
        Color gray = new Color(238, 229, 222);
        table.setBorder(noFocusBorder);
        if (isSelected) {
            setBorder(new MatteBorder(1, 0, 1, 0, Color.black));
            foreground = Color.black;
            background = darkBlue;
        } else {
            if (row % 2 == 0) {
                foreground = blue;
                background = gray;
            } else {
                foreground = gray;
                background = blue;
            }
        }
        renderer.setForeground(foreground);
        renderer.setBackground(background);
        return renderer;
    }
}
