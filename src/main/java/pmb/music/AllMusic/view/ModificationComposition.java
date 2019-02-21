package pmb.music.AllMusic.view;

import java.util.Vector;

import pmb.music.AllMusic.utils.MyException;

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
	 * @throws MyException if the edition fails
	 */
	void modifyCompositionAction(Vector<String> selectedRow) throws MyException;
}
