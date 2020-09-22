package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Dialogs to display exceptions with a panel to show its stack trace.
 *
 * @author PBR
 *
 */
public class ExceptionDialog extends AbstractDialog {
    private static final int DIALOG_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width - 100;
    private static final int DIALOG_HEIGHT = 140;

    // is error panel opened up
    private boolean open;
    private JLabel errorLabel;
    private JTextArea errorTextArea;
    private JTextArea exceptionTextArea;
    private JScrollPane exceptionTextAreaSP;
    private JButton okButton;
    private JButton viewButton;
    private JPanel topPanel;

    /**
     * Opens a dialog to show an exception.
     *
     * @param errorLabelText label of the exception
     * @param errorDescription message of the exception
     * @param e the exception whose stack trace is displayed
     */
    public ExceptionDialog(String errorLabelText, String errorDescription, Throwable e) {
        super("Error", new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT), true);
        initComposants();
        StringWriter errors = new StringWriter();
        if (e != null) {
            e.printStackTrace(new PrintWriter(errors));
        }
        errorTextArea.setText(errorDescription);
        errorLabel.setText(errorLabelText);
        exceptionTextArea.setText(errors.toString());
    }

    @Override
    protected void initComposants() {
        errorTextArea = new JTextArea();
        errorTextArea.setLineWrap(true);
        errorTextArea.setWrapStyleWord(true);
        errorTextArea.setEditable(false);
        errorLabel = new JLabel();
        exceptionTextArea = new JTextArea();
        exceptionTextArea.setWrapStyleWord(true);
        exceptionTextArea.setLineWrap(true);
        exceptionTextAreaSP = new JScrollPane(exceptionTextArea);
        JLabel iconLabel = new JLabel();
        iconLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("OK");
        buttonPanel.add(okButton);
        viewButton = new JButton("View Error");
        buttonPanel.add(viewButton);

        errorTextArea.setBackground(iconLabel.getBackground());
        JScrollPane textAreaSP = new JScrollPane(errorTextArea);
        textAreaSP.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        errorLabel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        exceptionTextAreaSP.setPreferredSize(new Dimension(100, 100));

        topPanel = new JPanel(new BorderLayout());
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(errorLabel, BorderLayout.CENTER);
        topPanel.add(textAreaSP, BorderLayout.SOUTH);
        this.getDialog().add(topPanel);
        this.getDialog().add(buttonPanel, BorderLayout.SOUTH);

        setUpListeners();
    }

    private void setUpListeners() {
        okButton.addActionListener((ActionEvent e) -> setVisible(false));
        viewButton.addActionListener((ActionEvent e) -> {
            if (open) {
                viewButton.setText("View Error");
                topPanel.remove(exceptionTextAreaSP);
                getDialog().setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
                exceptionTextAreaSP.setPreferredSize(new Dimension(100, 100));
            } else {
                viewButton.setText("Hide Error");
                topPanel.add(exceptionTextAreaSP, BorderLayout.SOUTH);
                getDialog().setSize(DIALOG_WIDTH, DIALOG_HEIGHT + 300);
                exceptionTextAreaSP.setPreferredSize(new Dimension(100, 300));
            }
            open = !open;
            getDialog().setLocationRelativeTo(null);
            topPanel.revalidate();
        });
    }
}
