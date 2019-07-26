package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.dialog.DialogFileTable;
import pmb.music.AllMusic.view.dialog.ExceptionDialog;

/**
 * Contextual Menu for {@link DialogFileTable}.
 * 
 * @see {@link PopupMenu}
 * @author pmbroca
 */
public class DialogFilePopupMenu extends PopupMenu {
	private static final Logger LOG = LogManager.getLogger(DialogFilePopupMenu.class);

	/**
	 * Constructor of {@link DialogFilePopupMenu}.
	 * 
	 * @param dialogFileTable dialog file
	 * @param artistIndex index in row of the artist
	 * @param titleIndex index in row of the title
	 * @param fileNameIndex index in row of the file name
	 * @param authorIndex index in row of the author
	 * @param rankIndex index in row of the rank
	 */
	@SuppressWarnings("unchecked")
	public DialogFilePopupMenu(DialogFileTable dialogFileTable, int artistIndex, int titleIndex, int fileNameIndex,
			int authorIndex, int rankIndex) {
		super();
		LOG.debug("Start DialogFilePopupMenu");

		// Open XML file
		buildOpenXmlFileMenu(KeyEvent.VK_X, fileNameIndex, rankIndex);
		// Open TXT file
		buildOpenTxtFileMenu(KeyEvent.VK_T, fileNameIndex, rankIndex, authorIndex);
		// Copy clipboard artist and title
		buildCopyArtistAndTitleMenu(KeyEvent.VK_C, artistIndex, titleIndex);
		// Copy clipboard artist
		buildCopySelectedRowFieldMenu(KeyEvent.VK_A, artistIndex, "Copier l'artiste");
		// Copy clipboard title
		buildCopySelectedRowFieldMenu(KeyEvent.VK_R, titleIndex, "Copier le titre");
		// Copy clipboard file name
		buildCopySelectedRowFieldMenu(KeyEvent.VK_C, fileNameIndex, "Copier le nom du fichier");
		// Edition of selected composition
		ComponentBuilder.buildMenuItem(menu, "Modifier cette composition", KeyEvent.VK_E, (ActionEvent e) -> {
			try {
				this.setVisible(false);
				dialogFileTable.modifyCompositionAction((Vector<Object>) selectedRow);
			} catch (MyException e1) {
				LOG.error("An exception has been thrown when editing composition: ", e1);
				ExceptionDialog ed = new ExceptionDialog("An exception has been thrown when editing composition",
						e1.getMessage(), e1);
				ed.setVisible(true);
			}
			LOG.debug("End edit");
		}, null);

		LOG.debug("End DialogFilePopupMenu");
	}
}
