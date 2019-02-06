package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ExceptionDialog {
	private JDialog dialog;
	private int dialogWidth = 700;
	private int dialogHeight = 140;
	private JLabel iconLabel = new JLabel();
	// is error panel opened up
	private boolean open = false;
	private JLabel errorLabel = new JLabel();
	private JTextArea errorTextArea = new JTextArea("");
	private JTextArea exceptionTextArea = new JTextArea("");
	private JScrollPane exceptionTextAreaSP = new JScrollPane();
	private JButton okButton = new JButton("OK");
	private JButton viewButton = new JButton("View Error");
	private JPanel topPanel = new JPanel(new BorderLayout());

	public ExceptionDialog(String errorLabelText, String errorDescription, Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		this.dialog = new JDialog();
		dialog.setSize(dialogWidth, dialogHeight);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		errorTextArea.setText(errorDescription);
		errorLabel.setText(errorLabelText);
		exceptionTextArea.setText(errors.toString());
		exceptionTextArea.setWrapStyleWord(true);
		exceptionTextArea.setLineWrap(true);
		exceptionTextAreaSP = new JScrollPane(exceptionTextArea);
		iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
		setupUI();
		setUpListeners();
		this.dialog.getRootPane().registerKeyboardAction(k -> this.dialog.dispose(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public ExceptionDialog(String errorLabelText, Throwable e) {
		this(errorLabelText, null, e);
	}

	public void setupUI() {
		this.dialog.setTitle("Error");
		errorTextArea.setLineWrap(true);
		errorTextArea.setWrapStyleWord(true);
		errorTextArea.setEditable(false);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okButton);
		buttonPanel.add(viewButton);
		errorTextArea.setBackground(iconLabel.getBackground());
		JScrollPane textAreaSP = new JScrollPane(errorTextArea);
		textAreaSP.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		errorLabel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		exceptionTextAreaSP.setPreferredSize(new Dimension(100, 100));
		topPanel.add(iconLabel, BorderLayout.WEST);
		JPanel p = new JPanel(new BorderLayout());
		p.add(errorLabel, BorderLayout.NORTH);
		p.add(textAreaSP);
		topPanel.add(p);
		this.dialog.add(topPanel);
		this.dialog.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void setUpListeners() {
		okButton.addActionListener((ActionEvent e) -> ExceptionDialog.this.dialog.setVisible(false));
		viewButton.addActionListener((ActionEvent e) -> {
			if (open) {
				viewButton.setText("View Error");
				topPanel.remove(exceptionTextAreaSP);
				ExceptionDialog.this.dialog.setSize(dialogWidth, dialogHeight);
				topPanel.revalidate();
				open = false;
			} else {
				viewButton.setText("Hide Error");
				topPanel.add(exceptionTextAreaSP, BorderLayout.SOUTH);
				ExceptionDialog.this.dialog.setSize(dialogWidth, dialogHeight + 100);
				topPanel.revalidate();
				open = true;
			}
		});
	}

	public void setVisible(boolean b) {
		this.dialog.setVisible(b);
	}
}
