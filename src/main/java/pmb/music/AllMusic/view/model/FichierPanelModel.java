/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Décrit la façon dont les tableaux contenant des {@code Fichier} seront
 * affichées pour l'onglet {@code FichierPanel}.
 * 
 * @see {@link DefaultTableModel}
 */
public class FichierPanelModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

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
		if (col == FichierPanel.INDEX_FILE_PUBLISH || col == FichierPanel.INDEX_FILE_SIZE
				|| col == FichierPanel.INDEX_FILE_LINE_NUMBER) {
			return Integer.class;
		} else if (col == FichierPanel.INDEX_CREATE_DATE) {
			return LocalDateTime.class;
		} else if (col == FichierPanel.INDEX_PERCENT_DELETED || col == FichierPanel.INDEX_FILE_SCORE
				|| col == FichierPanel.INDEX_FILE_SCORE_DELETED) {
			return Double.class;
		} else {
			return String.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex != FichierPanel.INDEX_CREATE_DATE) {
			return super.getValueAt(rowIndex, columnIndex);
		} else {
			return new Constant().getDateDTF().format((LocalDateTime) super.getValueAt(rowIndex, columnIndex));
		}
	}

	@Override
	public List<Object> getSelected() {
		return new ArrayList<>();
	}
}
