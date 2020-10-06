package pmb.allmusic.view.component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import pmb.allmusic.utils.MiscUtils;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.PanelUtils;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.model.AbstractModel;
import pmb.allmusic.view.popup.PopupMenu;

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
    private AtomicInteger deletedColumnIndex;

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
        deletedColumnIndex = new AtomicInteger(0);
    }

    /**
     * Updates table data, rendering and sorting.
     * @param data new data displayed
     * @param defaultSorting sorting applied by default
     * @param scrollTop if scroll at the top of the table once updated
     */
    public void updateTable(Vector<Vector<Object>> data, SortKey defaultSorting, boolean scrollTop) {
        // Update table data and rendering
        getModel().setRowCount(0);
        getModel().setDataVector(data, new Vector<>(Arrays.asList(header)));
        PanelUtils.colRenderer(getTable(), !index.has(Index.SELECTED), index);
        setSelectedRow(-1);

        // Sorting
        if (getSortedColumn() == null) {
            setSortedColumn(defaultSorting.getColumn());
            setSortOrder(defaultSorting.getSortOrder());
        }
        table.getRowSorter()
        .setSortKeys(Collections.singletonList(new RowSorter.SortKey(getSortedColumn(), getSortOrder())));

        // Line number column
        if (index.has(Index.LINE_NUMBER)) {
            setLineNumber();
            Integer lineNumberIndex = index.get(Index.LINE_NUMBER);
            table.getColumnModel().getColumn(lineNumberIndex).setMinWidth(40);
            table.getColumnModel().getColumn(lineNumberIndex).setMaxWidth(40);
        }

        // Specific sorter to compare percentages
        List.of(Index.PERCENT_DELETED, Index.SCORE_DELETED).stream().filter(index::has)
        .forEach(column -> ((TableRowSorter<?>) table.getRowSorter()).setComparator(index.get(column),
                MiscUtils.comparePercentage));

        // Hide technical columns
        deletedColumnIndex.set(0);
        List.of(Index.DECILE, Index.DELETED, Index.UUID).stream().filter(index::has).forEach(this::removeColumn);

        // Update view
        getModel().fireTableDataChanged();
        getTable().repaint();
        if (scrollTop && getTable().getParent() != null) {
            ((JScrollPane) getTable().getParent().getParent()).getVerticalScrollBar().setValue(0);
        }
    }

    /**
     * Removes from the table the given column, related data are kept.
     * @param column {@link Index} of the column to remove
     */
    public void removeColumn(Index column) {
        table.removeColumn(table.getColumnModel().getColumn(index.get(column) - deletedColumnIndex.getAndIncrement()));
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

    public AtomicInteger getDeletedColumnIndex() {
        return deletedColumnIndex;
    }
}
