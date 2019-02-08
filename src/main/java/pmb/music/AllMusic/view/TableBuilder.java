package pmb.music.AllMusic.view;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.AbstractModel;
import pmb.music.AllMusic.view.popup.PopupMenu;

/**
 * Builder for {@link JTable}. Created by PBR on 8 févr. 2019.
 */
public class TableBuilder {
	private MyTable table;

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
		if (table.getModel() == null || table.getHeader() == null || table.getRowSorter() == null) {
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
	 * @throws MyException if something went wrong when instantiating the model
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TableBuilder withModelAndData(Vector<Vector<Object>> data, String[] header,
			Class<? extends AbstractModel> modelType) throws MyException {
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
			throw new MyException("Error when instantiate model of a table", e);
		}
		table.setModel(model);
		table.getTable().setModel(model);
		return this;
	}

	/**
	 * Initializes the row sorter.
	 * 
	 * @param indexLineNumber index of the column line number
	 * @return the table builder
	 */
	public TableBuilder withRowSorter(int indexLineNumber) {
		if (table.getModel() == null) {
			throw new IllegalArgumentException("Table model must be initialized first");
		}
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel()) {
			@Override
			public boolean isSortable(int column) {
				return column != indexLineNumber;
			}
		};
		table.getTable().setRowSorter(sorter);
		return this;
	}

	/**
	 * Initializes the row sorter listener with standard configurations.
	 * 
	 * @param indexLineNumber index of the column line number
	 * @return the table builder
	 */
	public TableBuilder withDefaultRowSorterListener(int indexFileLineNumber) {
		if (table.getTable().getRowSorter() == null) {
			throw new IllegalArgumentException("Table row sorted must be initialized first");
		}
		table.getTable().getRowSorter().addRowSorterListener((RowSorterEvent e) -> {
			List<? extends SortKey> sortKeys = ((RowSorter<?>) e.getSource()).getSortKeys();
			if (!sortKeys.isEmpty()) {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					// Store sorted column and order
					table.setSortedColumn(sortKeys.get(0).getColumn());
					table.setSortOrder(sortKeys.get(0).getSortOrder());
				}
				// Handling of line numbers
				for (int i = 0; i < table.getTable().getRowCount(); i++) {
					table.getTable().setValueAt(i + 1, i, indexFileLineNumber);
				}
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
