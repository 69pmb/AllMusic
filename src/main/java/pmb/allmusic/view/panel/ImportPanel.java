/**
 *
 */
package pmb.allmusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.file.CleanFile;
import pmb.allmusic.file.ImportFile;
import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.utils.SearchUtils;
import pmb.allmusic.view.ActionPanel;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.PanelUtils;
import pmb.allmusic.view.component.MyInputRange;
import pmb.allmusic.xml.ExportXML;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.VariousUtils;

/**
 * Onglet d'import de fichiers txt.
 *
 */
public class ImportPanel extends JPanel implements ActionPanel {

    private static final long serialVersionUID = 5796304304079887263L;

    private static final Logger LOG = LogManager.getLogger(ImportPanel.class);

    private JTextField author;
    private JTextField publi;
    private MyInputRange range;
    private JTextField size;
    private JTextField name;
    private JTextField date;
    /**
     * Random line.
     */
    private JTextField line;
    private JTextField separator;
    private JTextField firstL1;
    private JTextField firstL2;
    private JTextField firstL3;
    private JTextField lastL1;
    private JTextField lastL2;
    /**
     * String to remove from import file.
     */
    private JTextField characterToRemove;
    /**
     * Maximum length of a line when cleaning.
     */
    private JTextField maxLengthClean;

    private JTextArea resultLabel = new JTextArea();

    private File file;
    private File xmlFile;

    private JComboBox<Cat> cat;

    private JComboBox<RecordType> type;

    private JComboBox<String> generated;

    private Fichier fichier;

    private String absolutePathFileTxt;
    private String absolutePathFileXml;

    private JCheckBox sorted;
    /**
     * Si l'artist est en 1er, puis le titre.
     */
    private JCheckBox order;
    /**
     * Si le nom et prénom de l'artist sont inversés. Ex: Young, Neil.
     */
    private JCheckBox reverseArtist;
    /**
     * Doit on supprimer lors de l'import des parenthèses.
     */
    private JCheckBox removeParenthese;
    /**
     * Pas de séparateur mais artist en majuscule ?
     */
    private JCheckBox upper;
    /**
     * 2 séparateurs, suppression du dernier.
     */
    private JCheckBox removeAfter;
    /**
     * Si le characterToRemove est à supprimer au debut ou à la fin de la ligne.
     */
    private JCheckBox isBefore;
    /**
     * Utilise-t-on le dossier du fichier à mettre en forme.
     */
    private JCheckBox isCompleteDirectory;

    /**
     * Path of the last opened file.
     */
    private String explorePath;

    /**
     * Content for the result area.
     */
    private List<String> result = new LinkedList<>();

    /**
     * Button that launchs the import of the current loaded file.
     */
    private JButton importFile;

    /**
     * Button opeing generated file selected by generated.
     */
    private JButton openGenerated;

    /**
     * Import params constants.
     */
    public static final String IMPORT_PARAM_SEPARATOR = "separator";
    public static final String IMPORT_PARAM_ARTIST_FIRST = "artistFirst";
    public static final String IMPORT_PARAM_REVERSE_ARTIST = "reverseArtist";
    public static final String IMPORT_PARAM_PARENTHESE = "parenthese";
    public static final String IMPORT_PARAM_UPPER = "upper";
    public static final String IMPORT_PARAM_REMOVE_AFTER = "removeAfter";
    public static final String IMPORT_PARAM_NAME = "name";
    public static final String IMPORT_PARAM_AUTEUR = "auteur";
    public static final String IMPORT_PARAM_CREATE = "create";
    public static final String IMPORT_PARAM_RECORD_TYPE = "type";
    public static final String IMPORT_PARAM_CATEGORIE = "cat";
    public static final String IMPORT_PARAM_RANGE_BEGIN = "rangeB";
    public static final String IMPORT_PARAM_RANGE_END = "rangeE";
    public static final String IMPORT_PARAM_SORTED = "sorted";
    public static final String IMPORT_PARAM_PUBLISH_YEAR = "publish";
    public static final String IMPORT_PARAM_SIZE = "size";

    public static final String GENERATED_XML = "XML";
    public static final String GENERATED_MISE_EN_FORME = "Mise en Forme";
    public static final String GENERATED_CLEAN = "Nettoyé";

    /**
     * Construit l'onglet import.
     */
    public ImportPanel() {
        super();
        LOG.debug("Start ImportPanel");
        explorePath = Constant.getMusicAbsDirectory();
        this.setLayout(new GridLayout(6, 1));

        // Insert les boutons du hauts
        insertTopPanel();
        // Insert tous les inputs
        insertInputs();
        // Insert les boutons du bas
        insertBottomPanel();

        LOG.debug("End ImportPanel");
    }

