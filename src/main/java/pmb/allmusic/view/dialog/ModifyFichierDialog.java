/** */
package pmb.allmusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.model.Cat;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.PanelUtils;
import pmb.allmusic.view.Resize;
import pmb.allmusic.view.component.MyInputRange;
import pmb.allmusic.view.panel.FichierPanel;

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
    super(header, new Dimension(1200, 150), true);
    LOG.debug("Start ModifyFichierDialog");
    this.fichier = fichier;
    initComposants();
    LOG.debug("End ModifyFichierDialog");
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void initComposants() {
    LOG.debug("Start initComposant");
    JPanel content = new JPanel();
    PanelUtils.setFlowLayout(content);
    Resize resize = new Resize(6, (int) getDialog().getSize().getWidth());

    // FileName
    JTextField fileName =
        new ComponentBuilder<JTextField, String>(JTextField.class)
            .withParent(content)
            .withLabel("Nom du fichier : ")
            .withInitialValue(
                String.valueOf(fichier.get(FichierPanel.getFichierindex().get(Index.FILE_NAME))))
            .withResize(resize)
            .build();

    // Publish Year
    JTextField publishYear =
        new ComponentBuilder<JTextField, String>(JTextField.class)
            .withParent(content)
            .withLabel("Publication : ")
            .withResize(resize)
            .withInitialValue(
                String.valueOf(fichier.get(FichierPanel.getFichierindex().get(Index.PUBLISH))))
            .build();

    // Range
    MyInputRange range =
        new ComponentBuilder<MyInputRange, String>(MyInputRange.class)
            .withParent(content)
            .withLabel("Année(s) du classement : ")
            .withResize(resize)
            .withInitialValue(fichier.get(FichierPanel.getFichierindex().get(Index.RANGE)))
            .build();

    // Cat
    JComboBox<Cat> cat =
        new ComponentBuilder<JComboBox, Cat>(JComboBox.class)
            .withParent(content)
            .withLabel("Catégorie : ")
            .withValues(Cat.values())
            .withInitialValue(
                Cat.getByValue(fichier.get(FichierPanel.getFichierindex().get(Index.CAT))))
            .withResize(resize)
            .build();

    // Size
    JTextField size =
        new ComponentBuilder<JTextField, String>(JTextField.class)
            .withParent(content)
            .withLabel("Taille : ")
            .withResize(resize)
            .withInitialValue(
                String.valueOf(fichier.get(FichierPanel.getFichierindex().get(Index.FILE_SIZE))))
            .build();

    // Sorted
    JCheckBox sorted =
        new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class)
            .withParent(content)
            .withLabel("Classé : ")
            .withInitialValue(
                StringUtils.equalsIgnoreCase(
                        fichier.get(FichierPanel.getFichierindex().get(Index.SORTED)), "oui")
                    ? Boolean.TRUE
                    : Boolean.FALSE)
            .withResize(resize)
            .withResize(resize)
            .build();

    JPanel control = new JPanel();
    JButton okBouton = new JButton("OK");

    okBouton.addActionListener(
        (ActionEvent arg0) -> {
          validate();
          fichier.set(FichierPanel.getFichierindex().get(Index.FILE_NAME), fileName.getText());
          fichier.set(FichierPanel.getFichierindex().get(Index.PUBLISH), publishYear.getText());
          fichier.set(
              FichierPanel.getFichierindex().get(Index.CAT), cat.getSelectedItem().toString());
          fichier.set(FichierPanel.getFichierindex().get(Index.FILE_SIZE), size.getText());
          fichier.set(
              FichierPanel.getFichierindex().get(Index.SORTED),
              sorted.isSelected() ? "Oui" : "Non");
          fichier.set(
              FichierPanel.getFichierindex().get(Index.RANGE),
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
