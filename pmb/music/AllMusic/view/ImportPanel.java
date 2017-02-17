/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import pmb.music.AllMusic.App;
import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CleanFile;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;

/**
 * @author i2113mj
 *
 */
public class ImportPanel extends JPanel {

    private static final long serialVersionUID = 5796304304079887263L;

    private JLabel catLabel, authorLabel, publiLabel, rangeLabel, sizeLabel, sortedLabel, typeLabel, dateLabel, 
    nameLabel, lineLabel, separatorLabel, orderLabel, getFinalLabel, lastLinesLabel, characterToRemoveLabel;

    private JTextField author, publi, rangeB, rangeE, size, name, date, line, separator, lastL1, lastL2, characterToRemove;

    private JTextArea resultLabel;
    
    private File file;
    
    private JComboBox<Cat> cat;

    private JComboBox<RecordType> type;

    private Fichier fichier;

    private String absolutePathFile;

    private JCheckBox sorted, order, getFinal;

    private RecordType determineType;

    public ImportPanel() {
        super();

        JPanel top = new JPanel();
        JPanel center = new JPanel(new BorderLayout());
        JPanel bottom = new JPanel();

        JButton browse = new JButton("Parcourir");
        browse.setBackground(Color.white);
        browse.setPreferredSize(new Dimension(220, 60));
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                file = addBrowsingFile();
                // TODO Pouvoir sélectionner plusieurs fichiers
                if (file != null) {
                    absolutePathFile = file.getAbsolutePath();
                    fichier = ImportFile.convertOneFile(file);
                    List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
                    fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(0)));
                    determineType = ImportFile.determineType(file.getName());
                    name.setText(fichier.getFileName());
                    author.setText(fichier.getAuthor());
                    date.setText(ExportXML.SDF_DTTM.format(fichier.getCreationDate()));
                    cat.setSelectedItem(fichier.getCategorie());
                    publi.setText(String.valueOf(fichier.getPublishYear()));
                    type.setSelectedItem(determineType);
                    rangeB.setText(String.valueOf(fichier.getRangeDateBegin()));
                    rangeE.setText(String.valueOf(fichier.getRangeDateEnd()));
                    sorted.setSelected(fichier.getSorted());
                    size.setText(String.valueOf(fichier.getSize()));
                    line.setText(randomLineAndLastLines.get(0));
                    lastL1.setText(randomLineAndLastLines.get(1));
                    lastL2.setText(randomLineAndLastLines.get(2));
                    separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(0)));
                }
            }
        });
        top.add(browse);

        JPanel firstLine = new JPanel();

        // Nom du fichier
        JPanel namePanel = new JPanel();
        namePanel.setPreferredSize(new Dimension(400, 60));
        nameLabel = new JLabel("Nom du fichier : ");
        name = new JTextField();
        name.setPreferredSize(new Dimension(350, 25));
        namePanel.add(nameLabel);
        namePanel.add(name);
        firstLine.add(namePanel);

        // Auteur
        JPanel authorPanel = new JPanel();
        authorPanel.setPreferredSize(new Dimension(200, 60));
        authorLabel = new JLabel("Auteur : ");
        author = new JTextField();
        author.setPreferredSize(new Dimension(150, 25));
        authorPanel.add(authorLabel);
        authorPanel.add(author);
        firstLine.add(authorPanel);

        // Date de creation
        JPanel datePanel = new JPanel();
        datePanel.setPreferredSize(new Dimension(200, 60));
        dateLabel = new JLabel("Date de création : ");
        date = new JTextField();
        date.setPreferredSize(new Dimension(150, 25));
        date.setEnabled(false);
        datePanel.add(dateLabel);
        datePanel.add(date);
        firstLine.add(datePanel);

        // Type
        JPanel typePanel = new JPanel();
        typePanel.setPreferredSize(new Dimension(180, 60));
        typeLabel = new JLabel("Type : ");
        type = new JComboBox<RecordType>();
        RecordType[] valuesType = RecordType.values();
        for (int i = 0; i < valuesType.length; i++) {
            type.addItem(valuesType[i]);
        }
        type.setPreferredSize(new Dimension(150, 25));
        typePanel.add(typeLabel);
        typePanel.add(type);
        firstLine.add(typePanel);

        // Categorie
        JPanel catPanel = new JPanel();
        catPanel.setPreferredSize(new Dimension(200, 60));
        catLabel = new JLabel("Catégorie : ");
        cat = new JComboBox<Cat>();
        Cat[] values = Cat.values();
        for (int i = 0; i < values.length; i++) {
            cat.addItem(values[i]);
        }
        cat.setPreferredSize(new Dimension(150, 25));
        catPanel.add(catLabel);
        catPanel.add(cat);
        firstLine.add(catPanel);

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

        // Sort
        JPanel sortedPanel = new JPanel();
        sortedPanel.setPreferredSize(new Dimension(80, 60));
        sortedLabel = new JLabel("Classé : ");
        sorted = new JCheckBox();
        sorted.setPreferredSize(new Dimension(25, 25));
        sortedPanel.add(sortedLabel);
        sortedPanel.add(sorted);
        firstLine.add(sortedPanel);

        // Order/Artist
        JPanel orderPanel = new JPanel();
        orderPanel.setPreferredSize(new Dimension(130, 60));
        orderLabel = new JLabel("Artiste en premier: ");
        order = new JCheckBox();
        order.setSelected(true);
        order.setPreferredSize(new Dimension(25, 25));
        orderPanel.add(orderLabel);
        orderPanel.add(order);
        firstLine.add(orderPanel);

        center.add(firstLine, BorderLayout.NORTH);
        JPanel secondLine = new JPanel();

        // Publi
        JPanel publiPanel = new JPanel();
        publiPanel.setPreferredSize(new Dimension(200, 60));
        publiLabel = new JLabel("Année de publication : ");
        publi = new JTextField();
        publi.setPreferredSize(new Dimension(150, 25));
        publiPanel.add(publiLabel);
        publiPanel.add(publi);
        secondLine.add(publiPanel);
        
        // Taille
        JPanel sizePanel = new JPanel();
        sizePanel.setPreferredSize(new Dimension(200, 60));
        sizeLabel = new JLabel("Taille : ");
        size = new JTextField();
        size.setPreferredSize(new Dimension(180, 25));
        sizePanel.add(sizeLabel);
        sizePanel.add(size);
        secondLine.add(sizePanel);

        // Line
        JPanel linePanel = new JPanel();
        linePanel.setPreferredSize(new Dimension(400, 60));
        lineLabel = new JLabel("Ligne utilisée : ");
        line = new JTextField();
        line.setPreferredSize(new Dimension(350, 25));
        line.setEnabled(false);
        linePanel.add(lineLabel);
        linePanel.add(line);
        secondLine.add(linePanel);

        // separator
        JPanel separatorPanel = new JPanel();
        separatorPanel.setPreferredSize(new Dimension(100, 60));
        separatorLabel = new JLabel("Séparateur trouvé: ");
        separator = new JTextField();
        separator.setPreferredSize(new Dimension(20, 25));
        separatorPanel.add(separatorLabel);
        separatorPanel.add(separator);
        secondLine.add(separatorPanel);

        // Final
        JPanel getFinalPanel = new JPanel();
        getFinalPanel.setPreferredSize(new Dimension(150, 60));
        getFinalLabel = new JLabel("Fusionner le fichier final : ");
        getFinal = new JCheckBox();
        getFinal.setPreferredSize(new Dimension(25, 25));
        getFinalPanel.add(getFinalLabel);
        getFinalPanel.add(getFinal);
        secondLine.add(getFinalPanel);

        // lastLines
        JPanel lastLinesPanel = new JPanel();
        lastLinesPanel.setPreferredSize(new Dimension(300, 80));
        lastLinesLabel = new JLabel("2 dernières lignes du fichier: ");
        lastL1 = new JTextField();
        lastL1.setPreferredSize(new Dimension(300, 20));
        lastL2 = new JTextField();
        lastL2.setPreferredSize(new Dimension(300, 20));
        lastLinesPanel.add(lastLinesLabel);
        lastLinesPanel.add(lastL1);
        lastLinesPanel.add(lastL2);
        secondLine.add(lastLinesPanel);

        // characterToRemove
        JPanel characterToRemovePanel = new JPanel();
        characterToRemovePanel.setPreferredSize(new Dimension(150, 60));
        characterToRemoveLabel = new JLabel("Caractères à supprimer: ");
        characterToRemove = new JTextField();
        characterToRemove.setPreferredSize(new Dimension(20, 20));
        characterToRemovePanel.add(characterToRemoveLabel);
        characterToRemovePanel.add(characterToRemove);
        secondLine.add(characterToRemovePanel);
        
        center.add(secondLine, BorderLayout.CENTER);
        JPanel thirdLine = new JPanel();
        
        // result
        JPanel resultPanel = new JPanel();
        resultPanel.setPreferredSize(new Dimension(1000, 300));
        resultLabel = new JTextArea(10,20);
        resultLabel.setWrapStyleWord(true);
        resultLabel.setLineWrap(true);
        resultLabel.setOpaque(false);
        resultLabel.setEditable(false);
        resultLabel.setFocusable(false);
        resultLabel.setBackground(UIManager.getColor("Label.background"));
        resultLabel.setFont(UIManager.getFont("Label.font"));
        resultLabel.setBorder(UIManager.getBorder("Label.border"));
        resultPanel.add(resultLabel);
        thirdLine.add(resultPanel);

        center.add(thirdLine, BorderLayout.SOUTH);

        JButton importFile = new JButton("Importer le fichier");
        importFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String result = "Sélectionnez un fichier";
                if (fichier != null) {
                    fichier.setSorted(sorted.isSelected());
                    fichier.setFileName(name.getText());
                    fichier.setAuthor(author.getText());
                    fichier.setCategorie((Cat) cat.getSelectedItem());
                    fichier.setPublishYear(Integer.parseInt(publi.getText()));
                    fichier.setRangeDateBegin(Integer.parseInt(rangeB.getText()));
                    fichier.setRangeDateEnd(Integer.parseInt(rangeE.getText()));
                    result = name.getText() + " OK !";

                    try {
                        List<Composition> compoList = ImportFile.getCompositionsFromFile(new File(absolutePathFile), fichier, (RecordType) type.getSelectedItem(),
                                separator.getText(), order.isSelected());
                        ExportXML.exportXML(compoList, name.getText());
                    } catch (Exception e) {
                        result = e.getMessage();
                    }
                }
                miseEnFormeResultLabel(result);
            }
        });
        bottom.add(importFile);
        
        JButton cleanFile = new JButton("Nettoyer le fichier");
        cleanFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String result = "Sélectionnez un fichier";
                if (file != null) {
                    result = file.getName() + " nettoyé !";

                    try {
                        CleanFile.clearFile(file, fichier.getSorted(), separator.getText(),characterToRemove.getText());
                    } catch (Exception e) {
                        result = e.getMessage();
                    }
                }
                miseEnFormeResultLabel(result);
            }
        });
        bottom.add(cleanFile);
        
        JButton fusionFile = new JButton("Fusionner les fichiers");
        fusionFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String result = "Fichiers fusionnés";
                try {
                    ImportXML.fusionFiles(System.getProperty("user.dir") + App.RESOURCES_DIRECTORY, getFinal.isSelected());
                } catch (Exception e) {
                    result = e.getMessage();
                }
                miseEnFormeResultLabel(result);
            }
        });
        bottom.add(fusionFile);

        this.setLayout(new BorderLayout());
        this.add(top, BorderLayout.NORTH);
        center.setBorder(BorderFactory.createTitledBorder(""));
        this.add(center, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);
    }

    private void miseEnFormeResultLabel(String result) {
        resultLabel.setText(result);
        resultLabel.setForeground(new Color(243,16,16));
        Font labelFont = resultLabel.getFont();
        resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 40));
    }

    private File addBrowsingFile() {
        JFileChooser jfile = new JFileChooser(App.MUSIC_DIRECTORY);
        jfile.setApproveButtonText("Ouvrir");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("txt", "txt");
        jfile.setFileFilter(filter);
        if (jfile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return jfile.getSelectedFile();
        } else {
            return null;
        }
    }

}