    /**
     * Ajoute les boutons en haut de l'écran.
     */
    private void insertTopPanel() {
        LOG.debug("Start insertTopPanel");
        JPanel top = new JPanel();
        JButton browse = ComponentBuilder.buildJButton("Parcourir", 220, Constant.ICON_FOLDER);
        browse.setToolTipText("Charge un fichier texte contenant des musiques.");
        browse.addActionListener((ActionEvent arg0) -> {
            LOG.debug("Start browse");
            file = addBrowsingFile("txt", explorePath, arg0);
            if (file != null) {
                loadFile();
            }
            LOG.debug("End browse");
        });
        top.add(browse);

        // Reset
        JButton cleanBtn = ComponentBuilder.buildJButton("Reset", 220, Constant.ICON_ERASE);
        cleanBtn.setToolTipText("Remet à zéro tous les champs.");
        cleanBtn.addActionListener((ActionEvent arg0) -> resetAll());
        top.add(cleanBtn);

        // Reload
        JButton reloadBtn = ComponentBuilder.buildJButton("Reload", 220, Constant.ICON_REFRESH);
        reloadBtn.setToolTipText(
                "Relance le chargement du fichier chargé précédemment. Utile si il a été modifié entre temps.");
        reloadBtn.addActionListener((ActionEvent arg0) -> loadFile());
        top.add(reloadBtn);

        // Open entry file in notepad
        JButton openFile = ComponentBuilder.buildJButton("Ouvrir le fichier source", 200, Constant.ICON_TXT_FILE);
        openFile.setToolTipText("Ouvre le fichier chargé dans Notepad++");
        openFile.addActionListener((ActionEvent arg0) -> openFileNotepad(absolutePathFileTxt));
        top.add(openFile);

        // Open Xml file
        JButton open = ComponentBuilder.buildJButton("Charger un fichier XML", 220, Constant.ICON_FILE);
        open.setToolTipText("Au lieu de charger un fichier texte, charge un xml.");
        open.addActionListener((ActionEvent arg0) -> {
            LOG.debug("Start open");
            xmlFile = addBrowsingFile("xml", Constant.getXmlPath(), arg0);
            if (xmlFile != null) {
                absolutePathFileXml = xmlFile.getAbsolutePath();
                getGeneratedFile(GENERATED_XML);
            }
            LOG.debug("End open");
        });
        top.add(open);

        // Opens txt file directory
        JButton openDir = ComponentBuilder.buildJButton("<html>Ouvrir le dossier du fichier source</html>", 200,
                Constant.ICON_FOLDER);
        openDir.setToolTipText("Ouvre le dossier du fichier source dans l'explorateur");
        openDir.addActionListener((ActionEvent arg0) -> {
            if (StringUtils.isNotBlank(absolutePathFileTxt)) {
                String directoryTxt = StringUtils.substringBeforeLast(absolutePathFileTxt, MyConstant.FS);
                try {
                    Desktop.getDesktop().open(new File(directoryTxt));
                } catch (IOException e) {
                    LOG.error("Error when opening input directory", e);
                    result = new LinkedList<>(Arrays.asList(e.toString()));
                    miseEnFormeResultLabel(result);
                }
            }
        });
        top.add(openDir);

        top.setBorder(BorderFactory.createTitledBorder(""));
        this.add(top);
        LOG.debug("End insertTopPanel");
    }

    private void insertInputs() {
        LOG.debug("Start insertInputs");
        insertFirstLine();
        insertSecondLine();
        insertThirdLine();
        insertResultPanel();
        LOG.debug("End insertInputs");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void insertFirstLine() {
        LOG.debug("Start insertFirstLine");
        JPanel firstLine = new JPanel();

        // Nom du fichier
        name = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(firstLine)
                .withLabel("Nom du fichier : ").withPanelWidth(360).withComponentWidth(350).build();
        name.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                absolutePathFileXml = Constant.getXmlPath() + name.getText() + MyConstant.XML_EXTENSION;
                if (new File(absolutePathFileXml).exists()) {
                    miseEnFormeResultLabel(new LinkedList<>(Arrays.asList(name.getText() + " existe déjà")));
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Nothing to do
            }
        });

