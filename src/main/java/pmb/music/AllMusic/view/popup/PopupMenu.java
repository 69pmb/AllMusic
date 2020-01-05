package pmb.music.AllMusic.view.popup;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;

/**
 * Parent class for creating a popup menu. Contains fields, init methods and builder for {@link JMenuItem}.
 *
 * @see {@link JPopupMenu}
 * @author PBR
 *
 */
public class PopupMenu {
    private static final Logger LOG = LogManager.getLogger(PopupMenu.class);
    protected Vector<?> selectedRow;
    protected JPopupMenu menu;
    private Point point;
    private JTable table;

    /**
     * Constructor of {@link PopupMenu}.
     */
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
     * Builds a {@link JMenuItem} to copy composition a field of selected row.
     *
     * @param shortcut shortcut of the menu
     * @param index index of the wanted field in the selected row
     * @param label label of the menu item
     */
    public void buildCopySelectedRowFieldMenu(int shortcut, Integer index, String label) {
        ComponentBuilder.buildMenuItem(menu, label, shortcut, (ActionEvent e) -> {
            LOG.debug("Start {}", label);
            MiscUtils.clipBoardAction((String) selectedRow.get(index));
            this.setVisible(false);
            LOG.debug("End copy {}", label);
        }, null);
    }

    /**
     * Builds a {@link JMenuItem} to copy composition artist and title of selected row.
     *
     * @param shortcut shortcut of the menu
     * @param titleIndex index of the title in the selected row
     * @param artistIndex index of the artist in the selected row
     */
    public void buildCopyArtistAndTitleMenu(int shortcut, int artistIndex, int titleIndex) {
        ComponentBuilder.buildMenuItem(menu, "Copier l'artiste et le titre", shortcut, (ActionEvent e) -> {
            LOG.debug("Start copy A+T");
            MiscUtils.clipBoardAction(selectedRow.get(artistIndex) + " " + selectedRow.get(titleIndex));
            this.setVisible(false);
            LOG.debug("End copy A+T");
        }, null);
    }

    /**
     * Builds a {@link JMenuItem} to open the selected xml file.
     *
     * @param shortcut shortcut of the menu
     * @param fileNameIndex index of the filename in the selected row
     * @param rankIndex index of the rank, to opens editor to a specific line
     */
    public void buildOpenXmlFileMenu(int shortcut, int fileNameIndex, Integer rankIndex) {
        ComponentBuilder.buildMenuItem(menu, "Ouvrir le fichier XML", shortcut, (ActionEvent e) -> {
            LOG.debug("Start openXml");
            try {
                FilesUtils.openFileInNotepad(
                        FilesUtils.buildXmlFilePath((String) selectedRow.get(fileNameIndex)).orElse(null),
                        rankIndex == null ? null : (Integer) selectedRow.get(rankIndex));
                this.setVisible(false);
            } catch (MajorException e1) {
                LOG.error("Error when opening with notepad file : " + selectedRow.get(fileNameIndex), e1);
            }
            LOG.debug("End openXml");
        }, null);
    }

    /**
     * Builds a {@link JMenuItem} to open the selected txt file.
     *
     * @param shortcut shortcut of the menu
     * @param index column index of rows
     */
    public void buildOpenTxtFileMenu(int shortcut, ColumnIndex index) {
        ComponentBuilder.buildMenuItem(menu, "Ouvrir le fichier TXT", shortcut, (ActionEvent e) -> {
            LOG.debug("Start openTxt");
            try {
                FilesUtils.openFileInNotepad(
                        FilesUtils.buildTxtFilePath((String) selectedRow.get(index.get(Index.FILE_NAME)),
                                (String) selectedRow.get(index.get(Index.AUTHOR))).orElse(null),
                        index.get(Index.RANK));
                this.setVisible(false);
            } catch (MajorException e1) {
                LOG.error("Error when opening with notepad file : " + selectedRow.get(index.get(Index.FILE_NAME)), e1);
            }
            LOG.debug("End openTxt");
        }, null);
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

    public JPopupMenu getMenu() {
        return menu;
    }
}
