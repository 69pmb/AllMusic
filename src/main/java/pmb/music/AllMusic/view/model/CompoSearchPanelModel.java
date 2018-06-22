/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront
 * affichées pour l'onglet {@code SearchPanel}.
 * 
 * @see {@link AbstractModel}
 */
public class CompoSearchPanelModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	private static final int INDEX_SELECTED = 5;
	private static final int INDEX_SCORE = 4;
	private static final int INDEX_FILE_SIZE = 3;

	/**
	 * Constructeur de {@link CompoSearchPanelModel}.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 */
	public CompoSearchPanelModel(Object[][] data, String[] title) {
		super(data, title);
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return i1 == INDEX_SELECTED;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(int col) {
		switch (col) {
		case INDEX_FILE_SIZE:
			return Integer.class;
		case INDEX_SCORE:
			return Integer.class;
		case INDEX_SELECTED:
			return Boolean.class;
		default:
			return String.class;
		}
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
}
