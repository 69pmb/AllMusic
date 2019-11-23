/**
 *
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;

/**
 * Une "pop-up" permettant de modifier une composition.
 *
 * @see {@link JDialog}
 */
public class ModifyCompositionDialog extends AbstractDialog {
    private static final Logger LOG = LogManager.getLogger(ModifyCompositionDialog.class);
    private final Vector<String> compo;
    private ColumnIndex index;

    /**
     * Constructeur de {@link ModifyCompositionDialog}.
     *
     * @param compo {@link Vector} la compo à modifier
     * @param index column index of rows
     */
    public ModifyCompositionDialog(Vector<String> compo, ColumnIndex index) {
        super("Modifier une composition", new Dimension(950, 150), true);
        LOG.debug("Start ModifyCompositionDialog");
        this.index = index;
        this.compo = new Vector<>(compo);
        initComposants();
        LOG.debug("End ModifyCompositionDialog");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initComposants() {
        LOG.debug("Start initComposant");
        JPanel content = new JPanel();

        // Artiste
        JTextField artist = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
                .withLabel("Artiste : ").withPanelWidth(250).withInitialValue(compo.get(index.get(Index.ARTIST)))
                .withComponentWidth(230).withLabelWidth(230).build();

        // Titre
        JTextField titre = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
                .withLabel("Titre : ").withPanelWidth(300).withInitialValue(compo.get(index.get(Index.TITLE)))
                .withComponentWidth(270).withLabelWidth(270).build();

        // Type
        JComboBox<RecordType> type = (JComboBox<RecordType>) new ComponentBuilder<RecordType>(JComboBox.class)
                .withParent(content).withPanelWidth(180).withLabel("Type : ").withValues(RecordType.values())
                .withInitialValue(RecordType.valueOf(compo.get(index.get(Index.TYPE)))).withComponentWidth(150).build();

        // Deleted
        JCheckBox deleted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(content)
                .withLabel("Supprimé : ").withPanelWidth(120).withComponentWidth(100)
                .withInitialValue(Boolean.parseBoolean(compo.get(index.get(Index.DELETED)))).build();

        JPanel control = new JPanel();
        JButton okBouton = new JButton("OK");

        okBouton.addActionListener((ActionEvent arg0) -> {
            validate();
            compo.set(index.get(Index.ARTIST), artist.getText());
            compo.set(index.get(Index.TITLE), titre.getText());
            compo.set(index.get(Index.TYPE), type.getSelectedItem().toString());
            compo.set(index.get(Index.DELETED), String.valueOf(deleted.isSelected()));
        });

        control.add(okBouton);
        control.add(buildCancelBtn("Annuler"));

        getDialog().getContentPane().add(content, BorderLayout.CENTER);
        getDialog().getContentPane().add(control, BorderLayout.SOUTH);
        LOG.debug("End initComposant");
    }

    /**
     * @return la composition à modifier
     */
    public Vector<String> getCompo() {
        return compo;
    }
}
