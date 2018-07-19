/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront
 * affichées pour {@code DialogFileTable}.
 * 
 * @see {@link DefaultTableModel}
 */
public class FichierDialogModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	private static final int INDEX_PUBLISH_YEAR = 5;
	private static final int INDEX_FILE_SIZE = 8;
	private static final int INDEX_RANK = 9;

	/**
	 * Constructeur de {@link FichierDialogModel}.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 */
	public FichierDialogModel(Object[][] data, String[] title) {
		super(data, title);
	}

	/**
	 * Constructeur de {@link FichierDialogModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	@SuppressWarnings("rawtypes")
	public FichierDialogModel(Vector data, Vector title) {
		super(data, title);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(int col) {
		if (col == INDEX_PUBLISH_YEAR || col == INDEX_FILE_SIZE || col == INDEX_RANK) {
			return Integer.class;
		} else {
			return String.class;
		}
	}

	@Override
	public List<Object> getSelected() {
		return null;
	}
}
