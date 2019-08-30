/**
 * 
 */
package pmb.music.AllMusic.view.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import pmb.music.AllMusic.view.dialog.DialogCompoTable;

/**
 * Décrit la façon dont les popup contenant des {@code Composition} seront affichées.
 * 
 * @see {@link AbstractModel}
 * @author pmbroca
 */
public class CompoDialogModel extends AbstractModel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur de {@link CompoDialogModel}.
	 * 
	 * @param data {@link Vector} les données
	 * @param title {@link Vector} les entetes du tableau
	 */
	public CompoDialogModel(Vector<? extends Vector<?>> data, Vector<?> title) {
		super(data, title);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		Class<?> result;
		if (Arrays.asList(DialogCompoTable.INDEX_RANK, DialogCompoTable.INDEX_FILE_SIZE, DialogCompoTable.INDEX_DECILE).contains(col)) {
			result = Integer.class;
		} else if (col == DialogCompoTable.INDEX_SCORE) {
			result = Long.class;
		} else {
			result = String.class;
		}
		return result;
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
