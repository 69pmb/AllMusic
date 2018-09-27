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

import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.model.AbstractModel;

/**
 * Classe decrivant le style des tableaux de l'application. Colorise une ligne
 * sur deux.
 * 
 * @author pmbroca
 * @see DefaultTableCellRenderer
 */
public class EvenOddRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

	private static final long serialVersionUID = 7366600520755781512L;
	private static final Color BLUE = new Color(47, 129, 210);
	/**
	 * Normal row selected.
	 */
	private static final Color DARK_BLUE = new Color(33, 93, 153);
	private static final Color GRAY = new Color(238, 229, 222);
	/**
	 * Deleted not selected
	 */
	private static final Color GREEN = new Color(10, 208, 111);
	/**
	 * Deleted selected.
	 */
	private static final Color DARK_GREEN = new Color(7, 145, 77);
	/**
	 * ALBUM color.
	 */
	private static final Color YELLOW = new Color(206, 200, 66);
	/**
	 * SONG color.
	 */
	private static final Color RED = new Color(255, 143, 143);
	/**
	 * UNKNOWN color.
	 */
	private static final Color PURPLE = new Color(216, 150, 255);

	Integer deletedIndex;
	Integer typeIndex;

	/**
	 * Constructor for {@link EvenOddRenderer}.
	 * 
	 * @param deletedIndex index of the deleted column, used to draw deleted row
	 *            with specific color
	 * @param typeIndex index of the record type column, used to color record type
	 *            cell depending on the type
	 */
	public EvenOddRenderer(Integer deletedIndex, Integer typeIndex) {
		this.deletedIndex = deletedIndex;
		this.typeIndex = typeIndex;
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
		if (isSelected) {
			setBorder(new MatteBorder(1, 0, 1, 0, Color.black));
		}

		if (typeIndex != null) {
			// If display a row with record type
			RecordType type = RecordType.getByValue(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(typeIndex));
			if (type.getRecordType().equals(value)) {
				// only the record type cell is colored
				renderer.setForeground(type == RecordType.ALBUM ? YELLOW : type == RecordType.SONG ? RED : PURPLE);
				background = isSelected ? DARK_BLUE : row % 2 == 0 ? GRAY : BLUE;
				renderer.setBackground(background);
				return renderer;
			}
		}
		if (deletedIndex != null) {
			// If display row with deleted rows
			Boolean rowDeleted = Boolean.valueOf(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(deletedIndex));
			if (isSelected) {
				// If the row is selected
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
