package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.ModificationComposition;

/**
 * Contextual Menu for compositions table.
 * 
 * @see {@link JPopupMenu}
 */
public class CompositionPopupMenu extends PopupMenu {
	private static final Logger LOG = Logger.getLogger(CompositionPopupMenu.class);

	/**
	 * Constructor of {@link CompositionPopupMenu}.
	 * 
	 * @param type Class type implementing {@link ModificationComposition}
	 * @param artistIndex index in row of the artist
	 * @param titleIndex index in row of the title
	 */
	@SuppressWarnings("unchecked")
	public CompositionPopupMenu(Class<? extends ModificationComposition> type, int artistIndex, int titleIndex) {
		super();
		LOG.debug("Start CompositionPopupMenu");

		// Copy clipboard artist and title
		buildCopyArtistAndTitleMenu(KeyEvent.VK_C, artistIndex, titleIndex);
		// Copy clipboard artist
		buildCopySelectedRowFieldMenu(KeyEvent.VK_A, artistIndex, "Copier l'artiste");
		// Copy clipboard title
		buildCopySelectedRowFieldMenu(KeyEvent.VK_T, titleIndex, "Copier le titre");
		if (type != null) {
			// Modify composition
			ComponentBuilder.buildMenuItem(menu, "Modifier la composition", KeyEvent.VK_E, (ActionEvent e) -> {
				LOG.debug("Start modifComposition");
				type.cast(SwingUtilities.getAncestorOfClass(type, getTable()))
						.modifyCompositionAction((Vector<String>) selectedRow);
				this.setVisible(false);
				LOG.debug("End modifComposition");
			}, null);
		}

		LOG.debug("End CompositionPopupMenu");
	}
}
