package pmb.allmusic.view.component;

import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.PanelUtils;

/**
 * Custom components merging a {@link MyInputText} with a {@link JComboBox}.
 *
 * @param <T> the type of the elements of the combo box
 */
public class JComboBoxInput<T> extends JPanel {

  private static final long serialVersionUID = -1407784810832692643L;

  private MyInputText input;
  private JComboBox<T> comboBox;
  public static final int COMBO_BOX_WIDTH = 45;

  /**
   * Constructor of {@link JComboBoxInput}.
   *
   * @param input the input
   * @param comboBox the combo box
   */
  public JComboBoxInput(MyInputText input, JComboBox<T> comboBox) {
    this.comboBox = comboBox;
    this.input = input;
    PanelUtils.setFlowLayout(this);
    add(comboBox);
    add(input);
    PanelUtils.setSize(comboBox, COMBO_BOX_WIDTH, ComponentBuilder.COMPONENT_HEIGHT);
    PanelUtils.setSize(
        this, input.getInputWidth() + COMBO_BOX_WIDTH * 2, ComponentBuilder.COMPONENT_HEIGHT);
    PanelUtils.setBorder(this, Color.red);
  }

  public MyInputText getInput() {
    return input;
  }

  public JComboBox<T> getComboBox() {
    return comboBox;
  }
}
