/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Composition} seront
 * affichées pour l'onglet {@code FichierPanel}.
 * 
 * @see {@link AbstractModel}
 */
public class CompoFichierPanelModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

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
		return i1 == FichierPanel.INDEX_COMPO_SELECTED;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case FichierPanel.INDEX_COMPO_RANK:
			return Integer.class;
		case FichierPanel.INDEX_COMPO_FILE_SIZE:
			return Integer.class;
		case FichierPanel.INDEX_COMPO_SCORE:
			return Long.class;
		case FichierPanel.INDEX_COMPO_LINE_NUMBER:
			return Integer.class;
		case FichierPanel.INDEX_COMPO_SELECTED:
			return Boolean.class;
		default:
			return String.class;
		}
	}

	@Override
	public List<Object> getSelected() {
		List<Object> toReturn = new ArrayList<>();
		for (int i = 0; i < dataVector.size(); i++) {
			if ((Boolean) ((Vector<?>) dataVector.elementAt(i)).elementAt(FichierPanel.INDEX_COMPO_SELECTED)) {
				toReturn.add(dataVector.elementAt(i));
			}
		}
		return toReturn;
	}
}
