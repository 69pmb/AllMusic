/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront
 * affichées pour l'onglet {@code FichierPanel}.
 * 
 * @see {@link AbstractModel}
 * @author pmbroca
 */
public class CompoFichierPanelModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	private static final int INDEX_CLASSEMENT = 3;
	private static final int INDEX_FILE_SIZE = 4;
	private static final int INDEX_SCORE = 5;
	private static final int INDEX_SELECTED = 6;

	/**
	 * Constructeur de {@link CompoFichierPanelModel}.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 */
	public CompoFichierPanelModel(Object[][] data, String[] title) {
		super(data, title);
	}

	/**
	 * Constructeur de {@link CompoFichierPanelModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	public CompoFichierPanelModel(Vector<?> data, Vector<?> title) {
		super(data, title);
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return i1 == INDEX_SELECTED;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case INDEX_CLASSEMENT:
			return Integer.class;
		case INDEX_FILE_SIZE:
			return Integer.class;
		case INDEX_SCORE:
			return Long.class;
		case INDEX_SELECTED:
			return Boolean.class;
		default:
			return String.class;
		}
	}

	@Override
	public List<Object> getSelected() {
		List<Object> toReturn = new ArrayList<>();
		for (int i = 0; i < dataVector.size(); i++) {
			if ((Boolean) ((Vector<?>) dataVector.elementAt(i)).elementAt(INDEX_SELECTED)) {
				toReturn.add(dataVector.elementAt(i));
			}
		}
		return toReturn;
	}
}
