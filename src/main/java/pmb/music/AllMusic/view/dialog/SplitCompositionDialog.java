/**
 *
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Dialog to split a composition's title.
 *
 * @see {@link JDialog}
 */
public class SplitCompositionDialog {
  private static final Logger LOG = LogManager.getLogger(SplitCompositionDialog.class);
  private JDialog dialog;
  private boolean sendData;
  private String title1;
  private String title2;
  private String info;

  /**
   * Constructor of {@link SplitCompositionDialog}.
   *
   * @param compo {@link Vector} la compo à modifier
   * @param title title to split
   * @param info others data of the composition
   */
  public SplitCompositionDialog(String title, String info) {
    LOG.debug("Start ModifyCompositionDialog");
    this.dialog = new JDialog((JFrame) null, "Modifier une composition", true);
    this.title1 = title;
    this.title2 = title;
    this.info = info;
    this.dialog.setSize(new Dimension(950, 400));
    this.dialog.setLocationRelativeTo(null);
    this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    this.dialog.setResizable(true);
    initComposant();
    this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    LOG.debug("End ModifyCompositionDialog");
  }

  private void initComposant() {
    LOG.debug("Start initComposant");
    JPanel content = PanelUtils.createBoxLayoutPanel(BoxLayout.Y_AXIS);
    new ComponentBuilder<String>(JLabel.class).withParent(content).withLabel("<html>" + info + "</html>").withPanelWidth(800).withLabelWidth(780).build();

    // Title1
    JTextField title1 = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
        .withLabel("Titre1 : ").withPanelWidth(300).withInitialValue(this.title1).withComponentWidth(270)
        .withLabelWidth(270).build();

    // Title2
    JTextField title2 = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
        .withLabel("Titre2 : ").withPanelWidth(300).withInitialValue(this.title2).withComponentWidth(270)
        .withLabelWidth(270).build();

    JPanel control = new JPanel();
    JButton okBouton = new JButton("OK");

    okBouton.addActionListener((ActionEvent arg0) -> {
      dialog.setVisible(false);
      sendData = true;
      this.title1 = title1.getText();
      this.title2 = title2.getText();
    });

    JButton cancelBouton = new JButton("Annuler");
    cancelBouton.addActionListener((ActionEvent arg0) -> {
      dialog.setVisible(false);
      sendData = false;
    });

    control.add(okBouton);
    control.add(cancelBouton);

    this.dialog.getContentPane().add(content, BorderLayout.CENTER);
    this.dialog.getContentPane().add(control, BorderLayout.SOUTH);
    LOG.debug("End initComposant");
  }

  /**
   * Affiche une {@link SplitCompositionDialog}.
   */
  public void showModifyCompositionDialog() {
    LOG.debug("Start showModifyCompositionDialog");
    this.sendData = false;
    this.dialog.setVisible(true);
    LOG.debug("End showModifyCompositionDialog");
  }

  /**
   * @return if true the user has validate the modifications
   */
  public boolean isSendData() {
    return sendData;
  }

  public String getTitle1() {
    return title1;
  }

  public String getTitle2() {
    return title2;
  }
}
