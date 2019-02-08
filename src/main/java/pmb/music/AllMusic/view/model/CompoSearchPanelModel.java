/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pmb.music.AllMusic.view.panel.SearchPanel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront
 * affichées pour l'onglet {@code SearchPanel}.
 * 
 * @see {@link AbstractModel}
 */
public class CompoSearchPanelModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur de {@link CompoSearchPanelModel}.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 */
	public CompoSearchPanelModel(Object[][] data, String[] title) {
		super(data, title);
	}

	/**
	 * Constructeur de {@link CompoSearchPanelModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	public CompoSearchPanelModel(Vector<?> data, Vector<?> title) {
		super(data, title);
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return i1 == SearchPanel.INDEX_SELECTED;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		Class<?> result;
		switch (col) {
		case SearchPanel.INDEX_FILE_SIZE:
			result = Integer.class;
			break;
		case SearchPanel.INDEX_SCORE:
			result = Integer.class;
			break;
		case SearchPanel.INDEX_LINE_NUMBER:
			result = Integer.class;
			break;
		case SearchPanel.INDEX_DECILE:
			result = Integer.class;
			break;
		case SearchPanel.INDEX_SELECTED:
			result = Boolean.class;
			break;
		default:
			result = String.class;
			break;
		}
		return result;
	}

	@Override
	public List<Object> getSelected() {
		List<Object> toReturn = new ArrayList<>();
		for (int i = 0; i < dataVector.size(); i++) {
			if ((Boolean) ((Vector<?>) dataVector.elementAt(i)).elementAt(SearchPanel.INDEX_SELECTED)) {
				toReturn.add(dataVector.elementAt(i));
			}
		}
		return toReturn;
	}
}
