/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront
 * affichées pour l'onglet {@code FichierPanel}.
 * 
 * @see {@link DefaultTableModel}
 */
public class FichierPanelModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	private static final int INDEX_PUBLISH_YEAR = 2;
	private static final int INDEX_CREATE_DATE = 5;
	private static final int INDEX_FILE_SIZE = 6;

	/**
	 * Constructeur de {@link FichierPanelModel}.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 */
	public FichierPanelModel(Object[][] data, String[] title) {
		super(data, title);
	}

	/**
	 * Constructeur de {@link FichierPanelModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	public FichierPanelModel(Vector<?> data, Vector<?> title) {
		super(data, title);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == INDEX_PUBLISH_YEAR || col == INDEX_FILE_SIZE) {
			return Integer.class;
		} else if (col == INDEX_CREATE_DATE) {
			return Date.class;
		} else {
			return String.class;
		}
	}

	@Override
	public List<Object> getSelected() {
		return null;
	}
}
