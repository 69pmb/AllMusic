/**
 *
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Dialog to split a composition's title.
 *
 * @see {@link JDialog}
 */
public class SplitCompositionDialog extends AbstractDialog {
    private static final Logger LOG = LogManager.getLogger(SplitCompositionDialog.class);
    private String title1;
    private String title2;
    private String info;

    /**
     * Constructor of {@link SplitCompositionDialog}.
     *
     * @param compo {@link Vector} la compo à modifier
     * @param title title to split
     * @param info  others data of the composition
     */
    public SplitCompositionDialog(String title, String info) {
        super(title, new Dimension(950, 400), true);
        LOG.debug("Start ModifyCompositionDialog");
        this.title1 = title;
        this.title2 = title;
        this.info = info;
        initComposants();
        LOG.debug("End ModifyCompositionDialog");
    }

    @Override
    protected void initComposants() {
        LOG.debug("Start initComposant");
        JPanel content = new JPanel();
        PanelUtils.setFlowLayout(content);
        int width = (int) getDialog().getSize().getWidth();

        new ComponentBuilder<JLabel, String>(JLabel.class).withParent(content).withLabel("<html>" + info + "</html>")
        .withPanelWidth(width).withHeight(ComponentBuilder.PANEL_HEIGHT).build();

        // Title1
        JTextField text1 = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(content)
                .withLabel("Titre1 : ").withInitialValue(title1).withPanelWidth(width).build();

        // Title2
        JTextField text2 = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(content)
                .withLabel("Titre2 : ").withInitialValue(title2).withPanelWidth(width).build();

        JPanel control = new JPanel();
        JButton okBouton = new JButton("OK");

        okBouton.addActionListener((ActionEvent arg0) -> {
            validate();
            title1 = text1.getText();
            title2 = text2.getText();
        });

        control.add(okBouton);
        control.add(buildCancelBtn("Annuler"));

        getDialog().getContentPane().add(content, BorderLayout.CENTER);
        getDialog().getContentPane().add(control, BorderLayout.SOUTH);
        LOG.debug("End initComposant");
    }

    public String getTitle1() {
        return title1;
    }

    public String getTitle2() {
        return title2;
    }
}
