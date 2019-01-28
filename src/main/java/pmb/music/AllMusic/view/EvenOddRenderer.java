/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.RecordType;
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
	 * UNKNOWN/YEAR color.
	 */
	private static final Color PURPLE = new Color(216, 150, 255);
	private static final Color GENRE = new Color(167, 196, 76);
	private static final Color ALL_TIME = new Color(246, 132, 27);
	private static final Color THEME = new Color(0, 174, 219);
	private static final Color LONG_PERIOD = new Color(36, 92, 72);
	private static final Color DECADE = new Color(120, 25, 70);

	private static final Color[] DECILE_SCORE_PURPLE = { new Color(243, 233, 252), new Color(219, 191, 246),
			new Color(196, 149, 240), new Color(173, 106, 234), new Color(149, 64, 228), new Color(138, 43, 226),
			new Color(110, 34, 180), new Color(82, 25, 135), new Color(55, 17, 90), new Color(27, 8, 45) };

	Integer deletedIndex;
	Integer typeIndex;
	Integer catIndex;
	Integer decileIndex;
	Integer scoreIndex;

	/**
	 * Constructor for {@link EvenOddRenderer}.
	 * 
	 * @param deletedIndex index of the deleted column, used to draw deleted row
	 *            with specific color
	 * @param typeIndex index of the record type column, used to color record type
	 *            cell depending on the type
	 * @param catIndex index of the category column, used to color category cell
	 * @param decileIndex index of the decile column, used to add tooltip
	 * @param scoreIndex TODO
	 */
	public EvenOddRenderer(Integer deletedIndex, Integer typeIndex, Integer catIndex, Integer decileIndex,
			Integer scoreIndex) {
		this.deletedIndex = deletedIndex;
		this.typeIndex = typeIndex;
		this.catIndex = catIndex;
		this.decileIndex = decileIndex;
		this.scoreIndex = scoreIndex;
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
		// ToolTip
		if (value instanceof String && ((String) value).length() > 30) {
			setToolTipText((String) value);
		} else if (decileIndex != null && scoreIndex != null && column == scoreIndex) {
			setToolTipText(String.valueOf(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(decileIndex)) + " / 10");
		} else {
			setToolTipText(null);
		}
		Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color foreground;
		Color background;
		table.setBorder(noFocusBorder);
		if (isSelected) {
			setBorder(new MatteBorder(1, 0, 1, 0, Color.black));
		}
		Boolean rowDeleted = null;
		if (deletedIndex != null) {
			// If display row with deleted rows
			rowDeleted = Boolean.valueOf(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(deletedIndex));
		}

		Font font = renderer.getFont();
		if (typeIndex != null && column == typeIndex) {
			// If display a row with record type
			RecordType type = RecordType.getByValue(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(typeIndex));
			renderer.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 5));
			if (type.getRecordType().equals(value) && !Boolean.TRUE.equals(rowDeleted)) {
				// only the record type cell is colored
				renderer.setForeground(type == RecordType.ALBUM ? YELLOW : type == RecordType.SONG ? RED : PURPLE);
				background = isSelected ? DARK_BLUE : row % 2 == 0 ? GRAY : BLUE;
				renderer.setBackground(background);
				return renderer;
			}
		}

		if (catIndex != null && column == catIndex) {
			// If display a row with cat
			Cat cat = Cat.getByValue(((Vector<String>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(catIndex));
			renderer.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 5));
			if (cat.getCat().equals(value) && !Boolean.TRUE.equals(rowDeleted)) {
				// only the cat cell is colored
				switch (cat) {
				case YEAR:
					foreground = PURPLE;
					break;
				case DECADE:
					foreground = DECADE;
					break;
				case LONG_PERIOD:
					foreground = LONG_PERIOD;
					break;
				case THEME:
					foreground = THEME;
					break;
				case GENRE:
					foreground = GENRE;
					break;
				case ALL_TIME:
					foreground = ALL_TIME;
					break;
				default:
					foreground = row % 2 != 0 ? GRAY : BLUE;
					break;
				}
				renderer.setForeground(foreground);
				background = isSelected ? DARK_BLUE : row % 2 == 0 ? GRAY : BLUE;
				renderer.setBackground(background);
				return renderer;
			}
		}
		if (scoreIndex != null && decileIndex != null && column == scoreIndex) {
			// If display a row with score
			Integer decile = (Integer) ((Vector<Object>) ((AbstractModel) table.getModel()).getDataVector()
					.get(table.getRowSorter().convertRowIndexToModel(row))).get(decileIndex);
			renderer.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 5));
			if (decile != 0) {
				// only the score cell is colored
				background = DECILE_SCORE_PURPLE[decile - 1];
				foreground = isSelected ? DARK_BLUE : decile <= 2 ? DARK_BLUE : row % 2 == 0 ? BLUE : GRAY;
				renderer.setForeground(foreground);
				renderer.setBackground(background);
				return renderer;
			}
		}
		if (deletedIndex != null) {
			// If display row with deleted rows
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
