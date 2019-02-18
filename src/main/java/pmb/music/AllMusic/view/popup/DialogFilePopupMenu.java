package pmb.music.AllMusic.view.popup;

import java.awt.event.KeyEvent;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.view.dialog.DialogFileTable;

/**
 * Contextual Menu for {@link DialogFileTable}.
 * 
 * @see {@link PopupMenu}
 * @author pmbroca
 */
public class DialogFilePopupMenu extends PopupMenu {
	private static final Logger LOG = Logger.getLogger(DialogFilePopupMenu.class);

	/**
	 * Constructor of {@link DialogFilePopupMenu}.
	 * 
	 * @param artistIndex index in row of the artist
	 * @param titleIndex index in row of the title
	 * @param fileNameIndex index in row of the file name
	 * @param authorIndex index in row of the author
	 * @param rankIndex index in row of the rank
	 */
	public DialogFilePopupMenu(int artistIndex, int titleIndex, int fileNameIndex, int authorIndex, int rankIndex) {
		super();
		LOG.debug("Start DialogFilePopupMenu");

		// Open XML file
		buildOpenXmlFileMenu(KeyEvent.VK_X, fileNameIndex, rankIndex);
		// Open TXT file
		buildOpenTxtFileMenu(KeyEvent.VK_T, fileNameIndex, rankIndex, authorIndex);
		// Copy clipboard artist and title
		buildCopyArtistAndTitleMenu(KeyEvent.VK_C, artistIndex, titleIndex);
		// Copy clipboard artist
		buildCopyArtistMenu(KeyEvent.VK_A, artistIndex);
		// Copy clipboard title
		buildCopyTitleMenu(KeyEvent.VK_I, titleIndex);
		// Copy clipboard file name
		buildCopyFileNameMenu(KeyEvent.VK_C, fileNameIndex);

		LOG.debug("End DialogFilePopupMenu");
	}
}
