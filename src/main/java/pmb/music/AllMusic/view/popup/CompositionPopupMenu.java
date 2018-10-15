package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.panel.SearchPanel;

/**
 * Contextual Menu for {@link SearchPanel}.
 * 
 * @see {@link JPopupMenu}
 * @author pmbroca
 */
public class CompositionPopupMenu extends PopupMenu {
	private static final long serialVersionUID = 4954841294693242496L;
	private static final Logger LOG = Logger.getLogger(CompositionPopupMenu.class);

	/**
	 * Constructor of {@link CompositionPopupMenu}.
	 * 
	 * @param table the composition table
	 * @param artistIndex index in row of the artist
	 * @param titleIndex index in row of the title
	 */
	public CompositionPopupMenu(JTable table, int artistIndex, int titleIndex) {
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
			MiscUtils.clipBoardAction(selectedRow.get(artistIndex));
			this.setVisible(false);
			LOG.debug("End copy A");
		});

		// Copy clipboard title
		buildMenuItem("Copier le titre", KeyEvent.VK_T, (ActionEvent e) -> {
			LOG.debug("Start copy T");
			MiscUtils.clipBoardAction(selectedRow.get(titleIndex));
			this.setVisible(false);
			LOG.debug("End copy T");
		});

		// Modify composition
		buildMenuItem("Modifier la composition", KeyEvent.VK_E, (ActionEvent e) -> {
			LOG.debug("Start modifComposition");
			((SearchPanel) SwingUtilities.getAncestorOfClass(SearchPanel.class, table)).modifAction(selectedRow);
			this.setVisible(false);
			LOG.debug("End modifComposition");
		});

		LOG.debug("End CompositionPopupMenu");
	}
}
