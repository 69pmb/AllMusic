/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;

/**
 * @author i2113mj
 *
 */
public class ArtistPanel extends JPanel {

    private static final long serialVersionUID = 2593372709628283573L;

    private JTable table;

    private ArtistModel model;
    
    List<Composition> list;

    private static final String title[] = { "Artiste", "Nombre d'occurrences", "Album", "Chanson" };

    public ArtistPanel() {
        super();
        table = new JTable();
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setBackground(UIManager.getColor("Label.background"));
        table.setFont(UIManager.getFont("Label.font"));
        table.setBorder(UIManager.getBorder("Label.border"));
        model = new ArtistModel(new Object[0][2], title);
        list = ImportXML.importXML(Constant.FINAL_FILE_PATH);
        model.setDataVector(CompositionUtils.convertCompositionListToArtistVector(list), new Vector<>(Arrays.asList(title)));
        table.setModel(model);
        table.setRowSorter(new TableRowSorter<TableModel>(model));
        table.getRowSorter().toggleSortOrder(1);
        table.getRowSorter().toggleSortOrder(1);
        colRenderer();
        table.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    System.out.println("Start artist mouse");
                    Vector<String> v = (Vector<String>) ((ArtistModel) target.getModel()).getDataVector()
                            .get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
                    System.out.println(v);
                    List<Fichier> files = new ArrayList<Fichier>();
                    try {
                        List<Composition> findByArtist = CompositionUtils.findByArtist(list, v.get(0));
                        for (Composition composition : findByArtist) {
                            files.addAll(composition.getFiles());
                        }
                        DialogFileTable pop = new DialogFileTable(null,"Fichier", true,files, new Dimension(1500, 600));
                        pop.showDialogFileTable();
                    } catch (MyException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("End artist mouse");
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    System.out.println("Start artist right mouse");
                    int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
                    if (rowAtPoint > -1) {
                        target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                    }
                    Vector<String> v = (Vector<String>) ((ArtistModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
                    StringSelection selection = new StringSelection(v.get(0));
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    System.out.println("End artist right mouse");
                }
            }
        });

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void colRenderer() {
        TableColumnModel modelecolonne = table.getColumnModel();
        int total = modelecolonne.getColumnCount();
        for (int i = 0; i < total; i++) {
            int taille = 0;
            int total2 = table.getRowCount();
            for (int j = 0; j < total2; j++) {
                int taille2 = table.getValueAt(j, i).toString().length() * 7; // determination
                // arbitraire
                if (taille2 > taille) {
                    taille = taille2;
                }
            }
            modelecolonne.getColumn(i).setPreferredWidth(taille + 50);
        }

        DefaultTableCellRenderer renderer = new EvenOddRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            renderer.setHorizontalAlignment(JLabel.CENTER);
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

}
