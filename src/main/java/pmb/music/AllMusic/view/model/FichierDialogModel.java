/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import pmb.music.AllMusic.view.dialog.DialogFileTable;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront
 * affichées pour {@code DialogFileTable}.
 * 
 * @see {@link DefaultTableModel}
 */
public class FichierDialogModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

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
	public FichierDialogModel(Vector<?> data, Vector<?> title) {
		super(data, title);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		Class<?> result;
		if (col == DialogFileTable.INDEX_PUBLISH_YEAR || col == DialogFileTable.INDEX_FILE_SIZE
				|| col == DialogFileTable.INDEX_RANK) {
			result = Integer.class;
		} else if (col == DialogFileTable.INDEX_PERCENT_DELETED) {
			result = Double.class;
		} else if (col == DialogFileTable.INDEX_SCORE) {
			result = Long.class;
		} else {
			result = String.class;
		}
		return result;
	}

	@Override
	public List<Object> getSelected() {
		return new ArrayList<>();
	}
}
