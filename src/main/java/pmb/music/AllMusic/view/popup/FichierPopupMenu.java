package pmb.music.AllMusic.view.popup;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Contextual Menu for {@link FichierPanel}.
 * 
 * @see {@link JPopupMenu}
 */
public class FichierPopupMenu extends PopupMenu {
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
		buildOpenXmlFileMenu(KeyEvent.VK_X, fileNameIndex, null);
		// Open TXT file
		buildOpenTxtFileMenu(KeyEvent.VK_T, fileNameIndex, null, authorIndex);
		// Copy clipboard file name
		buildCopySelectedRowFieldMenu(KeyEvent.VK_C, fileNameIndex, "Copier le nom du fichier");
		// Modify file
		ComponentBuilder.buildMenuItem(menu, "Modifier le fichier", KeyEvent.VK_E, (ActionEvent e) -> {
			LOG.debug("Start modifFile");
			((FichierPanel) SwingUtilities.getAncestorOfClass(FichierPanel.class, getTable()))
					.modifyFichierAction((Vector<String>) selectedRow);
			this.setVisible(false);
			LOG.debug("End modifFile");
		}, null);

		LOG.debug("End FichierPopupMenu");
	}
}
