package pmb.music.AllMusic.view.popup;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/**
 * Abstract class for creating a popup menu. Contains fields, init methods and
 * builder for {@link JMenuItem}.
 * 
 * @see {@link JPopupMenu}
 * @author PBR
 *
 */
public abstract class PopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(PopupMenu.class);
	protected transient Vector<?> selectedRow;
	protected Point point;

	/**
	 * Set the data and the position of the popup menu.
	 * 
	 * @param e {@link MouseEvent} event when user clicks on the table
	 * @param selectedRow the selected row by the user
	 */
	public void initDataAndPosition(MouseEvent e, Optional<Vector<String>> selectedRow) {
		LOG.debug("Start initDataAndPosition");
		setPoint(e.getPoint());
		setLocation(e.getLocationOnScreen());
		setSelectedRow(selectedRow.orElse(null));
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
			show(e.getComponent(), (int) getPoint().getX(), (int) getPoint().getY());
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
		this.add(item);
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
}
