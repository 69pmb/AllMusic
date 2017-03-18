package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.utils.SearchUtils;

public class SearchPanel extends JPanel {

    private static final long serialVersionUID = 2593372709628283573L;

    private JLabel catLabel, authorLabel, publiLabel, rangeLabel, typeLabel, titreLabel, fileNameLabel, artistLabel, countLabel, deleteLabel;

    private JTextField author, publi, rangeB, rangeE, titre, fileName, artist;

    private JTable result;

    private JComboBox<Cat> cat;

    private JComboBox<RecordType> type;

    private List<Composition> compoResult = new ArrayList<Composition>();

    private static final String title[] = { "Artiste", "Titre", "Type", "Nombre de fichiers", "" };

    private CompoModel model;

    public SearchPanel(final ArtistPanel artist2) {
        super();
        System.out.println("Start SearchPanel");
        this.setLayout(new GridLayout(2, 1));

        JPanel header = new JPanel();
        header.setLayout(new GridLayout(2, 1));
        JPanel top = new JPanel();
        AbstractAction searchAction = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent arg0) {

                System.out.println("Start search");
                deleteLabel.setText("");
                List<Composition> allCompo = ImportXML.importXML(Constant.FINAL_FILE_PATH);
                if (CollectionUtils.isNotEmpty(allCompo)) {
                    Map<String, String> criteria = new HashMap<>();
                    criteria.put("artist", artist.getText());
                    criteria.put("titre", titre.getText());
                    if (type.getSelectedItem() != null) {
                        criteria.put("type", type.getSelectedItem().toString());
                    }
                    criteria.put("publish", publi.getText());
                    criteria.put("fileName", fileName.getText());
                    criteria.put("auteur", author.getText());
                    if (cat.getSelectedItem() != null) {
                        criteria.put("cat", cat.getSelectedItem().toString());
                    }
                    criteria.put("dateB", rangeB.getText());
                    criteria.put("dateE", rangeE.getText());

                    compoResult = new ArrayList<Composition>();
                    compoResult.addAll(SearchUtils.searchContains(allCompo, criteria));
                    updateTable();
                }
                System.out.println("End search");
            }
        };

        JButton search = new JButton("Chercher");
        search.setBackground(Color.white);
        search.setPreferredSize(new Dimension(220, 60));
        search.addActionListener(searchAction);
        search.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "Enter_pressed");
        search.getActionMap().put("Enter_pressed", searchAction);
        top.add(search);

        // Clear Btn
        JButton clear = new JButton("Réinitialiser recherche");
        clear.setBackground(Color.white);
        clear.setPreferredSize(new Dimension(200, 60));
        AbstractAction cleanAction = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                artist.setText("");
                titre.setText("");
                type.setSelectedItem(null);
                publi.setText("");
                fileName.setText("");
                author.setText("");
                cat.setSelectedItem(null);
                rangeB.setText("");
                rangeE.setText("");
                deleteLabel.setText("");
                countLabel.setText("");
            }
        };
        clear.addActionListener(cleanAction);
        top.add(clear);

        // Delete Btn
        JButton delete = new JButton("Supprimer les compositions sélectionnées");
        delete.setBackground(Color.white);
        delete.setPreferredSize(new Dimension(400, 60));
        delete.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Start delete");
                List<Object> selected = model.getSelected();
                List<Composition> importXML = ImportXML.importXML(Constant.FINAL_FILE_PATH);
                for (Object o : selected) {
                    Vector<String> v = (Vector<String>) o;
                    try {
                        Composition toRemove = CompositionUtils.findByArtistTitreAndType(importXML, v.get(0), v.get(1), v.get(2));
                        compoResult.remove(compoResult.indexOf(toRemove));
                        importXML.remove(importXML.indexOf(toRemove));
                        CompositionUtils.removeCompositionsInFiles(toRemove);
                    } catch (MyException e1) {
                        e1.printStackTrace();
                    }
                }
                try {
                    ExportXML.exportXML(importXML, "final");
                    artist2.updateArtistPanel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                updateTable();
                deleteLabel.setText(selected.size() + " élément(s) supprimée(s)");
                System.out.println("End delete");
            }
        });
        top.add(delete);
        header.add(top);

        JPanel firstLine = new JPanel();

        // Artiste
        JPanel artistPanel = new JPanel();
        artistPanel.setPreferredSize(new Dimension(200, 60));
        artistLabel = new JLabel("Artiste : ");
        artist = new JTextField();
        artist.setPreferredSize(new Dimension(150, 25));
        artistPanel.add(artistLabel);
        artistPanel.add(artist);
        firstLine.add(artistPanel);

        // Titre
        JPanel titrePanel = new JPanel();
        titrePanel.setPreferredSize(new Dimension(180, 60));
        titreLabel = new JLabel("Titre : ");
        titre = new JTextField();
        titre.setPreferredSize(new Dimension(150, 25));
        titrePanel.add(titreLabel);
        titrePanel.add(titre);
        firstLine.add(titrePanel);

        // Nom du fichier
        JPanel fileNamePanel = new JPanel();
        fileNamePanel.setPreferredSize(new Dimension(400, 60));
        fileNameLabel = new JLabel("Nom du fichier : ");
        fileName = new JTextField();
        fileName.setPreferredSize(new Dimension(350, 25));
        fileNamePanel.add(fileNameLabel);
        fileNamePanel.add(fileName);
        firstLine.add(fileNamePanel);

        // Auteur
        JPanel authorPanel = new JPanel();
        authorPanel.setPreferredSize(new Dimension(200, 60));
        authorLabel = new JLabel("Auteur : ");
        author = new JTextField();
        author.setPreferredSize(new Dimension(150, 25));
        authorPanel.add(authorLabel);
        authorPanel.add(author);
        firstLine.add(authorPanel);

        // Type
        JPanel typePanel = new JPanel();
        typePanel.setPreferredSize(new Dimension(180, 60));
        typeLabel = new JLabel("Type : ");
        type = new JComboBox<RecordType>();
        type.addItem(null);
        RecordType[] valuesType = RecordType.values();
        for (int i = 0; i < valuesType.length; i++) {
            type.addItem(valuesType[i]);
        }
        type.setPreferredSize(new Dimension(150, 25));
        typePanel.add(typeLabel);
        typePanel.add(type);
        firstLine.add(typePanel);

        // Range
        JPanel rangePanel = new JPanel();
        rangePanel.setPreferredSize(new Dimension(310, 60));
        rangeLabel = new JLabel("Année(s) du classement :                ");
        rangeB = new JTextField();
        rangeE = new JTextField();
        rangeB.setPreferredSize(new Dimension(150, 25));
        rangeE.setPreferredSize(new Dimension(150, 25));
        rangePanel.add(rangeLabel);
        rangePanel.add(rangeB);
        rangePanel.add(rangeE);
        firstLine.add(rangePanel);

        // Categorie
        JPanel catPanel = new JPanel();
        catPanel.setPreferredSize(new Dimension(200, 60));
        catLabel = new JLabel("Catégorie : ");
        cat = new JComboBox<Cat>();
        cat.addItem(null);
        Cat[] values = Cat.values();
        for (int i = 0; i < values.length; i++) {
            cat.addItem(values[i]);
        }
        cat.setPreferredSize(new Dimension(150, 25));
        catPanel.add(catLabel);
        catPanel.add(cat);
        firstLine.add(catPanel);

        // Publi
        JPanel publiPanel = new JPanel();
        publiPanel.setPreferredSize(new Dimension(200, 60));
        publiLabel = new JLabel("Année de publication : ");
        publi = new JTextField();
        publi.setPreferredSize(new Dimension(150, 25));
        publiPanel.add(publiLabel);
        publiPanel.add(publi);
        firstLine.add(publiPanel);

        // Nombre de résultat
        JPanel countPanel = new JPanel();
        countPanel.setPreferredSize(new Dimension(200, 60));
        countLabel = new JLabel("");
        countLabel.setForeground(new Color(8, 187, 81));
        Font labelFont = countLabel.getFont();
        countLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 30));
        countPanel.add(countLabel);
        firstLine.add(countPanel);
        
        // Nombre de suppression
        JPanel deletePanel = new JPanel();
        deletePanel.setPreferredSize(new Dimension(400, 60));
        deleteLabel = new JLabel("");
        deleteLabel.setForeground(new Color(8, 187, 81));
        Font labelFont2 = deleteLabel.getFont();
        deleteLabel.setFont(new Font(labelFont2.getName(), labelFont2.getStyle(), 30));
        deletePanel.add(deleteLabel);
        firstLine.add(deletePanel);

        header.add(firstLine);
        this.add(header);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        // result
        result = new JTable();
        result.setAutoCreateRowSorter(true);
        result.setRowHeight(30);
        result.setFillsViewportHeight(true);
        result.setBackground(UIManager.getColor("Label.background"));
        result.setFont(UIManager.getFont("Label.font"));
        result.setBorder(UIManager.getBorder("Label.border"));
        model = new CompoModel(new Object[0][5], title);
        result.setModel(model);
        result.setRowSorter(new TableRowSorter<TableModel>(model));
        result.addKeyListener(new KeyListener() {

            private int selectedRow = -1;// before start

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void keyReleased(KeyEvent e) {
                JTable target = (JTable) e.getSource();
                String keyChar = String.valueOf(e.getKeyChar());
                TableModel model = target.getModel();
                int startRow = selectedRow;
                if (selectedRow == model.getRowCount() - 1) {
                    startRow = -1;// Go before start
                }
                // Check each cell to see if it starts with typed char.
                // if so set corresponding row selected and return.
                for (int row = startRow + 1; row < model.getRowCount(); row++) {
                    String value = ((Vector<String>) ((CompoModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(row))).get(0);
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
        result.addMouseListener(new MouseAdapter() {

            @SuppressWarnings("unchecked")
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    System.out.println("Start result mouse");
                    // Ouvre une popup pour afficher les fichiers de la composition séléectionnée
                    JTable target = (JTable) e.getSource();
                    Vector<String> v = (Vector<String>) ((CompoModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
                    List<Fichier> files;
                    try {
                        files = CompositionUtils.findByArtistTitreAndType(compoResult, v.get(0), v.get(1), v.get(2)).getFiles();
                        DialogFileTable pop = new DialogFileTable(null, "Fichier", true, files, new Dimension(1500, 400));
                        pop.showDialogFileTable();
                    } catch (MyException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("End result mouse");
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    System.out.println("Start right mouse");
                    // Copie dans le clipboard l'artist et l'oeuvre
                    JTable target = (JTable) e.getSource();
                    int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, new Point(e.getX(), e.getY()), target));
                    if (rowAtPoint > -1) {
                        target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                    }
                    Vector<String> v = (Vector<String>) ((CompoModel) target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
                    StringSelection selection = new StringSelection(v.get(0) + " " + v.get(1));
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    System.out.println("End right mouse");
                }
            }
        });
        bottom.add(new JScrollPane(result), BorderLayout.CENTER);

        this.add(bottom);
        System.out.println("End SearchPanel");
    }

    private void colRenderer() {
        TableColumnModel modelecolonne = result.getColumnModel();
        int total = modelecolonne.getColumnCount();
        for (int i = 0; i < total; i++) {
            int taille = 0;
            int total2 = result.getRowCount();
            for (int j = 0; j < total2; j++) {
                int taille2 = result.getValueAt(j, i).toString().length() * 7; // determination
                // arbitraire
                if (taille2 > taille) {
                    taille = taille2;
                }
            }
            modelecolonne.getColumn(i).setPreferredWidth(taille + 50);
        }

        DefaultTableCellRenderer renderer = new EvenOddRenderer();
        for (int i = 0; i < result.getColumnCount() - 1; i++) {
            renderer.setHorizontalAlignment(JLabel.CENTER);
            result.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public void updateTable() {
        model.setRowCount(0);
        model.setDataVector(CompositionUtils.convertCompositionListToVector(compoResult), new Vector<>(Arrays.asList(title)));
        colRenderer();
        countLabel.setText(compoResult.size() + " résultats");
        model.fireTableDataChanged();
        result.getRowSorter().toggleSortOrder(3);
        result.getRowSorter().toggleSortOrder(3);
        result.repaint();
    }

}
