package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Contextual Menu for {@link FichierPanel}.
 * 
 * @see {@link JPopupMenu}
 * @author pmbroca
 */
public class FichierPopupMenu extends AbstractPopupMenu {
	private static final long serialVersionUID = 4954841294693242496L;
	private static final Logger LOG = Logger.getLogger(FichierPopupMenu.class);

	/**
	 * Constructor of {@link FichierPopupMenu}.
	 * 
	 * @param fileNameIndex index in row of the filename
	 * @param authorIndex index in row of the author
	 */
	@SuppressWarnings("unchecked")
	public FichierPopupMenu(int fileNameIndex, int authorIndex) {
		super();
		LOG.debug("Start FichierPopupMenu");

		// Open XML file
		buildMenuItem("Ouvrir le fichier XML", KeyEvent.VK_X, (ActionEvent e) -> {
			LOG.debug("Start openXml");
			try {
				FichierUtils.openFileInNotepad(
						FichierUtils.buildXmlFilePath((String) selectedRow.get(fileNameIndex)).orElse(null), null);
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
				FichierUtils.openFileInNotepad(FichierUtils.buildTxtFilePath((String) selectedRow.get(fileNameIndex),
						(String) selectedRow.get(authorIndex)).orElse(null), null);
				this.setVisible(false);
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
			}
			LOG.debug("End openTxt");
		});

		// Copy clipboard file name
		buildMenuItem("Copier le nom du fichier", KeyEvent.VK_C, (ActionEvent e) -> {
			LOG.debug("Start copy");
			MiscUtils.clipBoardAction((String) selectedRow.get(fileNameIndex));
			this.setVisible(false);
			LOG.debug("End copy");
		});

		// Modify file
		buildMenuItem("Modifier le fichier", KeyEvent.VK_E, (ActionEvent e) -> {
			LOG.debug("Start modifFile");
			((FichierPanel) SwingUtilities.getAncestorOfClass(FichierPanel.class, getTable()))
					.modifyFichierAction((Vector<String>) selectedRow);
			this.setVisible(false);
			LOG.debug("End modifFile");
		});

		LOG.debug("End FichierPopupMenu");
	}
}
