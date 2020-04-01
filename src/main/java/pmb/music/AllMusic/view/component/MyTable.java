package pmb.music.AllMusic.view.component;

import java.io.Serializable;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import pmb.music.AllMusic.view.model.AbstractModel;
import pmb.music.AllMusic.view.popup.PopupMenu;

/**
 * Wrapper around {@link JTable} to handle generically sorting, popup, model and
 * initialization.
 */
public class MyTable implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTable table;
    private int selectedRow;
    private Integer sortedColumn;
    private SortOrder sortOrder;
    private SortOrder sortDeletedOrder;
    private AbstractModel model;
    private ColumnIndex index;
    private transient PopupMenu popupMenu;
    private String[] header;

    /**
     * Constructor of {@link MyTable}. Sets default value for its table (header,
     * design, sorting).
     */
    public MyTable() {
        table = new JTable();
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setResizingAllowed(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setBackground(UIManager.getColor("Label.background"));
        table.setFont(UIManager.getFont("Label.font"));
        table.setBorder(UIManager.getBorder("Label.border"));
        selectedRow = -1;
        sortDeletedOrder = SortOrder.ASCENDING;
    }

    /**
     * Updates line number column values.
     *
     * @param indexLineNumber index of the column line number
     */
    public void setLineNumber() {
        if (index.has(Index.LINE_NUMBER)) {
            // Handling of line numbers
            for (int i = 0; i < table.getRowCount(); i++) {
                table.setValueAt(i + 1, i, index.get(Index.LINE_NUMBER));
            }
        }
    }

    public RowSorter<? extends TableModel> getRowSorter() {
        return table.getRowSorter();
    }

    public int getRowCount() {
        return table.getRowCount();
    }

    /**
     * Edits a value in table.
     * 
     * @see JTable#setValueAt(Object, int, int)
     * @param aValue the new value
     * @param row row position of the value to replace
     * @param column column position of the value to replace
     */
    public void setValueAt(Object aValue, int row, int column) {
        table.setValueAt(aValue, row, column);
    }

    public TableColumnModel getColumnModel() {
        return table.getColumnModel();
    }

    /**
     * Remove given column from table.
     * 
     * @see JTable#removeColumn(TableColumn)
     * @param aColumn column to remove
     */
    public void removeColumn(TableColumn aColumn) {
        table.removeColumn(aColumn);
    }

    public JTable getTable() {
        return table;
    }

    public void setTable(JTable table) {
        this.table = table;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public Integer getSortedColumn() {
        return sortedColumn;
    }

    public void setSortedColumn(Integer sortedColumn) {
        this.sortedColumn = sortedColumn;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public AbstractModel getModel() {
        return model;
    }

    public void setModel(AbstractModel model) {
        this.model = model;
    }

    public PopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setPopupMenu(PopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public SortOrder getSortDeletedOrder() {
        return sortDeletedOrder;
    }

    public void setSortDeletedOrder(SortOrder sortDeletedOrder) {
        this.sortDeletedOrder = sortDeletedOrder;
    }

    public ColumnIndex getIndex() {
        return index;
    }

    public void setIndex(ColumnIndex index) {
        this.index = index;
    }
}
