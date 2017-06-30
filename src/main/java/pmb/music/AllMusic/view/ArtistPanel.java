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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

    private List<Composition> list;

    private static final String[] title = { "Artiste", "Nombre d'occurrences", "Album", "Chanson" };

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
        table.setModel(model);
        table.setRowSorter(new TableRowSorter<TableModel>(model));
        table.getRowSorter().toggleSortOrder(1);
        table.getRowSorter().toggleSortOrder(1);
        colRenderer();
        updateArtistPanel();
        table.addMouseListener(new MouseAdapter() {

            @SuppressWarnings("unchecked")
            @Override
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable) e.getSource();
                if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    System.out.println("Start artist mouse");
                    // Affiche tous les fichiers de l'artiste double cliqué
                    Vector<String> v = (Vector<String>) ((ArtistModel) target.getModel()).getDataVector()
                            .get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
                    System.out.println(v);
                    List<Fichier> files = new ArrayList<>();
                    try {
                        List<Composition> findByArtist = CompositionUtils.findByArtist(list, v.get(0));
                        for (Composition composition : findByArtist) {
                            files.addAll(composition.getFiles());
                        }
                        DialogFileTable pop = new DialogFileTable(null, "Fichier", true, files, new Dimension(1500, 600));
                        pop.showDialogFileTable();
                    } catch (MyException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("End artist mouse");
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    System.out.println("Start artist right mouse");
                    // Copie dans le presse papier le nom de l'artiste
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
        
        table.addKeyListener(new KeyListener() {

            private int selectedRow = -1;// before start

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void keyReleased(KeyEvent e) {
                JTable target = (JTable) e.getSource();
                String keyChar = String.valueOf(e.getKeyChar());
                TableModel tableModel = target.getModel();
                int startRow = selectedRow;
                if (selectedRow == tableModel.getRowCount() - 1) {
                    startRow = -1;// Go before start
                }
                // Check each cell to see if it starts with typed char.
                // if so set corresponding row selected and return.
                for (int row = startRow + 1; row < tableModel.getRowCount(); row++) {
                    String value = ((Vector<String>) ((ArtistModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(row))).get(0);
                    if (value != null && value.toLowerCase().startsWith(keyChar.toLowerCase())) {
                        target.getSelectionModel().clearSelection();
                        target.getColumnModel().getSelectionModel().clearSelection();
                        target.changeSelection(row,0,true,false);
                        target.setRowSelectionInterval(row, row);
                        selectedRow = row;
                        return;
                    }
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(table), BorderLayout.CENTER);
    }

	public void updateArtistPanel() {
		System.out.println("Start updateArtistPanel");
        model.setRowCount(0);
		list = ImportXML.importXML(Constant.FINAL_FILE_PATH);
        model.setDataVector(CompositionUtils.convertCompositionListToArtistVector(list), new Vector<>(Arrays.asList(title)));
        colRenderer();
        model.fireTableDataChanged();
        table.getRowSorter().toggleSortOrder(1);
        table.getRowSorter().toggleSortOrder(1);
        table.repaint();
		System.out.println("End updateArtistPanel");
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
