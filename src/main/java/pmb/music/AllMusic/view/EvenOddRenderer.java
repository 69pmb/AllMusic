/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.model.AbstractModel;

/**
 * Classe decrivant le style des tableaux de l'application. Colorise une ligne
 * sur deux.
 * 
 * @see DefaultTableCellRenderer
 */
public class EvenOddRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

	private static final long serialVersionUID = 7366600520755781512L;
	private static final Color BLUE = new Color(47, 129, 210);
	private static final Color DARK_BLUE = new Color(33, 93, 153);
	private static final Color GRAY = new Color(238, 229, 222);
	private static final Color GREEN = new Color(10, 208, 111);
	private static final Color DARK_GREEN = new Color(7, 145, 77);

	Integer deletedIndex;

	/**
	 * Constructor for {@link EvenOddRenderer}.
	 * 
	 * @param deletedIndex index of the deleted column, use it to draw deleted row
	 *            with specific color
	 */
	public EvenOddRenderer(Integer deletedIndex) {
		this.deletedIndex = deletedIndex;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (value instanceof Number) {
			value = NumberFormat.getNumberInstance().format(value);
		} else if (value instanceof Date) {
			value = new Constant().getSdfDttm().format(value);
		}
		Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color foreground;
		Color background;
		table.setBorder(noFocusBorder);

		if (deletedIndex != null) {
			// If display row with deleted rows
			Boolean rowDeleted = Boolean.valueOf(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(deletedIndex));
			if (isSelected) {
				// If the row is selected
				setBorder(new MatteBorder(1, 0, 1, 0, Color.black));
				if (rowDeleted) {
					// If the row is deleted
					background = DARK_GREEN;
					foreground = GRAY;
				} else {
					foreground = Color.black;
					background = DARK_BLUE;
				}
			} else {
				if (rowDeleted) {
					background = GREEN;
					foreground = GRAY;
				} else {
					if (row % 2 == 0) {
						foreground = BLUE;
						background = GRAY;
					} else {
						foreground = GRAY;
						background = BLUE;
					}
				}
			}
		} else {
			// No deleted row
			if (isSelected) {
				// If the row is selected
				setBorder(new MatteBorder(1, 0, 1, 0, Color.black));
				foreground = Color.black;
				background = DARK_BLUE;
			} else {
				if (row % 2 == 0) {
					foreground = BLUE;
					background = GRAY;
				} else {
					foreground = GRAY;
					background = BLUE;
				}
			}
		}

		renderer.setForeground(foreground);
		renderer.setBackground(background);
		return renderer;
	}
}
