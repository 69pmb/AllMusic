package pmb.music.AllMusic.view;

import java.util.Vector;

/**
 * Interface defining modification of composition.
 * 
 * @author PBR
 *
 */
public interface ModificationComposition {

	/**
	 * Modifies the given compostion.
	 * 
	 * @param selectedRow the selected composition to modify
	 */
	public void modifyCompositionAction(Vector<String> selectedRow);
}
