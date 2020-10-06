package pmb.allmusic.view.panel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.model.RecordType;
import pmb.allmusic.utils.BatchEditUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.MiscUtils;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.PanelUtils;

/**
 * Tab launching process that edit files.
 * <ul>
 * Batchs List :
 * <li>Find duplicate compositions</li>
 * <li>Finds and edits composition with a slash in title and alone:
 * slashEdit</li>
 * <li>Checks if deleted for given a list of compositions</li>
 * <li>Mass deletion: for deleting an important amount of compositions</li>
 * </ul>
 */
public class BatchEditPanel {
    private static final Logger LOG = LogManager.getLogger(BatchEditPanel.class);
    private BatchPanel batchPanel;

    /**
     * Constructor of {@link BatchEditPanel}.
     */
    public BatchEditPanel() {
        super();
        LOG.debug("Start BatchEditPanel");
        batchPanel = new BatchPanel(4);
        findDuplicateComposition();
        slashEdit();
        checksIfDeleted();
        massDeletion();
        LOG.debug("End BatchEditPanel");
    }

    /**
     * Initialise les composants pour trouver les compositions en double (FDC).
     */
    private void findDuplicateComposition() {
        LOG.debug("Start findDuplicateComposition");
        JPanel fdc = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        JLabel fdcLabel = new JLabel("Recherche les compositions en double: ");
        PanelUtils.addComponent(fdc, fdcLabel, Component.LEFT_ALIGNMENT, 100);

        // Checkbox song
        JLabel fdcSongLabel = new JLabel("Chanson: ");
        JCheckBox fdcSong = new JCheckBox();
        fdcSong.setSelected(true);
        PanelUtils.addComponent(fdc, fdcSongLabel, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(fdc, fdcSong, Component.LEFT_ALIGNMENT, 100);

        // Checkbox album
        JLabel fdcAlbumLabel = new JLabel("Album: ");
        JCheckBox fdcAlbum = new JCheckBox();
        fdcAlbum.setSelected(true);
        PanelUtils.addComponent(fdc, fdcAlbumLabel, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(fdc, fdcAlbum, Component.LEFT_ALIGNMENT, 100);

        // Checkbox unmergeable
        JLabel fdcUnmergeableLabel = new JLabel("Ignorer les fichier non mergeables: ");
        JCheckBox fdcUnmergeable = new JCheckBox();
        fdcUnmergeable.setSelected(true);
        PanelUtils.addComponent(fdc, fdcUnmergeableLabel, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(fdc, fdcUnmergeable, Component.LEFT_ALIGNMENT, 100);

        // Checkbox year
        JLabel fdcYearLabel = new JLabel("Uniquement les fichiers YEAR: ");
        JCheckBox fdcYear = new JCheckBox();
        PanelUtils.addComponent(fdc, fdcYearLabel, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(fdc, fdcYear, Component.LEFT_ALIGNMENT, 100);

        // Launch Button
        JButton fdcBtn = ComponentBuilder.buildJButton("Go Compositions En Double", 200, Constant.ICON_GO);
        fdcBtn.setToolTipText("Fusionne les compositions identiques mais non détectées à la fusion classique.");
        fdcBtn.addActionListener((ActionEvent arg0) -> {
            LOG.debug("Start findDuplicateComposition");
            batchPanel.displayText("Start findDuplicateComposition: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                OngletPanel.getArtist().interruptUpdateArtist(true);
                batchPanel.setFileResult(BatchEditUtils.detectsDuplicateFinal(fdcSong.isSelected(), fdcAlbum.isSelected(),
                        fdcUnmergeable.isSelected(), fdcYear.isSelected(), batchPanel));
                OngletPanel.getFichier().updateData();
                OngletPanel.getArtist().updateArtistData();
                batchPanel.displayText("End findDuplicateComposition: " + MiscUtils.getCurrentTime(), false);
                LOG.debug("End findDuplicateComposition");
            }).start();
        });
        PanelUtils.addComponent(fdc, fdcBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(fdc);
        LOG.debug("End findDuplicateComposition");
    }

    private void slashEdit() {
        LOG.debug("Start slashEdit");
        JPanel slash = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel suspiciousLabel = new JLabel("Editer les compositions avec un slash: ");
        PanelUtils.addComponent(slash, suspiciousLabel, Component.LEFT_ALIGNMENT, 800);

        // Slash Btn
        JButton slashBtn = ComponentBuilder.buildJButton("Go Compositions Slash", 200, Constant.ICON_GO);
        slashBtn.setToolTipText("Editer des compositions seules contenant un slash dans leur titre");
        slashBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start slashEdit: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchEditUtils.slashEdit());
                batchPanel.displayText("End slashEdit: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(slash, slashBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(slash);
        LOG.debug("End slashEdit");
    }

    private void checksIfDeleted() {
        LOG.debug("Start checksIfDeleted");
        JPanel checks = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel checksLabel = new JLabel("Vérifie si supprimé: ");
        PanelUtils.addComponent(checks, checksLabel, Component.LEFT_ALIGNMENT, 100);

        // File or directory
        JCheckBox isDirectory = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(checks)
                .withLabel("Sélectionner un dossier : ").build();

        // File chooser
        JLabel selectedFile = new JLabel();
        FileFilter fileFilter = new FileNameExtensionFilter("txt", "txt");
        BatchPanel.buildFileChooser(checks, "Charge un fichier txt contenant des chansons", selectedFile, fileFilter)
        .whenCompleteAsync((chooser, err) -> checksIfDeleted(checks, isDirectory, selectedFile, fileFilter, chooser));

        batchPanel.getRoot().add(checks);
        LOG.debug("End checksIfDeleted");
    }

    public void checksIfDeleted(JPanel checks, JCheckBox isDirectory, JLabel selectedFile, FileFilter fileFilter,
            JFileChooser chooser) {
        // Type
        JComboBox<RecordType> type = buildTypeComboBox(checks);
        isDirectory.addActionListener((ActionEvent e) -> {
            int selectionMode;
            selectedFile.setText("");
            if (!isDirectory.isSelected()) {
                selectionMode = JFileChooser.FILES_ONLY;
                chooser.setFileFilter(fileFilter);
                chooser.addChoosableFileFilter(fileFilter);
            } else {
                selectionMode = JFileChooser.DIRECTORIES_ONLY;
                chooser.setFileFilter(null);
                chooser.resetChoosableFileFilters();
            }
            type.setEnabled(!isDirectory.isSelected());
            chooser.setFileSelectionMode(selectionMode);
        });

        // Bouton d'action
        JButton checksBtn = ComponentBuilder.buildJButton("Go Checks If Deleted", 200, Constant.ICON_GO);
        checksBtn.setToolTipText("Vérifie dans le fichier donné si les compositions sont supprimées");
        checksBtn.addActionListener((ActionEvent arg0) -> {
            if (selectedFile.getText() != null
                    && !StringUtils.equalsIgnoreCase(selectedFile.getText(), Constant.getResourcesDir())) {
                LOG.debug("End browse");
                batchPanel.displayText("Start checksIfDeleted: " + MiscUtils.getCurrentTime(), false);
                new Thread(() -> {
                    BatchEditUtils.checksIfDeleted(new File(selectedFile.getText()),
                            (RecordType) type.getSelectedItem());
                    batchPanel.displayText("End checksIfDeleted: " + MiscUtils.getCurrentTime(), false);
                }).start();
            } else {
                batchPanel.displayText("No selected file", false);
            }
        });
        PanelUtils.addComponent(checks, checksBtn, Component.RIGHT_ALIGNMENT, 100);
    }

    /**
     * Mass deletion: import a file of compositions to delete, by type. A search and
     * a confirmation is done for each.
     */
    private void massDeletion() {
        LOG.debug("Start massDeletion");
        JPanel massDeletion = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel massDeletionLabel = new JLabel("Suppression des compositions en masse: ");
        PanelUtils.addComponent(massDeletion, massDeletionLabel, Component.LEFT_ALIGNMENT, 100);

        // File chooser
        JLabel selectedFile = new JLabel();
        BatchPanel.buildFileChooser(massDeletion, "Charge un fichier csv contenant des compositions", selectedFile,
                new FileNameExtensionFilter("csv", "csv"));

        // Type
        JComboBox<RecordType> type = buildTypeComboBox(massDeletion);

        // Bouton d'action
        JButton massDeletionBtn = ComponentBuilder.buildJButton("Go Mass Deletion", 200, Constant.ICON_GO);
        massDeletionBtn.setToolTipText("Supprime en masse des compositions.");
        massDeletionBtn.addActionListener((ActionEvent arg0) -> {
            if (selectedFile.getText() != null
                    && !StringUtils.equalsIgnoreCase(selectedFile.getText(), Constant.getResourcesDir())) {
                LOG.debug("End browse");
                batchPanel.displayText("Start massDeletion: " + MiscUtils.getCurrentTime(), false);
                new Thread(() -> {
                    batchPanel.setFileResult(BatchEditUtils.massDeletion(type.getSelectedItem().toString(),
                            new File(selectedFile.getText())));
                    batchPanel.displayText("End massDeletion: " + MiscUtils.getCurrentTime(), false);
                }).start();
            } else {
                batchPanel.displayText("No selected file", false);
            }
        });
        PanelUtils.addComponent(massDeletion, massDeletionBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(massDeletion);
        LOG.debug("End massDeletion");
    }

    private static JComboBox<RecordType> buildTypeComboBox(JPanel parent) {
        JPanel typePanel = PanelUtils.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        JLabel typeLabel = ComponentBuilder.buildJLabel("Type : ", 50).orElse(null);
        JComboBox<RecordType> type = new JComboBox<>(new RecordType[] { RecordType.SONG, RecordType.ALBUM });
        PanelUtils.setSize(type, 100, ComponentBuilder.COMPONENT_HEIGHT);
        PanelUtils.addComponent(typePanel, typeLabel, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(typePanel, type, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(parent, typePanel, Component.RIGHT_ALIGNMENT, 100);
        return type;
    }

    public BatchPanel getBatchPanel() {
        return batchPanel;
    }
}
