package pmb.music.AllMusic.view.component;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Created by PBR on 1 févr. 2019.
 */
public class JComboBoxInput extends JPanel {

	private static final long serialVersionUID = -1407784810832692643L;

	private MyInputText input;
	private JComboBox<String> comboBox;

	public void setInput(MyInputText input) {
		this.input = input;
		this.add(this.input);
	}

	public MyInputText getInput() {
		return input;
	}

	public JComboBox<String> getComboBox() {
		return comboBox;
	}

	public void setComboBox(JComboBox<String> comboBox) {
		this.comboBox = comboBox;
		this.add(this.comboBox);
	}
}
