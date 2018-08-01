package pmb.music.AllMusic.view;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class MyInputText extends JPanel {
	private static final long serialVersionUID = 3848984166701854798L;

	private JTextField input;
	private JButton reset;

	public MyInputText(int width) {
		super();
		this.input = new JTextField();
		this.add(this.input);
		Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
		this.reset = new JButton(icon);
		this.reset.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		this.reset.setFocusable(false);
		this.reset.addActionListener(l -> {
			input.setText("");
		});
		this.add(this.reset);
		PanelUtils.setSize(this.input, 220, PanelUtils.COMPONENT_HEIGHT);
	}

	public JTextField getInput() {
		return input;
	}

	public void setInput(JTextField input) {
		this.input = input;
	}

	public String getText() {
		return input.getText();
	}

	public void setText(String text) {
		input.setText(text);
	}

}
