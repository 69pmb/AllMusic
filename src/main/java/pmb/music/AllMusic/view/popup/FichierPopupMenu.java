package pmb.music.AllMusic.view.popup;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Contextual Menu for {@link FichierPanel}.
 * 
 * @see {@link JPopupMenu}
 */
public class FichierPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 4954841294693242496L;
	private static final Logger LOG = Logger.getLogger(FichierPopupMenu.class);

	private Vector<String> selectedRow;
	private int fileNameIndex;
	private int authorIndex;
	private JTable table;
	private Point point;

	/**
	 * Constructor of {@link FichierPopupMenu}.
	 * 
	 * @param table the fichier table
	 * @param fileNameIndex index in row of the filename
	 * @param authorIndex index in row of the author
	 */
	public FichierPopupMenu(JTable table, int fileNameIndex, int authorIndex) {
		super();
		LOG.debug("Start FichierPopupMenu");
		this.table = table;
		this.fileNameIndex = fileNameIndex;
		this.authorIndex = authorIndex;

		// Open XML file
		JMenuItem openXml = new JMenuItem("Ouvrir le fichier XML");
		openXml.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		openXml.addActionListener((ActionEvent e) -> {
			LOG.debug("Start openXml");
			try {
				FichierUtils.openFileInNotepad(FichierUtils.buildXmlFilePath(selectedRow.get(fileNameIndex)));
				this.setVisible(false);
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
			}
			LOG.debug("End openXml");
		});
		this.add(openXml);

		// Open TXT file
		JMenuItem openTxt = new JMenuItem("Ouvrir le fichier TXT");
		openTxt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		openTxt.addActionListener((ActionEvent e) -> {
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
		this.add(openTxt);

		// Copy clipboard file name
		JMenuItem copy = new JMenuItem("Copier le nom du fichier");
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		copy.addActionListener((ActionEvent e) -> {
			LOG.debug("Start copy");
			StringSelection selection = new StringSelection(selectedRow.get(fileNameIndex));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			this.setVisible(false);
			LOG.debug("End copy");
		});
		this.add(copy);

		// Modify file
		JMenuItem modifFile = new JMenuItem("Modifier le fichier");
		modifFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		modifFile.addActionListener((ActionEvent e) -> {
			LOG.debug("Start modifFile");
			((FichierPanel) SwingUtilities.getAncestorOfClass(FichierPanel.class, table))
					.modifyFichierAction(selectedRow);
			this.setVisible(false);
			LOG.debug("End modifFile");
		});
		this.add(modifFile);

		LOG.debug("End FichierPopupMenu");
	}

	public int getFileNameIndex() {
		return fileNameIndex;
	}

	public void setFileNameIndex(int fileNameIndex) {
		this.fileNameIndex = fileNameIndex;
	}

	public int getAuthorIndex() {
		return authorIndex;
	}

	public void setAuthorIndex(int authorIndex) {
		this.authorIndex = authorIndex;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	public Vector<String> getSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(Vector<String> selectedRow) {
		this.selectedRow = selectedRow;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}
}
