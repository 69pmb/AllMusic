package pmb.music.AllMusic.view;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.AbstractModel;
import pmb.music.AllMusic.view.popup.PopupMenu;

/**
 * Builder for {@link JTable}. Created by PBR on 8 févr. 2019.
 */
public class TableBuilder {
    private MyTable table;
    private static final Set<Index> UNSORTED_COLUMNS = Set.of(Index.LINE_NUMBER, Index.DECILE, Index.DELETED,
            Index.UUID);

    /**
     * Initializes the builder.
     */
    public TableBuilder() {
        table = new MyTable();
    }

    /**
     * Builds the table.
     *
     * @return the table built
     */
    public MyTable build() {
        if (table.getModel() == null || table.getHeader() == null || table.getRowSorter() == null
                || table.getIndex() == null) {
            throw new IllegalArgumentException("Table has not been fully initialized");
        }
        return table;
    }

    /**
     * Initializes table model, header and data. Data can be null.
     *
     * @param data init table data
     * @param header column header names
     * @param modelType the class of the model
     * @return the table builder
     * @throws MajorException if something went wrong when instantiating the model
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TableBuilder withModelAndData(Vector<Vector<Object>> data, String[] header,
            Class<? extends AbstractModel> modelType) throws MajorException {
        table.setHeader(header);
        AbstractModel model = null;
        try {
            if (data == null) {
                model = modelType.getDeclaredConstructor(Object[][].class, String[].class)
                        .newInstance(new Object[0][header.length - 1], header);
            } else {
                model = modelType.getDeclaredConstructor(Vector.class, Vector.class).newInstance(data,
                        new Vector(Arrays.asList(header)));
            }
        } catch (ReflectiveOperationException e) {
            throw new MajorException("Error when instantiate model of a table", e);
        }
        table.setModel(model);
        table.getTable().setModel(model);
        return this;
    }

    /**
     * Initializes the row sorter. Columns defined in the constant
     * {@link TableBuilder#UNSORTED_COLUMNS} are not sorted.
     *
     * @return the table builder
     */
    private TableBuilder withRowSorter() {
        if (table.getModel() == null) {
            throw new IllegalArgumentException("Table model must be initialized first");
        }
        if (table.getIndex() == null) {
            throw new IllegalArgumentException("Table index must be initialized first");
        }
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel()) {
            @Override
            public boolean isSortable(int column) {
                return UNSORTED_COLUMNS.stream().filter(table.getIndex()::has)
                        .noneMatch(index -> column == table.getIndex().get(index));
            }
        };
        table.getTable().setRowSorter(sorter);
        return this;
    }

    /**
     * Initializes the row sorter listener with standard configurations.
     *
     * @return the table builder
     */
    public TableBuilder withDefaultRowSorterListener() {
        withRowSorter();
        table.getTable().getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
            List<? extends SortKey> sortKeys = e.getSource().getSortKeys();
            if (!sortKeys.isEmpty()) {
                if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                    // Store sorted column and order
                    table.setSortedColumn(sortKeys.get(0).getColumn());
                    table.setSortOrder(sortKeys.get(0).getSortOrder());
                }
                table.setLineNumber();
            }
        });
        return this;
    }

    /**
     * Initializes the row sorter listener with selected column sortable on deleted
     * composition.
     *
     * @return the table builder
     */
    public TableBuilder withRowSorterListenerDelete() {
        withRowSorter();
        table.getTable().getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
            List<? extends SortKey> sortKeys = e.getSource().getSortKeys();
            if (!sortKeys.isEmpty()) {
                if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                    // Sort of deleted column and store sorted column and order
                    int column = sortKeys.get(0).getColumn();
                    if (column == table.getIndex().get(Index.SELECTED)) {
                        table.setSortedColumn(table.getIndex().get(Index.DELETED));
                        table.setSortDeletedOrder(
                                table.getSortDeletedOrder() == SortOrder.ASCENDING ? SortOrder.DESCENDING
                                        : SortOrder.ASCENDING);
                        table.setSortOrder(table.getSortDeletedOrder());
                        List<SortKey> list = new LinkedList<>(Arrays
                                .asList(new RowSorter.SortKey(table.getSortedColumn(), table.getSortDeletedOrder())));
                        table.getRowSorter().getSortKeys().stream().forEach(list::add);
                        table.getRowSorter().setSortKeys(list);
                    } else {
                        table.setSortOrder(sortKeys.get(0).getSortOrder());
                        table.setSortedColumn(column);
                    }
                }
                table.setLineNumber();
            }
        });
        return this;
    }

    /**
     * Initializes the table popup menu.
     *
     * @param popup the menu
     * @return the table builder
     */
    public TableBuilder withPopupMenu(PopupMenu popup) {
        popup.setTable(table.getTable());
        table.setPopupMenu(popup);
        return this;
    }

    /**
     * Sets column index of the table.
     *
     * @param index given index to set
     * @return the table builder
     */
    public TableBuilder withColumnIndex(ColumnIndex index) {
        table.setIndex(index);
        return this;
    }

    /**
     * Initializes the table key listener.
     *
     * @return the table builder
     */
    public TableBuilder withKeyListener() {
        table.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Nothing to do
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (table.getPopupMenu() != null && List
                        .of(KeyEvent.VK_UP, KeyEvent.VK_KP_UP, KeyEvent.VK_PAGE_UP, KeyEvent.VK_END, KeyEvent.VK_DOWN,
                                KeyEvent.VK_KP_DOWN, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_BEGIN)
                        .contains(e.getKeyCode())) {
                    PanelUtils.getSelectedRow(table.getTable(), table.getTable().getSelectedRow()).ifPresent(row -> {
                        table.getPopupMenu().setSelectedRow(row);
                        Point pointAtRow = PanelUtils.pointAtRow(table.getTable(), table.getTable().getSelectedRow());
                        table.getPopupMenu().setPoint(pointAtRow);
                        table.getPopupMenu().getMenu().setLocation(pointAtRow);
                    });
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT && table.getPopupMenu() != null) {
                    table.getPopupMenu().show(e);
                } else {
                    table.setSelectedRow(
                            PanelUtils.keyShortcutAction(e, table.getSelectedRow(), table.getSortedColumn()));
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Nothing to do
            }
        });
        return this;
    }

    /**
     * Initializes the mouse clicked listener.
     *
     * @param mouseClickAction the action executed when mouse is clicked
     * @return the table builder
     */
    public TableBuilder withMouseClickAction(Consumer<MouseEvent> mouseClickAction) {
        table.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseClickAction.accept(e);
            }
        });
        return this;
    }
}
