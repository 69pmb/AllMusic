/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.List;

import pmb.music.AllMusic.view.panel.ArtistPanel;

/**
 * Décrit la façon dont les données de l'onglet artiste seront affichées.
 * 
 * @see {@link AbstractModel}
 * @author pmbroca
 */
public class ArtistModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur.
	 * 
	 * @param data {@code Object[][]} les données
	 * @param title {@code String[]} les entetes du tableau
	 */
	public ArtistModel(Object[][] data, String[] title) {
		super(data, title);
	}

	@Override
	public boolean isCellEditable(int i, int i1) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == ArtistPanel.INDEX_ARTIST) {
			return String.class;
		} else {
			return Integer.class;
		}
	}

	@Override
	public List<Object> getSelected() {
		return null;
	}
}
