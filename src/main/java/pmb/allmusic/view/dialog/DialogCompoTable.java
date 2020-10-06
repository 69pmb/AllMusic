/**
 *
 */
package pmb.allmusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.exception.MajorException;
import pmb.allmusic.model.Composition;
import pmb.allmusic.utils.CompositionUtils;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.MiscUtils;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.TableBuilder;
import pmb.allmusic.view.model.CompoDialogModel;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.allmusic.view.popup.CompositionPopupMenu;

/**
 * Une "pop-up" permettant d'afficher une liste de {@link Composition}.
 *
 * @see {@link JDialog}
 * @author pmbroca
 */
public class DialogCompoTable extends AbstractFilterDialog<Composition> {
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

    private String fileName;
    private JDialog parent;

    private static final String[] header = { "Artiste", "Titre", "Type", "Classement", "Nombre de fichiers", "Score",
            "", "", "" };

    /**
     * Constructeur.
     * @param parent {@link JDialog} parent dialog
     * @param header {@link String} les entetes de la popup
     * @param compo {@code List<Composition>} la liste des fichier à afficher
     * @param fileName {@link String} file's name displayed in the dialog
     * @param height la hauteur de la popup
     */
    public DialogCompoTable(JDialog parent, String header, List<Composition> compo, String fileName, int height) {
        super(header, new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width - 100, height), compo,
                Predicate.not(Composition::isDeleted),
                list -> CompositionUtils.convertCompositionListToVector(list, fileName, true, true, false, true, false),
                new SortKey(index.get(Index.RANK), SortOrder.ASCENDING),
                "Filtrer les compositions supprimées");
        LOG.debug("Start DialogFileTable");
        this.fileName = fileName;
        this.parent = parent;
        initComposants();
        LOG.debug("End DialogFileTable");
    }

    @Override
    protected void initComposants() {
        LOG.debug("Start initComponent");
        try {
            setMyTable(new TableBuilder().withModelAndData(null, header, CompoDialogModel.class).withColumnIndex(index)
                    .withDefaultRowSorterListener().withMouseClickedActions((e, selectedRow) -> {
                        OngletPanel.setSelectTab(Constant.ONGLET_FICHIER);
                        dispose();
                        parent.dispose();
                        OngletPanel.getFichier().searchProgrammatically(fileName,
                                MiscUtils.stringToUuids(selectedRow.get(DialogCompoTable.getIndex().get(Index.UUID))));
                    }, true).withPopupMenu(new CompositionPopupMenu(null, this, DialogCompoTable.getIndex()))
                    .withKeyListener().build());
        } catch (MajorException e1) {
            LOG.error("An error occured when init Dialog Compo table", e1);
            return;
        }
        updateTableData();

        getDialog().setLayout(new BorderLayout());
        getDialog().add(new JScrollPane(getMyTable().getTable()), BorderLayout.CENTER);
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
