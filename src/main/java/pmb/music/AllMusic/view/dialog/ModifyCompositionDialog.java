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
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.Resize;

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void initComposants() {
        LOG.debug("Start initComposant");
        JPanel content = new JPanel();
        PanelUtils.setFlowLayout(content);
        Resize resize = new Resize(4, (int) getDialog().getSize().getWidth());

        // Artiste
        JTextField artist = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(content)
                .withLabel("Artiste : ").withInitialValue(compo.get(index.get(Index.ARTIST))).withResize(resize)
                .build();

        // Titre
        JTextField titre = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(content)
                .withLabel("Titre : ").withInitialValue(compo.get(index.get(Index.TITLE))).withResize(resize).build();

        // Type
        JComboBox<RecordType> type = new ComponentBuilder<JComboBox, RecordType>(JComboBox.class).withParent(content)
                .withLabel("Type : ").withValues(RecordType.values())
                .withInitialValue(RecordType.valueOf(compo.get(index.get(Index.TYPE)))).withResize(resize).build();

        // Deleted
        JCheckBox deleted = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(content)
                .withLabel("Supprimé : ").withResize(resize)
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
