package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JTable;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.dialog.DialogFileTable;

/**
 * Contextual Menu for {@link DialogFileTable}.
 * 
 * @see {@link PopupMenu}
 * @author pmbroca
 */
public class DialogFilePopupMenu extends PopupMenu {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(DialogFilePopupMenu.class);

	/**
	 * Constructor of {@link DialogFilePopupMenu}.
	 * 
	 * @param table the table
	 * @param artistIndex index in row of the artist
	 * @param titleIndex index in row of the title
	 * @param fileNameIndex index in row of the file name
	 * @param authorIndex index in row of the author
	 */
	public DialogFilePopupMenu(JTable table, int artistIndex, int titleIndex, int fileNameIndex, int authorIndex) {
		super();
		LOG.debug("Start DialogFilePopupMenu");

		// Open XML file
		buildMenuItem("Ouvrir le fichier XML", KeyEvent.VK_X, (ActionEvent e) -> {
			LOG.debug("Start openXml");
			try {
				FichierUtils.openFileInNotepad(FichierUtils.buildXmlFilePath(selectedRow.get(fileNameIndex)));
				this.setVisible(false);
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
			}
			LOG.debug("End openXml");
		});

		// Open TXT file
		buildMenuItem("Ouvrir le fichier TXT", KeyEvent.VK_T, (ActionEvent e) -> {
			LOG.debug("Start openTxt");
			try {
				FichierUtils.openFileInNotepad(
						FichierUtils.buildTxtFilePath(selectedRow.get(fileNameIndex), selectedRow.get(authorIndex)));
				this.setVisible(false);
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
			}
			LOG.debug("End openTxt");
		});

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
		buildMenuItem("Copier le titre", KeyEvent.VK_I, (ActionEvent e) -> {
			LOG.debug("Start copy I");
			MiscUtils.clipBoardAction(selectedRow.get(titleIndex));
			this.setVisible(false);
			LOG.debug("End copy I");
		});

		// Copy clipboard file name
		buildMenuItem("Copier le nom du fichier", KeyEvent.VK_F, (ActionEvent e) -> {
			LOG.debug("Start copy F");
			MiscUtils.clipBoardAction(selectedRow.get(fileNameIndex));
			this.setVisible(false);
			LOG.debug("End copy F");
		});

		LOG.debug("End DialogFilePopupMenu");
	}
}
