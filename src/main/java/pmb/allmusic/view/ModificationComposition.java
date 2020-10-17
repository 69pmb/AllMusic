package pmb.allmusic.view;

import java.util.Vector;

import pmb.my.starter.exception.MajorException;

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
     * @throws MajorException if the edition fails
     */
    void modifyCompositionAction(Vector<String> selectedRow) throws MajorException;

    /**
     * Launchs a dialog to split the selected composition.
     *
     * @param selected the selected row representing a composition
     */
    void splitCompositionAction(Vector<Object> selected);
}
