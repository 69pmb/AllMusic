/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Décrit la façon dont les popup contenant des {@code Composition} seront
 * affichées.
 * 
 * @see {@link AbstractModel}
 */
public class CompoDialogModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur de {@link CompoDialogModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	public CompoDialogModel(Vector<?> data, Vector<?> title) {
		super(data, title);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 3) {
			return Integer.class;
		} else {
			return String.class;
		}
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return false;
	}

	@Override
	public List<Object> getSelected() {
		return new ArrayList<>();
	}
}
