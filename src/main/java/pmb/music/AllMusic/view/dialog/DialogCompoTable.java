/**
 *
 */
package pmb.music.AllMusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.PanelUtils;
import pmb.music.AllMusic.view.TableBuilder;
import pmb.music.AllMusic.view.component.MyTable;
import pmb.music.AllMusic.view.model.CompoDialogModel;
import pmb.music.AllMusic.view.panel.OngletPanel;
import pmb.music.AllMusic.view.popup.CompositionPopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Composition}.
 *
 * @see {@link JDialog}
 * @author pmbroca
 */
public class DialogCompoTable extends AbstractDialog {
    private static final Logger LOG = LogManager.getLogger(DialogCompoTable.class);

    private static final ColumnIndex index = new ColumnIndex()
            .put(Index.ARTIST, 0)
            .put(Index.TITLE, 1)
            .put(Index.TYPE, 2)
            .put(Index.RANK, 3)
            .put(Index.FILE_SIZE, 4)
            .put(Index.SCORE, 5)
            .put(Index.DECILE, 6)
            .put(Index.DELETED, 7)
            .put(Index.UUID, 8);

    private List<Composition> compo = new ArrayList<>();
    private String fileName;
    private MyTable table;
    private JDialog parent;

    private static final String[] header = { "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers", "Score", "", "", "" };

    /**
     * Constructeur.
     * @param parent {@link JDialog} parent dialog
     * @param header {@link String} les entetes de la popup
     * @param compo {@code List<Composition>} la liste des fichier à afficher
     * @param fileName {@link String} file's name displayed in the dialog
     * @param height la hauteur de la popup
     */
    public DialogCompoTable(JDialog parent, String header, List<Composition> compo, String fileName, int height) {
        super(header, new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height), true);
        LOG.debug("Start DialogFileTable");
        this.compo = compo;
        this.fileName = fileName;
        this.parent = parent;
        initComposants();
        LOG.debug("End DialogFileTable");
    }

    @Override
    protected void initComposants() {
        LOG.debug("Start initComponent");
        try {
            table = new TableBuilder()
                    .withModelAndData(CompositionUtils.convertCompositionListToVector(compo, fileName, true, true, false,
                            true, false), header, CompoDialogModel.class)
                    .withDefaultRowSorterListener(null).withMouseClickAction(e -> {
                        Optional<Vector<String>> row = PanelUtils.getSelectedRow((JTable) e.getSource(),
                                e.getPoint());
                        if (SwingUtilities.isRightMouseButton(e)) {
                            LOG.debug("Start right mouse");
                            if (row.isPresent()) {
                                table.getPopupMenu().initDataAndPosition(e, row.get());
                                table.getPopupMenu().show(e);
                            }
                            LOG.debug("End right mouse");
                        } else if (e.getClickCount() == 2 && (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
                            OngletPanel.getOnglets().setSelectedIndex(OngletPanel.getTabIndex(Constant.ONGLET_FICHIER));
                            dispose();
                            parent.dispose();
                            OngletPanel.getFichier().searchProgrammatically(fileName, MiscUtils.stringToUuids(row.map(selected -> selected.get(DialogCompoTable.getIndex().get(Index.UUID))).orElse("")));
                        }
                    }).withPopupMenu(new CompositionPopupMenu(null, this, DialogCompoTable.getIndex())).withKeyListener()
                    .build();
            table.getRowSorter().toggleSortOrder(DialogCompoTable.getIndex().get(Index.RANK));
        } catch (MajorException e1) {
            LOG.error("An error occured when init Dialog Compo table", e1);
            return;
        }

        PanelUtils.colRenderer(table.getTable(), true, DialogCompoTable.getIndex());
        table.removeColumn(table.getColumnModel().getColumn(DialogCompoTable.getIndex().get(Index.DECILE)));
        table.removeColumn(table.getColumnModel().getColumn(DialogCompoTable.getIndex().get(Index.DELETED) - 1));
        table.removeColumn(table.getColumnModel().getColumn(DialogCompoTable.getIndex().get(Index.UUID) - 2));

        getDialog().setLayout(new BorderLayout());
        getDialog().add(new JScrollPane(table.getTable()), BorderLayout.CENTER);
        LOG.debug("End initComponent");
    }

    public static ColumnIndex getIndex() {
        return index;
    }

    public String getFileName() {
        return fileName;
    }

    public JDialog getParent() {
        return parent;
    }
}
