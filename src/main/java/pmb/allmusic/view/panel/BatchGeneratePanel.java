package pmb.allmusic.view.panel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.time.Year;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.utils.BatchGenerateUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.MiscUtils;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.PanelUtils;

/**
 * Tab launching process that generate reports.
 * <ul>
 * Batchs List :
 * <li>Top year</li>
 * <li>Calculates in a file for each compositions, the number of files
 * associates to it and get the average for the file: averageOfFilesByFiles</li>
 * <li>Looks for weird file size: weirdFileSize</li>
 * <li>By looking in other files, guess the type of unknown compositions:
 * findUnknown</li>
 * <li>Generates general stats</li>
 * </ul>
 */
public class BatchGeneratePanel {
    private static final Logger LOG = LogManager.getLogger(BatchGeneratePanel.class);
    private BatchPanel batchPanel;

    /**
     * Constructor of {@link BatchGeneratePanel}.
     */
    public BatchGeneratePanel() {
        super();
        LOG.debug("Start BatchGeneratePanel");
        batchPanel = new BatchPanel(5);
        topYear();
        averageOfFilesByFiles();
        weirdFileSize();
        findUnknown();
        stats();
        LOG.debug("End BatchGeneratePanel");
    }

    /**
     * Generates csv reports for a specific year.
     *
     * @param score
     */
    private void topYear() {
        JPanel top = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel topLabel = new JLabel("Générer les tops: ");
        PanelUtils.addComponent(top, topLabel, Component.LEFT_ALIGNMENT, 100);

        String previousYear = String.valueOf(Year.now().getValue() - 1);
        // Year Begin
        JTextField yearBeginTop = new JTextField(previousYear);
        PanelUtils.setSize(yearBeginTop, 100, ComponentBuilder.COMPONENT_HEIGHT);
        PanelUtils.addComponent(top, yearBeginTop, Component.LEFT_ALIGNMENT, 80);

        // Year End
        JTextField yearEndTop = new JTextField(previousYear);
        PanelUtils.setSize(yearEndTop, 100, ComponentBuilder.COMPONENT_HEIGHT);
        PanelUtils.addComponent(top, yearEndTop, Component.LEFT_ALIGNMENT, 80);

        // Album limit
        JTextField albumLimit = new JTextField("10");
        PanelUtils.setSize(albumLimit, 100, ComponentBuilder.COMPONENT_HEIGHT);
        PanelUtils.addComponent(top, albumLimit, Component.LEFT_ALIGNMENT, 80);

        // Song limit
        JTextField songLimit = new JTextField("4");
        PanelUtils.setSize(songLimit, 100, ComponentBuilder.COMPONENT_HEIGHT);
        PanelUtils.addComponent(top, songLimit, Component.LEFT_ALIGNMENT, 80);

        // Checkbox deleted
        JLabel deletedLabel = new JLabel("Supprimés: ");
        JCheckBox deleted = new JCheckBox();
        deleted.setSelected(false);
        PanelUtils.addComponent(top, deletedLabel, Component.LEFT_ALIGNMENT, 0);
        PanelUtils.addComponent(top, deleted, Component.LEFT_ALIGNMENT, 80);

        // Bouton d'action
        JButton topBtn = ComponentBuilder.buildJButton("Go Tops", 200, Constant.ICON_GO);
        topBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start topYear: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchGenerateUtils.topYear(Integer.parseInt(yearBeginTop.getText()),
                        Integer.parseInt(yearEndTop.getText()), Integer.parseInt(albumLimit.getText()),
                        Integer.parseInt(songLimit.getText()), deleted.isSelected()));
                batchPanel.displayText("End topYear: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(top, topBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(top);
    }

    private void averageOfFilesByFiles() {
        JPanel average = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel averageLabel = new JLabel("Moyenne du nombre de fichier par fichier: ");
        PanelUtils.addComponent(average, averageLabel, Component.LEFT_ALIGNMENT, 800);

        // average btn
        JButton averageBtn = ComponentBuilder.buildJButton("Go Average By File", 200, Constant.ICON_GO);
        averageBtn.setToolTipText("Calcule la moyenne du nombre de fichier de chaque composition par fichier.");
        averageBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start average: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchGenerateUtils.averageOfFilesByFiles(batchPanel));
                batchPanel.displayText("End average: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(average, averageBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(average);
    }

    private void weirdFileSize() {
        JPanel weird = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel weirdLabel = new JLabel("Taille de fichier bizarre: ");
        PanelUtils.addComponent(weird, weirdLabel, Component.LEFT_ALIGNMENT, 800);

        // weird btn
        JButton weirdBtn = ComponentBuilder.buildJButton("Go Weird Size By File", 200, Constant.ICON_GO);
        weirdBtn.setToolTipText("Recherche les fichiers dont la taille est bizarre");
        weirdBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start weird: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchGenerateUtils.weirdFileSize());
                batchPanel.displayText("End weird: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(weird, weirdBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(weird);
    }

    private void findUnknown() {
        JPanel unknown = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel unknownLabel = new JLabel("Inconnu: ");
        PanelUtils.addComponent(unknown, unknownLabel, Component.LEFT_ALIGNMENT, 900);

        // Unknown btn
        JButton unknownsBtn = ComponentBuilder.buildJButton("Go Inconnus", 200, Constant.ICON_GO);
        unknownsBtn.setToolTipText("Trouve le type des compositions inconnues.");
        unknownsBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start findUnknown: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchGenerateUtils.findUnknown());
                batchPanel.displayText("End findUnknown: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(unknown, unknownsBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(unknown);
    }

    private void stats() {
        JPanel stat = PanelUtils.createBoxLayoutPanel(BoxLayout.X_AXIS);

        // Label
        JLabel statLabel = new JLabel("Statistiques: ");
        PanelUtils.addComponent(stat, statLabel, Component.LEFT_ALIGNMENT, 900);

        // Stat btn
        JButton statsBtn = ComponentBuilder.buildJButton("Go Statistiques", 200, Constant.ICON_GO);
        statsBtn.setToolTipText("Génère des statistiques.");
        statsBtn.addActionListener((ActionEvent arg0) -> {
            batchPanel.displayText("Start statistic: " + MiscUtils.getCurrentTime(), false);
            new Thread(() -> {
                batchPanel.setFileResult(BatchGenerateUtils.stats());
                batchPanel.displayText("End statistic: " + MiscUtils.getCurrentTime(), false);
            }).start();
        });
        PanelUtils.addComponent(stat, statsBtn, Component.RIGHT_ALIGNMENT, 100);

        batchPanel.getRoot().add(stat);
    }

    public BatchPanel getBatchPanel() {
        return batchPanel;
    }
}
