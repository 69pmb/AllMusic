package pmb.music.AllMusic.view.component;

import javax.swing.JPanel;

/**
 * @author PBR
 *
 */
public class MyInputRange extends JPanel {
	private static final long serialVersionUID = 3433759477031743510L;
	private MyInputText first;
	private MyInputText second;

	public MyInputRange(MyInputText first, MyInputText second) {
		this.first = first;
		this.second = second;
		this.add(this.first);
		this.add(this.second);
	}

	public MyInputText getFirst() {
		return first;
	}

	public void setFirst(MyInputText first) {
		this.first = first;
	}

	public MyInputText getSecond() {
		return second;
	}

	public void setSecond(MyInputText second) {
		this.second = second;
	}
}
