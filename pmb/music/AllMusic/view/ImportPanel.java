/**
 * 
 */
package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.file.CleanFile;
import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MyException;

/**
 * @author i2113mj
 *
 */
public class ImportPanel extends JPanel {

    private static final long serialVersionUID = 5796304304079887263L;

    private JLabel catLabel, authorLabel, publiLabel, rangeLabel, sizeLabel, sortedLabel, typeLabel, dateLabel, nameLabel, lineLabel, separatorLabel, orderLabel, getFinalLabel,
            lastLinesLabel, firstLinesLabel, characterToRemoveLabel, reverseArtistLabel, removeParentheseLabel, upperLabel, removeAfterLabel;

    private JTextField author, publi, rangeB, rangeE, size, name, date, line, separator, firstL1, firstL2, firstL3,lastL1, lastL2, characterToRemove;

    private JTextArea resultLabel;
    
    private File file, xmlFile;
    
    private JComboBox<Cat> cat;

    private JComboBox<RecordType> type;

    private Fichier fichier;

    private String absolutePathFileTxt, absolutePathFileXml;

    private JCheckBox sorted, order, getFinal, reverseArtist, removeParenthese, upper, removeAfter;

    private RecordType determineType;
    
    private String explorePath;

    List<String> result;

    public ImportPanel() {
        super();
        System.out.println("Start ImportPanel");
        explorePath = Constant.MUSIC_DIRECTORY;
        this.setLayout(new GridLayout(6,1));

        JPanel top = new JPanel();
        JButton browse = new JButton("Parcourir");
        browse.setBackground(Color.white);
        browse.setPreferredSize(new Dimension(220, 60));
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start browse");
                miseEnFormeResultLabel(new LinkedList<String>(Arrays.asList("")));
                file = addBrowsingFile("txt",explorePath);
                if (file != null) {
                    explorePath = StringUtils.substring(file.getAbsolutePath(),0,file.getAbsolutePath().lastIndexOf(File.separator));
                    absolutePathFileTxt = file.getAbsolutePath();
                    fichier = ImportFile.convertOneFile(file);
                    List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
                    fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(0)));
                    absolutePathFileXml = Constant.RESOURCES_ABS_DIRECTORY + fichier.getFileName() + ".xml";
                    if(FileUtils.fileExists(absolutePathFileXml)){
                        miseEnFormeResultLabel(new LinkedList<String>(Arrays.asList(fichier.getFileName() + " a déjà été importé")));
                    }
                    determineType = ImportFile.determineType(file.getName());
                    name.setText(fichier.getFileName());
                    author.setText(fichier.getAuthor());
                    date.setText(Constant.SDF_DTTM.format(fichier.getCreationDate()));
                    cat.setSelectedItem(fichier.getCategorie());
                    publi.setText(String.valueOf(fichier.getPublishYear()));
                    type.setSelectedItem(determineType);
                    rangeB.setText(String.valueOf(fichier.getRangeDateBegin()));
                    rangeE.setText(String.valueOf(fichier.getRangeDateEnd()));
                    sorted.setSelected(fichier.getSorted());
                    size.setText(String.valueOf(fichier.getSize()));
                    firstL1.setText(randomLineAndLastLines.get(0));
                    firstL2.setText(randomLineAndLastLines.get(1));
                    firstL3.setText(randomLineAndLastLines.get(2));
                    line.setText(randomLineAndLastLines.get(3));
                    lastL1.setText(randomLineAndLastLines.get(4));
                    lastL2.setText(randomLineAndLastLines.get(5));
                    separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(0)));
                }
                System.out.println("End browse");
            }
        });
        top.add(browse);

        JButton open = new JButton("Charger un fichier XML");
        open.setBackground(Color.white);
        open.setPreferredSize(new Dimension(220, 60));
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start open");
                xmlFile = addBrowsingFile("xml", Constant.RESOURCES_ABS_DIRECTORY);
                if (xmlFile != null) {
                    absolutePathFileXml = xmlFile.getAbsolutePath();
                }
                System.out.println("End open");
            }
        });
        top.add(open);
        top.setBorder(BorderFactory.createTitledBorder(""));
        this.add(top);

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
        
        name.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                absolutePathFileXml = Constant.RESOURCES_ABS_DIRECTORY + name.getText() + ".xml";
                if(FileUtils.fileExists(absolutePathFileXml)){
                    miseEnFormeResultLabel(new LinkedList<String>(Arrays.asList(name.getText() + " existe déjà")));
                }
            }
            @Override
            public void focusGained(FocusEvent e) {
            }
        });

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

        this.add(firstLine);
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
        getFinalPanel.setPreferredSize(new Dimension(170, 60));
        getFinalLabel = new JLabel("Fusionner avec le fichier final : ");
        getFinal = new JCheckBox();
        getFinal.setPreferredSize(new Dimension(25, 25));
        getFinalPanel.add(getFinalLabel);
        getFinalPanel.add(getFinal);
        secondLine.add(getFinalPanel);

        // firstLines
        JPanel firstLinesPanel = new JPanel();
        firstLinesPanel.setPreferredSize(new Dimension(340, 100));
        firstLinesLabel = new JLabel("3 premières lignes du fichier: ");
        firstL1 = new JTextField();
        firstL1.setPreferredSize(new Dimension(340, 20));
        firstL2 = new JTextField();
        firstL2.setPreferredSize(new Dimension(340, 20));
        firstL3 = new JTextField();
        firstL3.setPreferredSize(new Dimension(340, 20));
        firstLinesPanel.add(firstLinesLabel);
        firstLinesPanel.add(firstL1);
        firstLinesPanel.add(firstL2);
        firstLinesPanel.add(firstL3);
        secondLine.add(firstLinesPanel);

        // lastLines
        JPanel lastLinesPanel = new JPanel();
        lastLinesPanel.setPreferredSize(new Dimension(340, 80));
        lastLinesLabel = new JLabel("2 dernières lignes du fichier: ");
        lastL1 = new JTextField();
        lastL1.setPreferredSize(new Dimension(340, 20));
        lastL2 = new JTextField();
        lastL2.setPreferredSize(new Dimension(340, 20));
        lastLinesPanel.add(lastLinesLabel);
        lastLinesPanel.add(lastL1);
        lastLinesPanel.add(lastL2);
        secondLine.add(lastLinesPanel);

        this.add(secondLine);
        JPanel thirdLine = new JPanel();

        // characterToRemove
        JPanel characterToRemovePanel = new JPanel();
        characterToRemovePanel.setPreferredSize(new Dimension(150, 60));
        characterToRemoveLabel = new JLabel("Caractères à supprimer: ");
        characterToRemove = new JTextField();
        characterToRemove.setPreferredSize(new Dimension(20, 20));
        characterToRemovePanel.add(characterToRemoveLabel);
        characterToRemovePanel.add(characterToRemove);
        thirdLine.add(characterToRemovePanel);
        
        // reverseArtist
        JPanel reverseArtistPanel = new JPanel();
        reverseArtistPanel.setPreferredSize(new Dimension(100, 60));
        reverseArtistLabel = new JLabel("Retourner l'artiste: ");
        reverseArtist = new JCheckBox();
        reverseArtist.setPreferredSize(new Dimension(20, 20));
        reverseArtistPanel.add(reverseArtistLabel);
        reverseArtistPanel.add(reverseArtist);
        thirdLine.add(reverseArtistPanel);
        
        // removeParenthese
        JPanel removeParenthesePanel = new JPanel();
        removeParenthesePanel.setPreferredSize(new Dimension(260, 60));
        removeParentheseLabel = new JLabel("Supprimer le texte entre parenthèse du titre: ");
        removeParenthese = new JCheckBox();
        removeParenthese.setPreferredSize(new Dimension(20, 20));
        removeParenthesePanel.add(removeParentheseLabel);
        removeParenthesePanel.add(removeParenthese);
        thirdLine.add(removeParenthesePanel);
        
        // upper
        JPanel upperPanel = new JPanel();
        upperPanel.setPreferredSize(new Dimension(240, 60));
        upperLabel = new JLabel("Pas de séparateur, artiste en capitale: ");
        upper = new JCheckBox();
        upper.setPreferredSize(new Dimension(20, 20));
        upperPanel.add(upperLabel);
        upperPanel.add(upper);
        thirdLine.add(upperPanel);
        
        // removeAfter
        JPanel removeAfterPanel = new JPanel();
        removeAfterPanel.setPreferredSize(new Dimension(240, 60));
        removeAfterLabel = new JLabel("Supprime après le dernier séparateur: ");
        removeAfter = new JCheckBox();
        removeAfter.setPreferredSize(new Dimension(20, 20));
        removeAfterPanel.add(removeAfterLabel);
        removeAfterPanel.add(removeAfter);
        thirdLine.add(removeAfterPanel);
        
        this.add(thirdLine);
        JPanel fourthLine = new JPanel(new GridLayout(0, 1));
        
        // result
        JPanel resultPanel = new JPanel(new BorderLayout());
