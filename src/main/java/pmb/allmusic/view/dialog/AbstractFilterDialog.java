package pmb.allmusic.view.dialog;

import java.awt.Dimension;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import javax.swing.RowSorter.SortKey;

import org.apache.commons.collections4.CollectionUtils;
import org.kordamp.ikonli.swing.FontIcon;

import pmb.allmusic.utils.Constant;
import pmb.allmusic.view.component.MyTable;

/**
 * Abstract dialog to configure filterable dialog diplaying a list of item with
 * a checkbox in header.
 *
 * @param <T> type displayed by dialog
 * @see {@link AbstractDialog}
 */
public abstract class AbstractFilterDialog<T> extends AbstractDialog {

    private static final FontIcon UNCHECK_ICON = FontIcon.of(Constant.ICON_UNCHECK);
    private static final FontIcon CHECK_ICON = FontIcon.of(Constant.ICON_CHECK);

    private List<T> data;
    private Vector<Vector<Object>> dataConverted;
    private Vector<Vector<Object>> filteredConverted;
    private Predicate<T> filter;
    private Function<List<T>, Vector<Vector<Object>>> convertor;
    private JCheckBoxMenuItem checkBoxMenu;
    private MyTable myTable;
    private SortKey defaultSorting;
    private String label;

    /**
     * Constructor, calling {@link AbstractDialog} one and updating dialog data.
	 *
     * @param title of the dialog
     * @param dim of the dialog
     * @param data data list of the table
     * @param filter condition filtering data
     * @param convertor to convert into suitable table data
     * @param defaultSort table default sorting
     * @param label of the checkbox filtering data
     */
    public AbstractFilterDialog(String title, Dimension dim, List<T> data, Predicate<T> filter,
            Function<List<T>, Vector<Vector<Object>>> convertor, SortKey defaultSort, String label) {
        super(title, dim, true);
        this.filter = filter;
        this.convertor = convertor;
        this.defaultSorting = defaultSort;
        this.label = label;
        updateData(data);
    }

    private JMenuBar checkboxMenuBar(String label) {
        JMenuBar menu = new JMenuBar();
        checkBoxMenu = new JCheckBoxMenuItem(label, UNCHECK_ICON);
        checkBoxMenu.addChangeListener(e -> updateTableData());
        menu.add(Box.createHorizontalGlue());
        menu.add(checkBoxMenu);
        return menu;
    }

    /**
     * Updates data, filters and converts it. If filtering is possible, a checkbox
     * is added in dialog's header.
     *
     * @param data
     */
    protected void updateData(List<T> data) {
        this.data = data;
        this.dataConverted = convertor.apply(data);
        List<T> filtered = data.stream().filter(filter).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(filtered) && filtered.size() != data.size()) {
            this.filteredConverted = convertor.apply(filtered);
            getDialog().setJMenuBar(checkboxMenuBar(label));
        }
    }

    /**
     * Updates table data depending on checkbox status.
     */
    protected void updateTableData() {
        if (checkBoxMenu != null) {
            if (checkBoxMenu.isSelected()) {
                checkBoxMenu.setIcon(CHECK_ICON);
                updateTable(filteredConverted);
            } else {
                checkBoxMenu.setIcon(UNCHECK_ICON);
                updateTable(dataConverted);
            }
        } else {
            updateTable(dataConverted);
        }
    }

    private void updateTable(Vector<Vector<Object>> list) {
        myTable.updateTable(new Vector<>(list), defaultSorting, true);
    }

    protected List<T> getData() {
        return data;
    }

    public MyTable getMyTable() {
        return myTable;
    }

    public void setMyTable(MyTable myTable) {
        this.myTable = myTable;
    }
}
