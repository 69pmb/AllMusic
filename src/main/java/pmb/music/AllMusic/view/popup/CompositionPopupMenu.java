package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ModificationComposition;

/**
 * Contextual Menu for compositions table.
 * 
 * @see {@link JPopupMenu}
 */
public class CompositionPopupMenu extends PopupMenu {
	private static final long serialVersionUID = 4954841294693242496L;
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
		buildMenuItem("Copier l'artiste et le titre", KeyEvent.VK_C, (ActionEvent e) -> {
			LOG.debug("Start copy A+T");
			MiscUtils.clipBoardAction(selectedRow.get(artistIndex) + " " + selectedRow.get(titleIndex));
			this.setVisible(false);
			LOG.debug("End copy A+T");
		});

		// Copy clipboard artist
		buildMenuItem("Copier l'artiste", KeyEvent.VK_A, (ActionEvent e) -> {
			LOG.debug("Start copy A");
			MiscUtils.clipBoardAction((String) selectedRow.get(artistIndex));
			this.setVisible(false);
			LOG.debug("End copy A");
		});

		// Copy clipboard title
		buildMenuItem("Copier le titre", KeyEvent.VK_T, (ActionEvent e) -> {
			LOG.debug("Start copy T");
			MiscUtils.clipBoardAction((String) selectedRow.get(titleIndex));
			this.setVisible(false);
			LOG.debug("End copy T");
		});

		if (type != null) {
			// Modify composition
			buildMenuItem("Modifier la composition", KeyEvent.VK_E, (ActionEvent e) -> {
				LOG.debug("Start modifComposition");
				type.cast(SwingUtilities.getAncestorOfClass(type, getTable()))
						.modifyCompositionAction((Vector<String>) selectedRow);
				this.setVisible(false);
				LOG.debug("End modifComposition");
			});
		}

		LOG.debug("End CompositionPopupMenu");
	}
}
