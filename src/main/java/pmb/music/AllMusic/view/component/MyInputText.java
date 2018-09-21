package pmb.music.AllMusic.view.component;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import pmb.music.AllMusic.view.PanelUtils;

public class MyInputText extends JPanel {
	private static final Logger LOG = Logger.getLogger(MyInputText.class);
	private static final long serialVersionUID = 3848984166701854798L;

	private JComponent input;
	private JButton reset;

	public MyInputText(Class<? extends JComponent> type, int width) {
		super();
		try {
			this.input = (JComponent) type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.error("Error when instantiate the input of class: " + type.getName(), e);
		}
		this.add(this.input);
		Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
		this.reset = new JButton(icon);
		this.reset.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		this.reset.setFocusable(false);
		this.reset.addActionListener(l -> {
			if (isInputInstanceOf(JTextField.class)) {
				((JTextField) input).setText("");
			} else if (isInputInstanceOf(JComboBox.class)) {
				((JComboBox<?>) input).setSelectedItem(null);
			}
		});
		this.add(this.reset);
		PanelUtils.setSize(this.input, width - this.reset.getWidth(),
				PanelUtils.COMPONENT_HEIGHT + this.reset.getHeight());
	}

	public String getText() {
		if (isInputInstanceOf(JTextField.class)) {
			return ((JTextField) input).getText();
		} else if (isInputInstanceOf(JComboBox.class)) {
			return ((JComboBox<?>) input).getSelectedItem() == null ? ""
					: (String) ((JComboBox<?>) input).getSelectedItem();
		} else {
			return "";
		}
	}

	public void setText(String text) {
		if (isInputInstanceOf(JTextField.class)) {
			((JTextField) input).setText(text);
		} else if (isInputInstanceOf(JComboBox.class)) {
			((JComboBox<?>) input).setSelectedItem(text);
		}
	}

	public boolean isInputInstanceOf(Class<?> clazz) {
		return clazz.isInstance(input);
	}

	public JComponent getInput() {
		return input;
	}

}
