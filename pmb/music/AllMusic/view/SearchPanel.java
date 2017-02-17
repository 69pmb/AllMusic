package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.collections.CollectionUtils;

import pmb.music.AllMusic.App;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.SearchUtils;

public class SearchPanel extends JPanel {

    private static final long serialVersionUID = 2593372709628283573L;

    private JLabel catLabel, authorLabel, publiLabel, rangeLabel, typeLabel, oeuvreLabel, fileNameLabel, artistLabel, countLabel;

    private JTextField author, publi, rangeB, rangeE, oeuvre, fileName, artist;

    private JTable result;

    private JComboBox<Cat> cat;

    private JComboBox<RecordType> type;
    
    private List<Composition> compoResult = new ArrayList<Composition>();
    
    private static final String  title[] = {"Artiste", "Oeuvre", "Type", "Nombre de fichiers", ""};
    
    private CompoModel model;
    
    private List<Composition> allCompo;

    public SearchPanel() {
        super();

        JPanel top = new JPanel();
        JPanel center = new JPanel(new BorderLayout());
        JPanel bottom = new JPanel(new GridLayout());
        
        allCompo = ImportXML.importXML(App.FINAL_FILE_PATH);

        JButton search = new JButton("Chercher");
        search.setBackground(Color.white);
        search.setPreferredSize(new Dimension(220, 60));
        search.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (CollectionUtils.isNotEmpty(allCompo)) {
                    Map<String, String> criteria = new HashMap<>();
                    criteria.put("artist", artist.getText());
                    criteria.put("oeuvre", oeuvre.getText());
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

                    compoResult = SearchUtils.search(allCompo, criteria);
//                    CompositionUtils.printCompoList(compoResult);
                    
                    model.setRowCount(0);
                    Object[][] convertListForJTable = CompositionUtils.convertListForJTable(compoResult);
                    for (int i = 0; i < convertListForJTable.length; i++) {
                        model.addRow(convertListForJTable[i]);
                        model.setValueAt(Boolean.valueOf((String) model.getValueAt(i, 4)),i, 4);
                    }
                    colRenderer();
                    countLabel.setText(compoResult.size() + " résultats");
                    model.fireTableDataChanged();
                    result.repaint();
                }
            }
        });
        top.add(search);

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

        // Oeuvre
        JPanel oeuvrePanel = new JPanel();
        oeuvrePanel.setPreferredSize(new Dimension(200, 60));
        oeuvreLabel = new JLabel("Oeuvre : ");
        oeuvre = new JTextField();
        oeuvre.setPreferredSize(new Dimension(150, 25));
        oeuvrePanel.add(oeuvreLabel);
        oeuvrePanel.add(oeuvre);
        firstLine.add(oeuvrePanel);

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

        center.add(firstLine, BorderLayout.NORTH);
        JPanel secondLine = new JPanel();

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
        secondLine.add(catPanel);

        // Publi
        JPanel publiPanel = new JPanel();
        publiPanel.setPreferredSize(new Dimension(200, 60));
        publiLabel = new JLabel("Année de publication : ");
        publi = new JTextField();
        publi.setPreferredSize(new Dimension(150, 25));
        publiPanel.add(publiLabel);
        publiPanel.add(publi);
        secondLine.add(publiPanel);
        
        // Nombre de résultat
        JPanel countPanel = new JPanel();
        countPanel.setPreferredSize(new Dimension(200, 60));
        countLabel = new JLabel("");
        countLabel.setForeground(new Color(8, 187, 81));
        Font labelFont = countLabel.getFont();
        countLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 30));
        countPanel.add(countLabel);
        secondLine.add(countPanel);
        
        JButton delete = new JButton("Supprimer les compositions sélectionnées");
        delete.setBackground(Color.white);
        delete.setPreferredSize(new Dimension(220, 60));
        delete.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Object> selected = model.getSelected();
                for (Object o : selected) {
                    System.out.println(o);
                    // TODO Remove from final those composition
                }
            }
        });
        secondLine.add(delete);

        center.add(secondLine, BorderLayout.CENTER);

        // result
        result = new JTable();
        result.setAutoCreateRowSorter(true);
        result.setRowHeight(30);
        result.setFillsViewportHeight(true);
        result.setBackground(UIManager.getColor("Label.background"));
        result.setFont(UIManager.getFont("Label.font"));
        result.setBorder(UIManager.getBorder("Label.border"));
        model = new CompoModel(new Object[0][5],title);
        result.setModel(model);
        result.setRowSorter(new TableRowSorter<TableModel>(model));
        result.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK)!=0) {
                    JTable target = (JTable)e.getSource();
                    Vector<String> v = (Vector<String>) ((CompoModel)target.getModel()).getDataVector().get(target.getRowSorter().convertRowIndexToModel(target.getSelectedRow()));
                    System.out.println(v);
                    List<Fichier> files = CompositionUtils.findByArtistOeuvreAndType(compoResult, v.get(0), v.get(1), v.get(2)).getFiles();
                    System.out.println(files);
                    DialogFileTable pop = new DialogFileTable(null,"Fichier", true,files);
                    pop.showDialogFileTable();
                 }
            }
        });

        bottom.add(new JScrollPane(result), BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(top, BorderLayout.NORTH);
        center.setBorder(BorderFactory.createTitledBorder(""));
        this.add(center, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);
    }
    
    private void colRenderer(){
        TableColumnModel modelecolonne = result.getColumnModel();
        int total = modelecolonne.getColumnCount();
        for (int i = 0; i < total; i++) {
            int taille = 0;
            int total2 = result.getRowCount();
            for (int j = 0; j < total2; j++) {
                // Partie determinant la taille de la cellule de ligne j et de
                // colonne i
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
//        result.removeColumn(result.getColumnModel().getColumn(5));
//        result.getColumnModel().getColumn(4).getCellRenderer(); 
//        MyBooleanRenderer boolRenderer = new MyBooleanRenderer();
//        boolRenderer.setHorizontalAlignment(JLabel.CENTER);
//        result.getColumnModel().getColumn(4).setCellRenderer(boolRenderer);
    }
}