        // Auteur
        author = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(firstLine).withLabel("Auteur : ")
                .withPanelWidth(160).withComponentWidth(150).build();
        // Date de creation
        date = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(firstLine)
                .withLabel("Date de création : ").withPanelWidth(160).withComponentWidth(150).build();
        date.setEnabled(false);
        // Type
        type = new ComponentBuilder<JComboBox, RecordType>(JComboBox.class).withParent(firstLine).withPanelWidth(110)
                .withLabel("Type : ").withValues(RecordType.values()).withComponentWidth(100).build();
        // Categorie
        cat = new ComponentBuilder<JComboBox, Cat>(JComboBox.class).withParent(firstLine).withPanelWidth(130)
                .withLabel("Catégorie : ").withValues(Cat.values()).withComponentWidth(120).build();
        // Range
        range = new ComponentBuilder<MyInputRange, String>(MyInputRange.class).withParent(firstLine)
                .withLabel("Année(s) du classement : ").withPanelWidth(300).withComponentWidth(200).build();
        // Sort
        sorted = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(firstLine).withLabel("Classé : ")
                .withPanelWidth(60).withComponentWidth(25).build();
        // Order/Artist
        order = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(firstLine)
                .withLabel("Artiste en premier : ").withInitialValue(true).withPanelWidth(100).withComponentWidth(25)
                .build();

