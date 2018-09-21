package pmb.music.AllMusic.view.component;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang3.StringUtils;

public class JComboCheckBox extends JComboBox<Object> {
	private static final long serialVersionUID = 3148985615922245686L;

	/**
	 * Store which check boxes are selected. Key: the text of the check box Value:
	 * if the check box is selected or not
	 */
	private Map<String, Boolean> boxes;
	/**
	 * Label of the selectedItem.
	 */
	private String label;
	/**
	 * The selected item of the combo box.
	 */
	private JCheckBox selectedItem;
	private boolean show = false;

	private static final String CHECKBOX_ALL = "All";

	public JComboCheckBox(List<String> items) {
		super(Stream.concat(Stream.of(new JCheckBox(CHECKBOX_ALL)), items.stream().map(item -> new JCheckBox(item)))
				.toArray(JCheckBox[]::new));
		((JComponent) getItemAt(0)).setOpaque(false);
		this.selectedItem = new JCheckBox();
		selectedItem.setOpaque(false);
		this.boxes = new HashMap<>();
		items.forEach(i -> this.boxes.put(i, false));
		this.boxes.put(CHECKBOX_ALL, false);
		setRenderer(new ComboBoxRenderer());
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				itemSelected();
			}
		});
		setLabel();
		insertItemAt(selectedItem, 0);
		this.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if (getModel().getElementAt(0).equals(selectedItem)) {
					removeItemAt(0);
				}
				((JComponent) getItemAt(0)).setOpaque(true);
				show = true;
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				show = false;
				insertSelectedItem();
				((JComponent) getItemAt(1)).setOpaque(false);
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	/**
	 * Deselects all boxes.
	 */
	public void clearSelection() {
		int size = this.getModel().getSize();
		for (int i = 0; i < size; i++) {
			JCheckBox jCheckBox = (JCheckBox) this.getModel().getElementAt(i);
			jCheckBox.setSelected(false);
			this.boxes.put(jCheckBox.getText(), false);
		}
		label = "";
		selectedItem = new JCheckBox();
		selectedItem.setOpaque(false);
		removeItemAt(0);
	}

	@Override
	public void setPopupVisible(boolean v) {
		if (!v && show) {
			// Do nothing(prevent the combo popup from closing)
		} else {
			super.setPopupVisible(v);
			if (!v) {
				insertSelectedItem();
			}
		}
	}

	/**
	 * Calculates the label of the selected item of the combo box depending on the
	 * check boxes selected.
	 */
	private void setLabel() {
		Map<String, Boolean> selected = boxes.entrySet().stream().filter(entry -> entry.getValue())
				.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		int count = selected.keySet().size();
		boolean isAllSelected = selected.keySet().contains(CHECKBOX_ALL);
		if (isAllSelected) {
			label = CHECKBOX_ALL;
		} else if (count == 1) {
			label = selected.keySet().iterator().next();
		} else if (count > 1) {
			label = count + " selected";
		} else {
			label = "";
		}
		selectedItem.setText(label);
	}

	/**
	 * Returns the selected check boxes.
	 * 
	 * @return a joined string with the char ";"
	 */
	public String getSelectedItems() {
		if (boxes.get(CHECKBOX_ALL)) {
			return "";
		} else {
			return this.boxes.entrySet().stream().filter(e -> e.getValue() && !e.getKey().contains(CHECKBOX_ALL))
					.map(e -> e.getKey()).collect(Collectors.joining(";"));
		}
	}

	private void itemSelected() {
		if (getSelectedItem() instanceof JCheckBox && isPopupVisible() && !getSelectedItem().equals(selectedItem)) {
			JCheckBox jcb = (JCheckBox) getSelectedItem();
			boolean selected = !jcb.isSelected();
			if (StringUtils.equalsIgnoreCase(jcb.getText(), CHECKBOX_ALL)) {
				// If click on All check box -> select or deselect all others check boxs
				int size = this.getModel().getSize();
				for (int i = 0; i < size; i++) {
					JCheckBox jCheckBox = (JCheckBox) this.getModel().getElementAt(i);
					jCheckBox.setSelected(selected);
					this.boxes.put(jCheckBox.getText(), selected);
				}
				show = false; // Bug on refreshing check boxes
			} else if (boxes.get(CHECKBOX_ALL)) {
				// deselect all check boxs if deselect a check box
				int size = this.getModel().getSize();
				for (int i = 0; i < size; i++) {
					setSelectedCheckBoxAll(false);
				}
			}
			// Select or deselect the check box
			jcb.setSelected(selected);
			this.boxes.put(jcb.getText(), selected);
			// If all checkboxes are selected, we check the all box
			if (!boxes.get(CHECKBOX_ALL) && this.boxes.entrySet().stream()
					.filter(e -> !e.getKey().contains(CHECKBOX_ALL)).allMatch(e -> e.getValue())) {
				setSelectedCheckBoxAll(true);
			}
		}
	}

	/**
	 * Set the property selected of the check box CHECKBOX_ALL.
	 * 
	 * @param selected true or false
	 */
	private void setSelectedCheckBoxAll(boolean selected) {
		int size = this.getModel().getSize();
		for (int i = 0; i < size; i++) {
			JCheckBox jCheckBox = (JCheckBox) this.getModel().getElementAt(i);
			if (StringUtils.equalsIgnoreCase(jCheckBox.getText(), CHECKBOX_ALL)) {
				jCheckBox.setSelected(selected);
				this.boxes.put(CHECKBOX_ALL, selected);
				break;
			}
		}
	}

	private void insertSelectedItem() {
		setLabel();
		if (!show && !((JCheckBox) getModel().getElementAt(0)).equals(selectedItem)) {
			insertItemAt(selectedItem, 0);
			setSelectedItem(selectedItem);
			selectedItem.setSelected(StringUtils.isNotBlank(label));
		}
	}

	class ComboBoxRenderer implements ListCellRenderer<Object> {
		private JLabel defaultLabel;

		public ComboBoxRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value instanceof Component) {
				Component c = (Component) value;
				if (isSelected) {
					c.setBackground(list.getSelectionBackground());
					c.setForeground(list.getSelectionForeground());
				} else {
					c.setBackground(list.getBackground());
					c.setForeground(list.getForeground());
				}
				return c;
			} else {
				if (defaultLabel == null) {
					defaultLabel = new JLabel(value.toString());
				} else {
					defaultLabel.setText(value.toString());
				}
				return defaultLabel;
			}
		}
	}
}
