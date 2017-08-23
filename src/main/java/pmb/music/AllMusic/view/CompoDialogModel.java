/**
 * 
 */
package pmb.music.AllMusic.view;

import java.util.Vector;

/**
 * Décrit la façon dont les popup contenant des {@code Composition} seront
 * affichées.
 * 
 * @see {@link CompoModel}
 * @author pmbroca
 */
public class CompoDialogModel extends CompoModel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	@SuppressWarnings("rawtypes")
	public CompoDialogModel(Vector data, Vector title) {
		super(data, title);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(int col) {
		switch (col) {
		case 2:
			return Integer.class;
		default:
			return String.class;
		}
	}
}
