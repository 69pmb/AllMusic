package pmb.music.AllMusic.view;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

public class MyInputText extends JPanel {
	private static final Logger LOG = Logger.getLogger(MyInputText.class);
	private static final long serialVersionUID = 3848984166701854798L;

	private JComponent input;
	private JButton reset;
	private Class<?> classe;

	public MyInputText(Class<?> classe, int width) {
		super();
		this.classe = classe;
		try {
			this.input = (JComponent) classe.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.error("Error when instantiate the input of class: " + classe.getName(), e);
		}
		this.add(this.input);
		Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
		this.reset = new JButton(icon);
		this.reset.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		this.reset.setFocusable(false);
		this.reset.addActionListener(l -> {
			if (this.classe == JTextField.class) {
				((JTextField) input).setText("");
			} else if (this.classe == JComboBox.class) {
				((JComboBox<?>) input).setSelectedItem(null);
			}
		});
		this.add(this.reset);
		PanelUtils.setSize(this.input, width - this.reset.getWidth(), PanelUtils.COMPONENT_HEIGHT);
	}

	public String getText() {
		if (this.classe == JTextField.class) {
			return ((JTextField) input).getText();
		} else if (this.classe == JComboBox.class) {
			return ((JComboBox<?>) input).getSelectedItem() == null ? ""
					: (String) ((JComboBox<?>) input).getSelectedItem();
		} else {
			return "";
		}
	}

	public void setText(String text) {
		if (this.classe == JTextField.class) {
			((JTextField) input).setText(text);
		} else if (this.classe == JComboBox.class) {
			((JComboBox<?>) input).setSelectedItem(text);
		}
	}

	public JComponent getInput() {
		return input;
	}

}
