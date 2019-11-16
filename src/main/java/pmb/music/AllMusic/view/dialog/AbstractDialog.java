package pmb.music.AllMusic.view.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

public abstract class AbstractDialog {
	private JDialog dialog;
	private Boolean sendData;

    /**
     * Constructor.
     * @param title of the dialog
     * @param dim its size
     * @param disposable if it can be closed by pressing escape key
     */
    public AbstractDialog(String title, Dimension dim, boolean disposable) {
        dialog = new JDialog((JFrame) null, title, true);
        dialog.setSize(dim);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);
        if (disposable) {
            dialog.getRootPane().registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }

	protected abstract void initComposants();

	public JButton buildCancelBtn(String label) {
		JButton cancelBouton = new JButton(label);
		cancelBouton.addActionListener((ActionEvent arg0) -> {
			dialog.setVisible(false);
			sendData = false;
		});
		return cancelBouton;
	}

	/**
	 * Make the dialog visible.
	 */
	public void show() {
		sendData = false;
		dialog.setVisible(true);
	}

	/**
	 * Validate the action and hide the dialog.
	 */
	public void validate() {
		dialog.setVisible(false);
		setSendData(true);
	}

	/**
	 * Make the dialog visible or not.
	 */
	public void setVisible(boolean b) {
		dialog.setVisible(b);
	}

	/**
	 * Dispose the dialog.
	 */
	public void dispose() {
		dialog.dispose();
	}

	public JDialog getDialog() {
		return dialog;
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	public void setSendData(Boolean sendData) {
		this.sendData = sendData;
	}

	/**
	 * @return if true the user has validated the modifications
	 */
	public Boolean getSendData() {
		return sendData;
	}
}