        this.add(firstLine);
        LOG.debug("End insertFirstLine");
    }

    private void insertSecondLine() {
        LOG.debug("Start insertSecondLine");
        JPanel secondLine = new JPanel();

        // Publi
        publi = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(secondLine)
                .withLabel("Année de publication : ").withPanelWidth(160).withComponentWidth(150).build();
        publi.addFocusListener(PanelUtils.selectAll);
        // Size
        size = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(secondLine).withLabel("Taille : ")
                .withPanelWidth(200).withComponentWidth(180).build();
        size.addFocusListener(PanelUtils.selectAll);
        // Line
        line = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(secondLine)
                .withLabel("Ligne utilisée : ").withPanelWidth(400).withComponentWidth(350).build();
        line.setEnabled(false);
        // Separator
        separator = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(secondLine)
                .withLabel("<html>Séparateur trouvé : </html>").withPanelWidth(120).withComponentWidth(20).build();
        separator.addFocusListener(PanelUtils.selectAll);

        // firstLines
        JPanel firstLinesPanel = new JPanel();
        firstLinesPanel.setPreferredSize(new Dimension(340, 100));
        JLabel firstLinesLabel = new JLabel("3 premières lignes du fichier: ");
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

        this.add(secondLine);
        LOG.debug("End insertSecondLine");
    }

    private void insertThirdLine() {
        LOG.debug("Start insertThirdLine");
        JPanel thirdLine = new JPanel();

        // lastLines
        JPanel lastLinesPanel = new JPanel();
        lastLinesPanel.setPreferredSize(new Dimension(340, 80));
        JLabel lastLinesLabel = new JLabel("2 dernières lignes du fichier: ");
        lastL1 = new JTextField();
        lastL1.setPreferredSize(new Dimension(340, 20));
        lastL2 = new JTextField();
        lastL2.setPreferredSize(new Dimension(340, 20));
        lastLinesPanel.add(lastLinesLabel);
        lastLinesPanel.add(lastL1);
        lastLinesPanel.add(lastL2);
        thirdLine.add(lastLinesPanel);

        // reverseArtist
        reverseArtist = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(thirdLine)
                .withLabel("<html>Retourner l'artiste: </html>").withPanelWidth(100).withComponentWidth(20).build();
        // removeParenthese
        removeParenthese = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(thirdLine)
                .withLabel("<html>Supprimer le texte entre parenthèse du titre: </html>").withPanelWidth(150)
                .withComponentWidth(20).build();
        // upper
        upper = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(thirdLine)
                .withLabel("<html>Pas de séparateur, artiste en capitale: </html>").withPanelWidth(100)
                .withComponentWidth(20).build();
        // removeAfter
        removeAfter = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(thirdLine)
                .withLabel("<html>Supprime après le dernier séparateur: </html>").withPanelWidth(100)
                .withComponentWidth(20).build();
        // isCompleteDirectory
        isCompleteDirectory = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(thirdLine)
                .withLabel("<html>Utiliser le dossier du fichier pour la mise en forme: </html>").withPanelWidth(150)
                .withComponentWidth(20).build();

        // Clean Params
        JPanel cleanBtnPanel = new JPanel();
        cleanBtnPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Paramètres de nettoyage: ", TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
        // characterToRemove
        characterToRemove = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(cleanBtnPanel)
                .withLabel("<html>Caractères à supprimer : </html>").withPanelWidth(140).withComponentWidth(40).build();
        characterToRemove.addFocusListener(PanelUtils.selectAll);
        // maxLengthClean
        maxLengthClean = new ComponentBuilder<JTextField, String>(JTextField.class).withParent(cleanBtnPanel)
                .withLabel("<html><body style='width: 100%'>Longueur maximale d'une ligne valide: </body></html>")
                .withInitialValue("120").withPanelWidth(120).withComponentWidth(40).build();
        maxLengthClean.addFocusListener(PanelUtils.selectAll);
        // isBefore
        isBefore = new ComponentBuilder<JCheckBox, Boolean>(JCheckBox.class).withParent(cleanBtnPanel)
                .withLabel("<html>Supprimer au début : </html>").withPanelWidth(120)
                .withInitialValue(true).withComponentWidth(20).build();

        thirdLine.add(cleanBtnPanel);
        this.add(thirdLine);
        LOG.debug("End insertThirdLine");
    }

    private void insertResultPanel() {
        JPanel fourthLine = new JPanel(new GridLayout(0, 1));

        // result
        JPanel resultPanel = new JPanel(new BorderLayout());
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
    }

    /**
     * Ajoute les boutons du bas de l'écran.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void insertBottomPanel() {
        LOG.debug("Start insertBottomPanel");
        JPanel bottom = new JPanel();

        // Import
        importFile = ComponentBuilder.buildJButton("Importer le fichier", 200, Constant.ICON_UPLOAD);
        importFile.setToolTipText("Importe au format XML le fichier chargé précédemment avec les critères renseignés.");
        importFile.addActionListener((ActionEvent arg0) -> importFileAction());
        bottom.add(importFile);

        // Clean
        JButton cleanFile = ComponentBuilder.buildJButton("Nettoyer le fichier", 200, Constant.ICON_CLEAN);
        cleanFile.setToolTipText(
                "Supprime les lignes qui ne contiennent pas le séparateur. Supprime également les charactères à supprimer.");
        cleanFile.addActionListener((ActionEvent arg0) -> cleanFileAction());
        bottom.add(cleanFile);

        // Mise en forme
        JButton mef = ComponentBuilder.buildJButton("Mettre en forme un fichier ou dossier", 250, Constant.ICON_ALIGN);
        mef.setToolTipText("Pour supprimer les diacritiques et remplacer des charactères spéciaux.");
        mef.addActionListener((ActionEvent arg0) -> {
            result = new LinkedList<>(
                    Arrays.asList((isCompleteDirectory.isSelected() ? "Dossier" : "Fichier") + " mis en forme:"));
            CleanFile.miseEnForme(file, isCompleteDirectory.isSelected(), result);
            if (result.size() > 1) {
                selectGeneratedFile(GENERATED_MISE_EN_FORME);
                miseEnFormeResultLabel(result);
            } else {
                miseEnFormeResultLabel(new LinkedList<>(Arrays.asList("Rien à mettre en forme")));
            }
        });
        bottom.add(mef);

        // Fusion
        JButton fusionFile = ComponentBuilder.buildJButton("Fusionner tous les fichiers", 200, Constant.ICON_FUSION);
        fusionFile.setToolTipText("Aggrège tous les fichiers XML importés dans le fichier final.");
        fusionFile.addActionListener((ActionEvent arg0) -> new Thread(this::fusionFilesAction).start());
        bottom.add(fusionFile);

        // Open generated file in notepad
        JPanel wrapGenerated = new JPanel(new GridLayout(2, 1));
        generated = new ComponentBuilder<JComboBox, String>(JComboBox.class).withParent(wrapGenerated)
                .withPanelWidth(200).withComponentWidth(100)
                .withValues(new String[] { GENERATED_XML, GENERATED_CLEAN, GENERATED_MISE_EN_FORME })
                .withInitialValue(GENERATED_XML).build();
        generated.addActionListener((ActionEvent arg0) -> selectGeneratedFile((String) generated.getSelectedItem()));
        wrapGenerated.add(generated);

        openGenerated = ComponentBuilder.buildJButton("Ouvrir le fichier généré", 200, Constant.ICON_XML_FILE);
        openGenerated.setToolTipText("Ouvre le fichier généré seléctionné dans Notepad++");
        openGenerated.addActionListener((ActionEvent arg0) -> getGeneratedFile((String) generated.getSelectedItem())
                .ifPresent(this::openFileNotepad));
        openGenerated.setEnabled(false);
        wrapGenerated.add(openGenerated);

        wrapGenerated.setPreferredSize(new Dimension(200, ComponentBuilder.PANEL_HEIGHT));
        bottom.add(wrapGenerated);

        // Ouvre le fichier de log
        JButton log = ComponentBuilder.buildJButton("Logs", 200, Constant.ICON_TXT_FILE);
        log.setToolTipText("Ouvre le fichier de logs dans Notepad++");
        log.addActionListener((ActionEvent arg0) -> openFileNotepad(MyConstant.FILE_LOG_PATH));
        bottom.add(log);

        bottom.setBorder(BorderFactory.createTitledBorder(""));
        this.add(bottom);
        LOG.debug("End insertBottomPanel");
    }

    /**
     * Le traitement lorsqu'on importe un fichier txt.
     */
    private void importFileAction() {
        LOG.debug("Start importFileAction");
        result = new LinkedList<>(Arrays.asList("Sélectionnez un fichier"));
        if (fichier != null) {
            fichier.setSorted(sorted.isSelected());
            String xmlFileName = name.getText();
            fichier.setFileName(xmlFileName);
            fichier.setAuthor(author.getText());
            fichier.setCategorie((Cat) cat.getSelectedItem());
            fichier.setPublishYear(Integer.parseInt(publi.getText()));
            fichier.setRangeDateBegin(Integer.parseInt(range.getFirst().getText()));
            fichier.setRangeDateEnd(Integer.parseInt(range.getSecond().getText()));
            fichier.setSize(Integer.parseInt(size.getText()));
            result = new LinkedList<>(Arrays.asList(xmlFileName + " Import OK !"));

            try {
                // Parse Txt file to Compositions List
                List<Composition> compoList = ImportFile.getCompositionsFromFile(new File(absolutePathFileTxt), fichier,
                        (RecordType) type.getSelectedItem(), separator.getText(), result, order.isSelected(),
                        reverseArtist.isSelected(), removeParenthese.isSelected(), upper.isSelected(),
                        removeAfter.isSelected());
                if (!new File(absolutePathFileXml).exists()) {
                    // Keep uuid, deleted, mergeable fields and artist/title if file has already been imported
                    compoList = mergeWithFormerData(compoList, xmlFileName);
                }
                // Export Compositions to XML file
                ExportXML.exportXML(compoList, xmlFileName);
                // Write in Txt file import params
                FilesUtils.writeMapInTxtFile(new File(absolutePathFileTxt),
                        convertParamsToMap(separator.getText(), order.isSelected(), reverseArtist.isSelected(),
                                removeParenthese.isSelected(), upper.isSelected(), removeAfter.isSelected()));
                selectGeneratedFile(GENERATED_XML);
            } catch (MajorException e) {
                LOG.error("Erreur lors de l'import du fichier: {}", absolutePathFileTxt, e);
                result = new LinkedList<>(Arrays.asList(e.toString()));
            }
        }
        miseEnFormeResultLabel(result);
        LOG.debug("End importFileAction");
    }

    private static List<Composition> mergeWithFormerData(List<Composition> compoList, String fileName) {
        List<Composition> oldList = FilesUtils.buildXmlFilePath(fileName).map(ImportXML::importXML).orElseThrow(
                () -> new MinorException("Can't merge file " + fileName + " because file doesn't exist yet"));
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        return compoList.stream()
                .map(c -> findByRankAndFileName(oldList, c.getFiles().get(0).getClassement(), fileName).map(old -> {
                    c.setUuids(old.getUuids());
                    c.setDeleted(old.isDeleted());
                    c.setCanBeMerged(old.isCanBeMerged());
                    if (!SearchUtils.isEqualsJaro(jaro, c.getArtist(), old.getArtist(),
                            Constant.SCORE_LIMIT_ARTIST_FUSION)) {
                        c.setArtist(old.getArtist());
                    }
                    if (!SearchUtils.isEqualsJaro(jaro, c.getTitre(), old.getTitre(),
                            Constant.SCORE_LIMIT_TITLE_FUSION)) {
                        c.setTitre(old.getTitre());
                    }
                    return c;
                }).orElseThrow(() -> new MinorException("Can't find composition: " + c))).collect(Collectors.toList());
    }

    private static Optional<Composition> findByRankAndFileName(List<Composition> list, Integer rank, String filename) {
        return SearchUtils.findBy(list, c -> Integer.compare(c.getFiles().get(0).getClassement(), rank) == 0
                && StringUtils.equals(filename, c.getFiles().get(0).getFileName()));
    }

    private Map<String, String> convertParamsToMap(String separator, boolean artistFirst, boolean reverseArtist,
            boolean parenthese, boolean upper, boolean removeAfter) {
        Map<String, String> map = new HashMap<>();
        map.put(IMPORT_PARAM_SEPARATOR, separator);
        map.put(IMPORT_PARAM_ARTIST_FIRST, Boolean.toString(artistFirst));
        map.put(IMPORT_PARAM_REVERSE_ARTIST, Boolean.toString(reverseArtist));
        map.put(IMPORT_PARAM_PARENTHESE, Boolean.toString(parenthese));
        map.put(IMPORT_PARAM_UPPER, Boolean.toString(upper));
        map.put(IMPORT_PARAM_REMOVE_AFTER, Boolean.toString(removeAfter));
        map.put(IMPORT_PARAM_NAME, fichier.getFileName());
        map.put(IMPORT_PARAM_AUTEUR, fichier.getAuthor());
        map.put(IMPORT_PARAM_CREATE, new Constant().getFullDTF().format(fichier.getCreationDate()));
        map.put(IMPORT_PARAM_RECORD_TYPE, type.getSelectedItem().toString());
        map.put(IMPORT_PARAM_CATEGORIE, fichier.getCategorie().getValue());
        map.put(IMPORT_PARAM_RANGE_BEGIN, String.valueOf(fichier.getRangeDateBegin()));
        map.put(IMPORT_PARAM_RANGE_END, String.valueOf(fichier.getRangeDateEnd()));
        map.put(IMPORT_PARAM_SORTED, String.valueOf(fichier.getSorted()));
        map.put(IMPORT_PARAM_PUBLISH_YEAR, String.valueOf(fichier.getPublishYear()));
        map.put(IMPORT_PARAM_SIZE, String.valueOf(fichier.getSize()));
        return map;
    }

    /**
     * Mise en forme des messages pour l'afficher dans la zone texte result.
     *
     * @param result2 liste de texte à afficher
     */
    private void miseEnFormeResultLabel(List<String> result2) {
        LOG.debug("Start miseEnFormeResultLabel");
        StringBuilder s = new StringBuilder();
        for (String string : result2) {
            s.append(string).append(MyConstant.NEW_LINE);
        }
        resultLabel.setText(s.toString());
        resultLabel.setForeground(new Color(243, 16, 16));
        Font labelFont = resultLabel.getFont();
        resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
        LOG.debug("End miseEnFormeResultLabel");
    }

    /**
     * Crée un file chooser pour sélectionner un fichier selon l'extension donnée et
     * à l'endroit donnée.
     *
     * @param extension le filtre sur les extensions
     * @param dir à quel endroit le file chooser s'ouvre
     * @param e event
     * @return le fichier choisit
     */
    private File addBrowsingFile(String extension, String dir, ActionEvent e) {
        LOG.debug("Start addBrowsingFile");
        LOG.debug(dir);
        JFileChooser jfile = new JFileChooser(dir);
        jfile.setApproveButtonText("Ouvrir");
        jfile.setPreferredSize(new Dimension(1200, 600));
        jfile.setFileFilter(new FileNameExtensionFilter(extension, extension));
        Optional.ofNullable(jfile.getActionMap().get("viewTypeDetails"))
        .ifPresent(a -> a.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "viewTypeDetails")));
        if (jfile.showOpenDialog(new JDialog()) == JFileChooser.APPROVE_OPTION) {
            resetAll();
            LOG.debug("End addBrowsingFile");
            return jfile.getSelectedFile();
        } else {
            LOG.debug("End addBrowsingFile, no file choose");
            return null;
        }
    }

    /**
     * Remet à zéro tous les champs de l'écran.
     */
    private void resetAll() {
        LOG.debug("Start resetAll");
        explorePath = Constant.getMusicAbsDirectory();
        absolutePathFileTxt = "";
        fichier = null;
        absolutePathFileXml = "";
        file = null;
        name.setText("");
        author.setText("");
        date.setText("");
        type.setSelectedItem(null);
        cat.setSelectedItem(null);
        range.getFirst().setText("");
        range.getSecond().setText("");
        sorted.setSelected(false);
        order.setSelected(true);
        publi.setText("");
        size.setText("");
        line.setText("");
        firstL1.setText("");
        firstL2.setText("");
        firstL3.setText("");
        lastL1.setText("");
        lastL2.setText("");
        separator.setText("");
        characterToRemove.setText("");
        maxLengthClean.setText("120");
        isBefore.setSelected(true);
        reverseArtist.setSelected(false);
        removeParenthese.setSelected(false);
        upper.setSelected(false);
        removeAfter.setSelected(false);
        isCompleteDirectory.setSelected(false);
        generated.setSelectedItem(GENERATED_XML);
        openGenerated.setEnabled(false);
        miseEnFormeResultLabel(new ArrayList<>());
        LOG.debug("End resetAll");
    }

    /**
     * Traitement lorsqu'on séléctionne un fichier. Les paramètres d'import sont
     * déduit du nom du fichier et du contenu.
     */
    private void loadFile() {
        LOG.debug("Start loadFile");
        List<String> label = new LinkedList<>();
        explorePath = StringUtils.substring(file.getAbsolutePath(), 0,
                file.getAbsolutePath().lastIndexOf(File.separator));
        absolutePathFileTxt = file.getAbsolutePath();
        List<String> randomLineAndLastLines = ImportFile.randomLineAndLastLines(file);
        String firstLine = MyFileUtils.readFirstLine(file.getAbsolutePath());
        DateTimeFormatter fullDTF = new Constant().getFullDTF();

        // Initializes input fields
        if (StringUtils.startsWith(firstLine, Constant.IMPORT_PARAMS_PREFIX)) {
            initInputsWithImportParams(label, firstLine, fullDTF);
        } else {
            initInputsWithGuessedParams(label, randomLineAndLastLines, fullDTF);
        }

        if (new File(absolutePathFileXml).exists()) {
            label.add(name.getText() + " a déjà été importé");
            miseEnFormeResultLabel(label);
        }
        Map.of(0, firstL1, 1, firstL2, 2, firstL3, 3, line, 4, lastL1, 5, lastL2).entrySet().stream()
        .filter(e -> randomLineAndLastLines.size() > e.getKey())
        .forEach(e -> e.getValue().setText(randomLineAndLastLines.get(e.getKey())));
        LOG.debug("End loadFile");
    }

    private void initInputsWithImportParams(List<String> label, String firstLine, DateTimeFormatter fullDTF) {
        Map<String, String> value = new HashMap<>();
        try {
            value = VariousUtils
                    .<String>readValueAsMap(StringUtils.substringAfter(firstLine, Constant.IMPORT_PARAMS_PREFIX));
        } catch (IOException e) {
            LOG.error("Error while decoding import params: {} in file {}", firstLine, absolutePathFileTxt, e);
            return;
        }
        Optional.of(value.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining(", "))).ifPresent(crit -> LOG.debug("value: {}", crit));
        LOG.debug("Init with stored params");
        name.setText(value.get(IMPORT_PARAM_NAME));
        absolutePathFileXml = Constant.getXmlPath() + value.get(IMPORT_PARAM_NAME) + MyConstant.XML_EXTENSION;
        author.setText(value.get(IMPORT_PARAM_AUTEUR));
        date.setText(value.get(IMPORT_PARAM_CREATE));
        cat.setSelectedItem(Cat.getByValue(value.get(IMPORT_PARAM_CATEGORIE)));
        publi.setText(value.get(IMPORT_PARAM_PUBLISH_YEAR));
        type.setSelectedItem(RecordType.valueOf(value.get(IMPORT_PARAM_RECORD_TYPE)));
        range.getFirst().setText(value.get(IMPORT_PARAM_RANGE_BEGIN));
        range.getSecond().setText(value.get(IMPORT_PARAM_RANGE_END));
        sorted.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_SORTED)));
        size.setText(value.get(IMPORT_PARAM_SIZE));
        separator.setText(value.get(IMPORT_PARAM_SEPARATOR));
        upper.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_UPPER)));
        removeParenthese.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_PARENTHESE)));
        removeAfter.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_REMOVE_AFTER)));
        reverseArtist.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_REVERSE_ARTIST)));
        order.setSelected(Boolean.parseBoolean(value.get(IMPORT_PARAM_ARTIST_FIRST)));
        fichier = new Fichier();
        try {
            fichier.setCreationDate(LocalDateTime.parse(value.get(IMPORT_PARAM_CREATE), fullDTF));
        } catch (DateTimeParseException e) {
            LOG.warn("Error when parsing creation date", e);
        }
        label.add("Paramètres importés");
    }

    private void initInputsWithGuessedParams(List<String> label, List<String> randomLineAndLastLines,
            DateTimeFormatter fullDTF) {
        LOG.debug("Start initInputsWithGuessedParams");
        fichier = ImportFile.convertOneFile(file);
        if (randomLineAndLastLines.size() == 6) {
            fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(3)));
            fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
            separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(3)));
            sorted.setSelected(fichier.getSorted());
        } else if (!randomLineAndLastLines.isEmpty()) {
            label.add("Fichier trop petit, paramètres devinés sur la 1ère ligne du fichier.");
            fichier.setSorted(ImportFile.isSorted(randomLineAndLastLines.get(0)));
            fichier.setSize(ImportFile.determineSize(fichier, randomLineAndLastLines, file.getAbsolutePath()));
            separator.setText(ImportFile.getSeparator(randomLineAndLastLines.get(0)));
            sorted.setSelected(fichier.getSorted());
        }
        absolutePathFileXml = Constant.getXmlPath() + fichier.getFileName() + MyConstant.XML_EXTENSION;
        RecordType determineType = ImportFile.determineType(file.getName());
        boolean rangeDatesZero = fichier.getRangeDateBegin() == 0 && fichier.getRangeDateEnd() == 0;
        if (Cat.MISCELLANEOUS == fichier.getCategorie() && RecordType.UNKNOWN != determineType
                && fichier.getPublishYear() != 0 && rangeDatesZero) {
            fichier.setCategorie(Cat.YEAR);
            fichier.setRangeDateBegin(fichier.getPublishYear());
            fichier.setRangeDateEnd(fichier.getPublishYear());
        }
        reverseArtist.setSelected(ImportFile.countCharacter(file, ",") > fichier.getSize() / 3);
        removeParenthese.setSelected(ImportFile.countCharacter(file, "(") > fichier.getSize() / 3);
        name.setText(fichier.getFileName());
        author.setText(fichier.getAuthor());
        date.setText(fullDTF.format(fichier.getCreationDate()));
        cat.setSelectedItem(fichier.getCategorie());
        publi.setText(String.valueOf(fichier.getPublishYear()));
        type.setSelectedItem(determineType);
        range.getFirst().setText(String.valueOf(fichier.getRangeDateBegin()));
        range.getSecond().setText(String.valueOf(fichier.getRangeDateEnd()));
        size.setText(String.valueOf(fichier.getSize()));
        label.add("Paramètres devinés");
        LOG.debug("End initInputsWithGuessedParams");
    }

    /**
     * Traitement lorsqu'on fusionne tous les fichiers xml.
     *
     * @throws InterruptedException
     */
    private void fusionFilesAction() {
        LOG.debug("Start fusionFilesAction");
        OngletPanel.getArtist().interruptUpdateArtist(true);
        result = new LinkedList<>(Arrays.asList("Fichiers fusionnés"));
        try {
            ImportXML.fusionFiles(Constant.getXmlPath(), resultLabel);
        } catch (MajorException e) {
            LOG.error("Erreur lors de la fusion de tous les fichiers xml", e);
            result = new LinkedList<>(Arrays.asList(e.toString()));
        }
        try {
            ImportXML.synchroDeletedWithFinal();
        } catch (MinorException e) {
            LOG.error("Erreur lors de la détection de composition supprimées", e);
            result = new LinkedList<>(Arrays.asList(e.toString()));
        }
        SwingUtilities.invokeLater(() -> {
            OngletPanel.getFichier().updateData();
            OngletPanel.getArtist().updateArtistData();
            miseEnFormeResultLabel(result);
        });
        LOG.debug("End fusionFilesAction");
    }

    /**
     * Pour ouvrir un fichier dans notepad.
     */
    private void openFileNotepad(String path) {
        LOG.debug("Start openFileNotepad");
        try {
            FilesUtils.openFileInNotepad(Optional.ofNullable(path).orElse(null), null);
        } catch (MajorException e) {
            result = new LinkedList<>(Arrays.asList(e.toString()));
            miseEnFormeResultLabel(result);
            LOG.error("Erreur lors de l'ouverture du fichier: {}", path, e);
        }
        LOG.debug("End openFileNotepad");
    }

    /**
     * Executed action when cleaning a txt file.
     */
    private void cleanFileAction() {
        LOG.debug("Start cleanFileAction");
        String txt = "";
        if (file == null) {
            txt = "Sélectionnez un fichier !";
        } else {
            String text = maxLengthClean.getText();
            if (StringUtils.isBlank(text)) {
                txt = "Pas de valeur pour la taille maximale !";
            } else if (!StringUtils.isNumeric(text)) {
                txt = "La valeur entrée pour la taille maximale n'est pas un nombre !";
            } else if (Integer.parseInt(text) < 0) {
                txt = "La valeur entrée pour la taille maximale est négative !";
            } else {
                CleanFile.clearFile(file, sorted.isSelected(), separator.getText(), characterToRemove.getText(),
                        Integer.parseInt(text), isBefore.isSelected());
                txt = file.getName() + " nettoyé !";
                selectGeneratedFile(GENERATED_CLEAN);
            }
        }
        result = new LinkedList<>(Arrays.asList(txt));
        miseEnFormeResultLabel(result);
        LOG.debug("End cleanFileAction");
    }

    private void selectGeneratedFile(String type) {
        getGeneratedFile(type).ifPresent(generatedFile -> {
            generated.setSelectedItem(type);
            openGenerated.setEnabled(new File(generatedFile).exists());
        });
    }

    private Optional<String> getGeneratedFile(String type) {
        if (StringUtils.isAnyBlank(type, absolutePathFileTxt)) {
            return Optional.empty();
        }
        String generatedFile = null;
        switch (type) {
        case GENERATED_CLEAN:
            generatedFile = CleanFile.buildGeneratedFilePath(new File(absolutePathFileTxt), CleanFile.SUFFIX_CLEAR);
            break;
        case GENERATED_XML:
            generatedFile = absolutePathFileXml;
            break;
        case GENERATED_MISE_EN_FORME:
            generatedFile = CleanFile.buildGeneratedFilePath(new File(absolutePathFileTxt), CleanFile.SUFFIX_MEF);
            break;
        default:
            generatedFile = null;
            break;
        }
        return Optional.ofNullable(generatedFile);
    }

    @Override
    public JButton getActionButton() {
        return importFile;
    }
}
