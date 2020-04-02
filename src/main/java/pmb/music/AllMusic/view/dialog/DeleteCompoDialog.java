package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.FichierUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.ComponentBuilder;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.FichierDialogModel;

/**
 * Une "pop-up" permettant de confirmer ou non la suppression d'une
 * {@link Composition}.
 *
 * @see {@link JDialog}
 * @author pmbroca
 */
public class DeleteCompoDialog extends AbstractDialog {
    private static final Logger LOG = LogManager.getLogger(DeleteCompoDialog.class);

    private static final String[] header = { "Artiste", "Oeuvre", "Type", "Auteur", "Nom du fichier",
            "Date de publication", "Categorie", "Dates", "Supprimés", "Taille", "Score", "Classement", "", "", "Classé" };

    private static final ColumnIndex index = new ColumnIndex()
            .put(Index.ARTIST, 0)
            .put(Index.TITLE, 1)
            .put(Index.TYPE, 2)
            .put(Index.AUTHOR, 3)
            .put(Index.FILE_NAME, 4)
            .put(Index.PUBLISH, 5)
            .put(Index.CAT, 6)
            .put(Index.PERCENT_DELETED, 8)
            .put(Index.FILE_SIZE, 9)
            .put(Index.SCORE, 10)
            .put(Index.RANK, 11)
            .put(Index.DELETED, 12)
            .put(Index.UUID, 13)
            .put(Index.SORTED, 14);

    private int size;

    // Components
    private JTextPane compoCsv;
    private JTextPane warning;
    private MyTable filesFound;

    /**
     * Constructeur de {@link DeleteCompoDialog}.
     *
     * @param parent {@link JFrame} la fenetre parente
     * @param size amount of compositions to delete
     */
    public DeleteCompoDialog(JFrame parent, int size) {
        super("", new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100,
                Toolkit.getDefaultToolkit().getScreenSize().height - 50), false);
        LOG.debug("Start DeleteCompoDialog");
        this.size = size;
        initComposants();
        LOG.debug("End DeleteCompoDialog");
    }

    @Override
    protected void initComposants() {
        LOG.debug("Start initComponent");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Csv compo
        JPanel compoCsvPanel = new JPanel();
        compoCsvPanel.setLayout(new BoxLayout(compoCsvPanel, BoxLayout.Y_AXIS));
        compoCsv = ComponentBuilder.initJTextPaneComponent(new Color(21, 77, 153), 20);
        compoCsvPanel.add(compoCsv);

        // Warning
        warning = ComponentBuilder.initJTextPaneComponent(new Color(255, 67, 67), 30);
        compoCsvPanel.add(warning);
        panel.add(new JScrollPane(compoCsvPanel));

        // Files found
        try {
            filesFound = new TableBuilder().withModelAndData(null, header, FichierDialogModel.class)
                    .withColumnIndex(index)
                    .withDefaultRowSorterListener().withKeyListener().build();
            PanelUtils.colRenderer(filesFound.getTable(), true, DeleteCompoDialog.getIndex());
            panel.add(new JScrollPane(filesFound.getTable()), BorderLayout.CENTER);
        } catch (MajorException e1) {
            LOG.error("An error occured when init Delete Compo Dialog table", e1);
            warning.setText(e1.getMessage());
            return;
        }

        JPanel btnPanel = new JPanel();

        JButton yes = new JButton("Oui");
        yes.addActionListener((ActionEvent a) -> validate());
        btnPanel.add(yes);
        btnPanel.add(buildCancelBtn("Non"));
        panel.add(btnPanel);

        getDialog().setLayout(new BorderLayout());
        getDialog().add(panel, BorderLayout.CENTER);
        LOG.debug("End initComponent");
    }

    /**
     * Update delete dialog infos.
     *
     * @param csv composition infos from csv file
     * @param found composition found from xml final file
     * @param index index in csv file
     * @param warning warning on csv composition like play count or rank
     */
    public void updateDialog(String csv, Composition found, int index, String warning) {
        setSendData(null);
        getDialog().setTitle(
                index + "/" + size + " - " + BigDecimal.valueOf(100D).setScale(2).multiply(new BigDecimal(index))
                .divide(new BigDecimal(size), RoundingMode.HALF_UP).doubleValue() + "%");
        compoCsv.setText(csv);

        this.warning.setText(warning);

        filesFound.getModel().setRowCount(0);
        filesFound.getModel().setDataVector(
                FichierUtils.convertCompositionListToFichierVector(Arrays.asList(found), true, false),
                new Vector<>(Arrays.asList(header)));
        PanelUtils.colRenderer(filesFound.getTable(), true, DeleteCompoDialog.getIndex());
        filesFound.removeColumn(filesFound.getColumnModel().getColumn(DeleteCompoDialog.getIndex().get(Index.DELETED)));
        filesFound.removeColumn(filesFound.getColumnModel().getColumn(DeleteCompoDialog.getIndex().get(Index.UUID) - 1));
        filesFound.getModel().fireTableDataChanged();
        filesFound.getTable().repaint();
    }

    public static ColumnIndex getIndex() {
        return index;
    }
}
