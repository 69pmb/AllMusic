package pmb.music.AllMusic.view.component;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Defines a panel with an input component and a reset button.
 *
 * @author PBR
 *
 */
public class MyInputText extends JPanel {
    private static final Logger LOG = LogManager.getLogger(MyInputText.class);
    private static final long serialVersionUID = 3848984166701854798L;

    private JComponent input;
    private JButton reset;

    /**
     * Constructor of {@link MyInputText}.
     *
     * @param type the class of the input component
     * @param width the width of the panel
     */
    public MyInputText(Class<? extends JComponent> type, int width) {
        super();
        try {
            this.input = type.getDeclaredConstructor().newInstance();
            this.input.addFocusListener(PanelUtils.selectAll);
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
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
        PanelUtils.setSize(this.input, Double.valueOf(width) - this.reset.getWidth(),
                Double.valueOf(ComponentBuilder.COMPONENT_HEIGHT) + this.reset.getHeight());
    }

    /**
     * Gets the text of the input component.
     *
     * @return a string
     */
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

    /**
     * Sets the text of the input component.
     *
     * @param a string
     */
    public void setText(String text) {
        if (isInputInstanceOf(JTextField.class)) {
            ((JTextField) input).setText(text);
        } else if (isInputInstanceOf(JComboBox.class)) {
            ((JComboBox<?>) input).setSelectedItem(text);
        }
    }

    private boolean isInputInstanceOf(Class<?> clazz) {
        return clazz.isInstance(input);
    }

    /**
     * Getter fo the input component.
     *
     * @return a JComponent
     */
    public JComponent getInput() {
        return input;
    }

}