//        resultPanel.setPreferredSize(new Dimension(1000, 300));
        resultLabel = new JTextArea();
        resultLabel.setWrapStyleWord(true);
        resultLabel.setLineWrap(true);
        resultLabel.setOpaque(false);
        resultLabel.setEditable(false);
        resultLabel.setBackground(UIManager.getColor("Label.background"));
        resultLabel.setFont(UIManager.getFont("Label.font"));
        resultLabel.setBorder(UIManager.getBorder("Label.border"));
        resultPanel.add(new JScrollPane(resultLabel), BorderLayout.CENTER);
        fourthLine.add(resultPanel);

        this.add(fourthLine);
        

        JPanel bottom = new JPanel();
        JButton importFile = new JButton("Importer le fichier");
        importFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start importFile");
                result=new LinkedList<String>(Arrays.asList("Sélectionnez un fichier"));
                if (fichier != null) {
                    fichier.setSorted(sorted.isSelected());
                    fichier.setFileName(name.getText());
                    fichier.setAuthor(author.getText());
                    fichier.setCategorie((Cat) cat.getSelectedItem());
                    fichier.setPublishYear(Integer.parseInt(publi.getText()));
                    fichier.setRangeDateBegin(Integer.parseInt(rangeB.getText()));
                    fichier.setRangeDateEnd(Integer.parseInt(rangeE.getText()));
                    result = new LinkedList<String>(Arrays.asList(name.getText() + " OK !"));

                    try {
                        List<Composition> compoList = ImportFile.getCompositionsFromFile(new File(absolutePathFileTxt), fichier, (RecordType) type.getSelectedItem(),
                                separator.getText(),result, order.isSelected(), reverseArtist.isSelected(), removeParenthese.isSelected(), upper.isSelected(), removeAfter.isSelected());
                        ExportXML.exportXML(compoList, name.getText());
                        absolutePathFileXml = Constant.RESOURCES_ABS_DIRECTORY + name.getText() + ".xml";
                    } catch (IOException | MyException e) {
                        result = new LinkedList<String>(Arrays.asList(e.toString()));
                    }
                }
                miseEnFormeResultLabel(result);
                System.out.println("End importFile");
            }
        });
        bottom.add(importFile);
        
        JButton cleanFile = new JButton("Nettoyer le fichier");
        cleanFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start cleanFile");
                result = new LinkedList<String>(Arrays.asList("Sélectionnez un fichier"));
                if (file != null) {
                    result = new LinkedList<String>(Arrays.asList(file.getName() + " nettoyé !"));

                    try {
                        CleanFile.clearFile(file, fichier.getSorted(), separator.getText(),characterToRemove.getText());
                    } catch (IOException e) {
                        result = new LinkedList<String>(Arrays.asList(e.toString()));
                    }
                }
                miseEnFormeResultLabel(result);
                System.out.println("End cleanFile");
            }
        });
        bottom.add(cleanFile);
        
        JButton fusionFile = new JButton("Fusionner tous les fichiers");
        fusionFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start fusionFile");
                result = new LinkedList<String>(Arrays.asList("Fichiers fusionnés"));
                try {
                    ImportXML.fusionFiles(System.getProperty("user.dir") + Constant.RESOURCES_DIRECTORY, getFinal.isSelected());
                } catch (IOException e) {
                    result = new LinkedList<String>(Arrays.asList(e.toString()));
                }
                miseEnFormeResultLabel(result);
                System.out.println("End fusionFile");
            }
        });
        bottom.add(fusionFile);

        JButton fusionOneFile = new JButton("Ajouter le fichier aux autres");
        fusionOneFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start fusionOneFile");
                if (StringUtils.isNotBlank(absolutePathFileXml)) {
                    result = new LinkedList<String>(Arrays.asList("Fichier ajouté"));
                    try {
                        ImportXML.fusionOneFile(absolutePathFileXml);
                    } catch (IOException e) {
                        result = new LinkedList<String>(Arrays.asList(e.toString()));
                    }
                    miseEnFormeResultLabel(result);
                }
                System.out.println("End fusionOneFile");
            }
        });
        bottom.add(fusionOneFile);

        JButton openFile = new JButton("Éditer le fichier source");
        openFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start openFile");
                if (StringUtils.isNotBlank(absolutePathFileTxt)) {
                    try {
                        Runtime.getRuntime().exec(Constant.NOTEPAD_EXE + absolutePathFileTxt);
                    } catch (IOException e) {
                        result = new LinkedList<String>(Arrays.asList(e.toString()));
                    }
                    miseEnFormeResultLabel(result);
                }
                System.out.println("End openFile");
            }
        });
        bottom.add(openFile);

        JButton openXml = new JButton("Éditer le fichier xml");
        openXml.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Start openXml");
                if (StringUtils.isNotBlank(absolutePathFileXml)) {
                    try {
                        System.out.println(absolutePathFileXml);
                        Runtime.getRuntime().exec(Constant.NOTEPAD_EXE + absolutePathFileXml);
                    } catch (IOException e) {
                        result = new LinkedList<String>(Arrays.asList(e.toString()));
                    }
                    miseEnFormeResultLabel(result);
                }
                System.out.println("End openXml");
            }
        });
        bottom.add(openXml);
        bottom.setBorder(BorderFactory.createTitledBorder(""));
        this.add(bottom);

        System.out.println("End ImportPanel");
    }

    private void miseEnFormeResultLabel(List<String> result2) {
        StringBuilder s = new StringBuilder();
        for (String string : result2) {
            s.append(string).append('\n');
        }
        resultLabel.setText(s.toString());
        resultLabel.setForeground(new Color(243,16,16));
        Font labelFont = resultLabel.getFont();
        resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
    }

    private File addBrowsingFile(String extension, String dir) {
        JFileChooser jfile = new JFileChooser(dir);
        jfile.setApproveButtonText("Ouvrir");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(extension,extension);
        jfile.setFileFilter(filter);
        if (jfile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return jfile.getSelectedFile();
        } else {
            return null;
        }
    }

}
