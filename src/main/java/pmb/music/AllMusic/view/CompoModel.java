/**
 * 
 */
package pmb.music.AllMusic.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront
 * affichées.
 * 
 * @see {@link DefaultTableModel}
 * @author pmbroca
 */
public class CompoModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	private static final int INDEX_SELECTED = 5;
	private static final int INDEX_SCORE = 4;
	private static final int INDEX_FILE_SIZE = 3;

	private boolean withScore;

	/**
	 * Constructeur de {@link CompoModel}.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 * @param withScore TODO
	 */
	public CompoModel(Object[][] data, String[] title, boolean withScore) {
		super(data, title);
		this.withScore = withScore;
	}

	/**
	 * Constructeur de {@link CompoModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 * @param withScore TODO
	 */
	@SuppressWarnings("rawtypes")
	public CompoModel(Vector data, Vector title, boolean withScore) {
		super(data, title);
		this.withScore = withScore;
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return withScore ? i1 == INDEX_SELECTED : i1 == INDEX_SCORE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(int col) {
		switch (col) {
		case INDEX_FILE_SIZE:
			return Integer.class;
		case INDEX_SCORE:
			return withScore ? Integer.class : Boolean.class;
		case INDEX_SELECTED:
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
			if ((boolean) ((Vector) dataVector.elementAt(i)).elementAt(INDEX_SELECTED)) {
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
