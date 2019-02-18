package pmb.music.AllMusic.view.popup;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;

/**
 * Parent class for creating a popup menu. Contains fields, init methods and
 * builder for {@link JMenuItem}.
 * 
 * @see {@link JPopupMenu}
 * @author PBR
 *
 */
public class PopupMenu {
	private static final Logger LOG = Logger.getLogger(PopupMenu.class);
	protected Vector<?> selectedRow;
	protected JPopupMenu menu;
	protected Point point;
	private JTable table;

	public PopupMenu() {
		this.menu = new JPopupMenu();
	}

	/**
	 * Set the data and the position of the popup menu.
	 * 
	 * @param e {@link MouseEvent} event when user clicks on the table
	 * @param selectedRow the selected row by the user
	 */
	public void initDataAndPosition(MouseEvent e, Vector<String> selectedRow) {
		LOG.debug("Start initDataAndPosition");
		setPoint(e.getPoint());
		menu.setLocation(e.getLocationOnScreen());
		setSelectedRow(selectedRow);
		LOG.debug("End initDataAndPosition");
	}

	/**
	 * Displays the popup.
	 * 
	 * @param e {@link ComponentEvent} event of the component clicked
	 */
	public void show(ComponentEvent e) {
		LOG.debug("Start show");
		if (getPoint() != null) {
			menu.show(e.getComponent(), (int) getPoint().getX(), (int) getPoint().getY());
		}
		LOG.debug("End show");
	}

	/**
	 * Build and add to the popup a {@link JMenuItem}.
	 * 
	 * @param text text of the item
	 * @param shortcut {@link KeyEvent} constant
	 * @param action {@link ActionListener} of the item
	 */
	public void buildMenuItem(String text, int shortcut, ActionListener action) {
		JMenuItem item = new JMenuItem(text);
		item.setAccelerator(KeyStroke.getKeyStroke(shortcut, ActionEvent.CTRL_MASK));
		item.addActionListener(action);
		menu.add(item);
	}

	/**
	 * Builds a {@link JMenuItem} to copy composition title of selected row.
	 * 
	 * @param shortcut shortcut of the menu
	 * @param titleIndex index of the title in the selected row
	 */
	public void buildCopyTitleMenu(int shortcut, int titleIndex) {
		buildMenuItem("Copier le titre", shortcut, (ActionEvent e) -> {
			LOG.debug("Start copy title");
			MiscUtils.clipBoardAction((String) selectedRow.get(titleIndex));
			this.setVisible(false);
			LOG.debug("End copy title");
		});
	}

	/**
	 * Builds a {@link JMenuItem} to copy composition artist of selected row.
	 * 
	 * @param shortcut shortcut of the menu
	 * @param artistIndex index of the artist in the selected row
	 */
	public void buildCopyArtistMenu(int shortcut, int artistIndex) {
		buildMenuItem("Copier l'artiste", shortcut, (ActionEvent e) -> {
			LOG.debug("Start copy Artist");
			MiscUtils.clipBoardAction((String) selectedRow.get(artistIndex));
			this.setVisible(false);
			LOG.debug("End copy Artist");
		});
	}

	/**
	 * Builds a {@link JMenuItem} to copy composition artist and title of selected
	 * row.
	 * 
	 * @param shortcut shortcut of the menu
	 * @param titleIndex index of the title in the selected row
	 * @param artistIndex index of the artist in the selected row
	 */
	public void buildCopyArtistAndTitleMenu(int shortcut, int artistIndex, int titleIndex) {
		buildMenuItem("Copier l'artiste et le titre", shortcut, (ActionEvent e) -> {
			LOG.debug("Start copy A+T");
			MiscUtils.clipBoardAction(selectedRow.get(artistIndex) + " " + selectedRow.get(titleIndex));
			this.setVisible(false);
			LOG.debug("End copy A+T");
		});
	}

	/**
	 * Builds a {@link JMenuItem} to copy file name of selected row.
	 * 
	 * @param shortcut shortcut of the menu
	 * @param fileNameIndex index of the filename in the selected row
	 */
	public void buildCopyFileNameMenu(int shortcut, int fileNameIndex) {
		buildMenuItem("Copier le nom du fichier", shortcut, (ActionEvent e) -> {
			LOG.debug("Start copy filename");
			MiscUtils.clipBoardAction((String) selectedRow.get(fileNameIndex));
			this.setVisible(false);
			LOG.debug("End copy filename");
		});
	}

	/**
	 * Builds a {@link JMenuItem} to open the selected xml file.
	 * 
	 * @param shortcut shortcut of the menu
	 * @param fileNameIndex index of the filename in the selected row
	 * @param rankIndex index of the rank, to opens editor to a specific line
	 */
	public void buildOpenXmlFileMenu(int shortcut, int fileNameIndex, Integer rankIndex) {
		buildMenuItem("Ouvrir le fichier XML", shortcut, (ActionEvent e) -> {
			LOG.debug("Start openXml");
			try {
				FichierUtils.openFileInNotepad(
						FichierUtils.buildXmlFilePath((String) selectedRow.get(fileNameIndex)).orElse(null),
						rankIndex == null ? null : (Integer) selectedRow.get(rankIndex));
				this.setVisible(false);
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
			}
			LOG.debug("End openXml");
		});
	}

	/**
	 * Builds a {@link JMenuItem} to open the selected txt file.
	 * 
	 * @param shortcut shortcut of the menu
	 * @param fileNameIndex index of the filename in the selected row
	 * @param rankIndex index of the rank, to opens editor to a specific line
	 * @param authorIndex index of the artist, to find the directory of the file
	 */
	public void buildOpenTxtFileMenu(int shortcut, int fileNameIndex, Integer rankIndex, int authorIndex) {
		buildMenuItem("Ouvrir le fichier TXT", shortcut, (ActionEvent e) -> {
			LOG.debug("Start openTxt");
			try {
				FichierUtils.openFileInNotepad(
						FichierUtils.buildTxtFilePath((String) selectedRow.get(fileNameIndex),
								(String) selectedRow.get(authorIndex)).orElse(null),
						rankIndex == null ? null : (Integer) selectedRow.get(rankIndex));
				this.setVisible(false);
			} catch (MyException e1) {
				LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
			}
			LOG.debug("End openTxt");
		});
	}

	protected void setVisible(boolean visible) {
		menu.setVisible(visible);
	}

	public void setSelectedRow(Vector<?> selectedRow) {
		this.selectedRow = selectedRow;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}
}
