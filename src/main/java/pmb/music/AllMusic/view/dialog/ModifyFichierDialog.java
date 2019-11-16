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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.panel.FichierPanel;

/**
 * Une "pop-up" permettant de modifier un fichier.
 *
 * @see {@link JDialog}
 */
public class ModifyFichierDialog extends AbstractDialog {
    private static final Logger LOG = LogManager.getLogger(ModifyFichierDialog.class);
    private final Vector<String> fichier;

    /**
     * Constructeur de {@link ModifyFichierDialog}.
     *
     * @param parent {@link JFrame} la fenetre parente
     * @param header {@link String} les entetes de la popup
     * @param modal {@code boolean} si la popup bloque l'utilisateur
     * @param fichier {@link Vector} le fichier à modifier
     */
    public ModifyFichierDialog(JFrame parent, String header, boolean modal, Vector<String> fichier) {
        super(header, new Dimension(1200, 150),true);
        LOG.debug("Start ModifyFichierDialog");
        this.fichier = fichier;
        initComposants();
        LOG.debug("End ModifyFichierDialog");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initComposants() {
        LOG.debug("Start initComposant");
        JPanel content = new JPanel();

        // FileName
        JTextField fileName = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
                .withLabel("Nom du fichier : ").withPanelWidth(450)
                .withInitialValue(String.valueOf(fichier.get(FichierPanel.getFichierindex().get(Index.FILE_NAME))))
                .withComponentWidth(430).withLabelWidth(430).build();

        // Publish Year
        JTextField publishYear = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
                .withLabel("Publication : ").withPanelWidth(80)
                .withInitialValue(String.valueOf(fichier.get(FichierPanel.getFichierindex().get(Index.PUBLISH)))).withComponentWidth(50)
                .withLabelWidth(80).build();

        // Range
        MyInputRange range = (MyInputRange) new ComponentBuilder<String>(MyInputRange.class).withParent(content)
                .withLabel("Année(s) du classement : ").withPanelWidth(300).withComponentWidth(170).withLabelWidth(200)
                .withInitialValue(fichier.get(FichierPanel.getFichierindex().get(Index.RANGE))).build();

        // Cat
        JComboBox<Cat> cat = (JComboBox<Cat>) new ComponentBuilder<Cat>(JComboBox.class).withParent(content)
                .withPanelWidth(180).withLabel("Catégorie : ").withValues(Cat.values())
                .withInitialValue(Cat.getByValue(fichier.get(FichierPanel.getFichierindex().get(Index.CAT))))
                .withComponentWidth(150).withLabelWidth(150).build();

        // Size
        JTextField size = (JTextField) new ComponentBuilder<String>(JTextField.class).withParent(content)
                .withLabel("Taille : ").withPanelWidth(50)
                .withInitialValue(String.valueOf(fichier.get(FichierPanel.getFichierindex().get(Index.FILE_SIZE)))).withComponentWidth(50)
                .withLabelWidth(50).build();

        // Sorted
        JCheckBox sorted = (JCheckBox) new ComponentBuilder<Boolean>(JCheckBox.class).withParent(content)
                .withLabel("Classé : ")
                .withInitialValue(
                        StringUtils.equalsIgnoreCase(fichier.get(FichierPanel.getFichierindex().get(Index.SORTED)), "oui") ? Boolean.TRUE
                                : Boolean.FALSE)
                .withPanelWidth(50).withComponentWidth(50).withLabelWidth(50).build();

        JPanel control = new JPanel();
        JButton okBouton = new JButton("OK");

        okBouton.addActionListener((ActionEvent arg0) -> {
            validate();
            fichier.set(FichierPanel.getFichierindex().get(Index.FILE_NAME), fileName.getText());
            fichier.set(FichierPanel.getFichierindex().get(Index.PUBLISH), publishYear.getText());
            fichier.set(FichierPanel.getFichierindex().get(Index.CAT), cat.getSelectedItem().toString());
            fichier.set(FichierPanel.getFichierindex().get(Index.FILE_SIZE), size.getText());
            fichier.set(FichierPanel.getFichierindex().get(Index.SORTED), sorted.isSelected() ? "Oui" : "Non");
            fichier.set(FichierPanel.getFichierindex().get(Index.RANGE),
                    range.getFirst().getText() + " - " + range.getSecond().getText());
        });

        control.add(okBouton);
        control.add(buildCancelBtn("Annuler"));

        getDialog().getContentPane().add(content, BorderLayout.CENTER);
        getDialog().getContentPane().add(control, BorderLayout.SOUTH);
        LOG.debug("End initComposant");
    }

    public Vector<String> getFichier() {
        return fichier;
    }
}
