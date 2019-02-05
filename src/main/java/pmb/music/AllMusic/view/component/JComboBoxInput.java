package pmb.music.AllMusic.view.component;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Created by PBR on 1 févr. 2019.
 */
public class JComboBoxInput<T> extends JPanel {

	private static final long serialVersionUID = -1407784810832692643L;

	private MyInputText input;
	private JComboBox<T> comboBox;

	public JComboBoxInput(MyInputText input, JComboBox<T> comboBox) {
		this.comboBox = comboBox;
		this.input = input;
		PanelUtils.setSize(comboBox, 45, ComponentBuilder.COMPONENT_HEIGHT);
		this.add(this.comboBox);
		this.add(this.input);
	}

	public MyInputText getInput() {
		return input;
	}

	public JComboBox<T> getComboBox() {
		return comboBox;
	}
}
