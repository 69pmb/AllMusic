package pmb.music.AllMusic.view.panel;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.utils.BatchUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;

/**
 * Tab launching process that check and validate.
 * <ul>
 * Batchs List :
 * <li>Find duplicate files</li>
 * <li>Missing XML files</li>
 * <li>Finds suspicious compositions: suspicious</li>
 * <li>Finds duplicates title with different artist: duplicateTitle</li>
 * <li>Checks filenames (Author + name + publishYear): validateFileName</li>
 * </ul>
 */
public class BatchCheckPanel {
    private static final Logger LOG = LogManager.getLogger(BatchCheckPanel.class);
    private BatchPanel batchPanel;

    /**
     * Constructor of {@link BatchCheckPanel}.
     */
    public BatchCheckPanel() {
        super();
        LOG.debug("Start BatchCheckPanel");
        batchPanel = new BatchPanel(5);
        findDuplicateFiles();
        missingXmlFiles();
        suspicious();
        duplicateTitle();
        validateFileName();
        LOG.debug("End BatchCheckPanel");
    }

    /**
     * Initialise les composants pour trouver les fichiers en double (FDF).
     */
    private void findDuplicateFiles() {
        JPanel fdf = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel fdfLabel = new JLabel("Recherche les fichiers en double: ");
        PanelUtils.addComponent(fdf, fdfLabel, Component.LEFT_ALIGNMENT, 700);

        // Bouton d'action
        JButton fdfBtn = ComponentBuilder.buildJButton("Go Fichiers En Double", 200, Constant.ICON_GO);
        fdfBtn.setToolTipText("Cherche les fichiers en double.");
        fdfBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start findDuplicateFiles: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchUtils.findDuplicateFiles());
                batchPanel.displayText("End findDuplicateFiles: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(fdf, fdfBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(fdf);
    }

    /**
     * Initialise les composants pour trouver les fichiers txt non importés (MXF).
     */
    private void missingXmlFiles() {
        JPanel mxf = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel mxfLabel = new JLabel("Rechercher les fichiers XML manquant: ");
        PanelUtils.addComponent(mxf, mxfLabel, Component.LEFT_ALIGNMENT, 700);

        // Bouton d'action
        JButton mxfBtn = ComponentBuilder.buildJButton("Go XML Manquant", 200, Constant.ICON_GO);
        mxfBtn.setToolTipText("Cherche si des fichiers txt n'ont pas d'équivalent XML.");
        mxfBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start missingXML: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchUtils.missingXML());
                batchPanel.displayText("End missingXML: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(mxf, mxfBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(mxf);
    }

    private void suspicious() {
        JPanel suspicious = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel suspiciousLabel = new JLabel("Trouver des compositions suspectes: ");
        PanelUtils.addComponent(suspicious, suspiciousLabel, Component.LEFT_ALIGNMENT, 800);

        // suspicious Btn
        JButton suspiciousBtn = ComponentBuilder.buildJButton("Go Compositions Suspectes", 200, Constant.ICON_GO);
        suspiciousBtn.setToolTipText("Trouve des compositions bizarres");
        suspiciousBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start findSuspiciousComposition: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchUtils.findSuspiciousComposition());
                batchPanel.displayText("End findSuspiciousComposition: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(suspicious, suspiciousBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(suspicious);
    }

    private void duplicateTitle() {
        JPanel duplicateTitle = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel duplicateTitleLabel = new JLabel("Trouver les chansons aux titres identiques: ");
        PanelUtils.addComponent(duplicateTitle, duplicateTitleLabel, Component.LEFT_ALIGNMENT, 800);

        // duplicateTitle Btn
        JButton duplicateTitleBtn = ComponentBuilder.buildJButton("Go Same Title", 200, Constant.ICON_GO);
        duplicateTitleBtn.setToolTipText("Trouve les chansons avec le même titre mais avec l'artiste différent");
        duplicateTitleBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start findDuplicateTitleComposition: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchUtils.findDuplicateTitleComposition());
                batchPanel.displayText("End findDuplicateTitleComposition: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(duplicateTitle, duplicateTitleBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(duplicateTitle);
    }

    private void validateFileName() {
        JPanel validate = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel validateLabel = new JLabel("Trouver les noms de fichier incorrect: ");
        PanelUtils.addComponent(validate, validateLabel, Component.LEFT_ALIGNMENT, 800);

        // validate Btn
        JButton validateBtn = ComponentBuilder.buildJButton("Go Filename Incorrect", 200, Constant.ICON_GO);
        validateBtn.setToolTipText("Trouve les noms de fichier incorrect");
        validateBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start findIncorectFileNames: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchUtils.findIncorrectFileNames());
                batchPanel.displayText("End findIncorectFileNames: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(validate, validateBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(validate);
    }

    public BatchPanel getBatchPanel() {
        return batchPanel;
    }

    public JButton getBatchFileBtn() {
        return batchPanel.getBatchFileBtn();
    }
}
