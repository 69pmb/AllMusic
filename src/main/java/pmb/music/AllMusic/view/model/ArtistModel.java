/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Décrit la façon dont les données de l'onglet artiste seront affichées.
 * @see {@link DefaultTableModel}
 */
public class ArtistModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur.
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Class getColumnClass(int col) {
		if (col == 0) {
			return String.class;
		} else {
			return Integer.class;
		}
	}

    @Override
    public int getRowCount() {
        if (this.getDataVector() != null) {
            return this.getDataVector().size();
        } else {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return this.columnIdentifiers.size();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Vector) this.getDataVector().elementAt(rowIndex)).elementAt(columnIndex);
    }
}
