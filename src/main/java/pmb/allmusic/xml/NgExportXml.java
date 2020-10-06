/**
 *
 */
package pmb.allmusic.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.dropbox.core.v2.files.WriteMode;

import pmb.allmusic.exception.MajorException;
import pmb.allmusic.exception.MinorException;
import pmb.allmusic.file.CleanFile;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.DropBoxUtils;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.utils.MiscUtils;
import pmb.allmusic.utils.ScoreUtils;
import pmb.allmusic.view.panel.OngletPanel;

/**
 * Classe pour exporter des fichiers au format XML pour être utilisé par une
 * application Angular.
 *
 */
public final class NgExportXml extends ExportXML {

    private static final Logger LOG = LogManager.getLogger(NgExportXml.class);
    private static final String TAG_FICHIER_ROOT = "Fichiers";
    private static final String FICHIER_FILE_NAME = "fichiers";

    private NgExportXml() {
        super();
    }

    /**
     * Converti en XML la liste de composition puis sauvegarde dans un fichier.
     *
     * @param compList List<{@link Composition}> les compositions à sauvegarder
     * @param fileName {@link String} le nom du fichier
     * @throws MajorException if error occured when when saving the file
     */
    public static void exportXML(List<Composition> compList, String fileName) throws MajorException {
        LOG.debug("Start ngExportXml");
        // Export Compositions
        Document doc = DocumentHelper.createDocument();
        Element listComp = doc.addElement(CompoHandler.TAG_ROOT);
        DateTimeFormatter fullDTF = new Constant().getFullDTF();
        Set<Entry<String, String>> entrySet = CleanFile.getModifSet();
        compList.forEach(composition -> exportFichier(composition.getFiles(), fullDTF,
                formatComposition(listComp.addElement(CompoHandler.TAG_COMPOSITION), entrySet, composition, null)));
        saveFile(fileName, doc);

        // Export Fichiers
        Document doc2 = DocumentHelper.createDocument();
        Element listFichier = doc2.addElement(TAG_FICHIER_ROOT);
        OngletPanel.getFichier().getData().forEach((key, value) -> {
            exportFichier(Collections.singletonList(key), fullDTF, listFichier);
            Element fichier = listFichier.elements().get(listFichier.elements().size() - 1);
            fichier.addAttribute(CompoHandler.TAG_TYPE, getType(value));
            value.forEach(comp -> formatComposition(fichier.addElement(CompoHandler.TAG_COMPOSITION), entrySet, comp, key));
        });
        saveFile(FICHIER_FILE_NAME, doc2);
        LOG.debug("End ngExportXml");
    }

    private static String getType(List<Composition> list) {
        Map<RecordType, Long> collect = list.stream().collect(Collectors.groupingBy(Composition::getRecordType, Collectors.counting()));
        if(collect.size() == 1) {
            return collect.keySet().iterator().next().name();
        } else {
            return RecordType.UNKNOWN.name();
        }
    }

    private static Element formatComposition(Element comp, Set<Entry<String, String>> entrySet,
            Composition composition, Fichier fichier) {
        comp.addAttribute(CompoHandler.TAG_ARTIST, String.valueOf(composition.getArtist()));
        comp.addAttribute(CompoHandler.TAG_TITRE, String.valueOf(composition.getTitre()));

        // Clean artist and title
        String stripArtist = StringUtils.substringBefore(
                MiscUtils.removeParentheses(CleanFile
                        .removeDiactriticals(MiscUtils.cleanLine(composition.getArtist().toLowerCase(), entrySet))),
                " and ");
        if (StringUtils.startsWith(stripArtist, "the ")) {
            stripArtist = StringUtils.substringAfter(stripArtist, "the ");
        }
        comp.addAttribute("s" + CompoHandler.TAG_ARTIST, MiscUtils.removePunctuation(stripArtist));
        comp.addAttribute("s" + CompoHandler.TAG_TITRE, MiscUtils.removePunctuation(MiscUtils.removeParentheses(
                CleanFile.removeDiactriticals(MiscUtils.cleanLine(composition.getTitre().toLowerCase(), entrySet)))));

        comp.addAttribute(CompoHandler.TAG_TYPE, String.valueOf(composition.getRecordType()));
        comp.addAttribute(CompoHandler.TAG_DELETED, String.valueOf(composition.isDeleted()));
        if (fichier != null) {
            composition.getFiles().stream()
            .filter(f -> StringUtils.equalsIgnoreCase(f.getFileName(), fichier.getFileName())).findFirst()
            .map(Fichier::getClassement)
            .ifPresent(rank -> comp.addAttribute(CompoHandler.TAG_CLASSEMENT, String.valueOf(rank)));
        }
        comp.addAttribute("size", String.valueOf(composition.getFiles().size()));
        long calculatedScore = ScoreUtils.getCompositionScore(composition);
        comp.addAttribute("score", String.valueOf(calculatedScore));
        comp.addAttribute("decile", String.valueOf(ScoreUtils.getDecile(composition.getRecordType(), calculatedScore)));
        return comp;
    }

    /**
     * Sauvegarde du document dans un fichier.
     *
     * @param fileName Le nom du fichier
     * @param doc      Le document a enregistrer
     * @throws MajorException if error occured when when saving the file
     */
    private static void saveFile(String fileName, Document doc) throws MajorException {
        LOG.debug("Start saveFile");
        // Nom du fichier
        String fullFileName = fileName;
        if (StringUtils.endsWith(fullFileName, Constant.XML_EXTENSION)) {
            fullFileName = StringUtils.substringBeforeLast(fullFileName, Constant.XML_EXTENSION);
        }
        fullFileName += ";" + MiscUtils.dateNow() + Constant.XML_EXTENSION;

        // Sauvegarde du document dans le fichier
        try {
            writeCompositionInFile(doc, fullFileName);
            File exportedFile = new File(Constant.getXmlPath() + fullFileName);
            File zipFile = FilesUtils.zipFile(exportedFile);
            DropBoxUtils.uploadFile(zipFile, "XML/" + zipFile.getName(), WriteMode.OVERWRITE);
            Files.delete(zipFile.toPath());
            Files.delete(exportedFile.toPath());
        } catch (MajorException e) {
            throw new MajorException("Error when saving file: " + fullFileName + " to dropbox", e);
        } catch (IOException e) {
            throw new MinorException("Error when deleting file: " + Constant.getXmlPath() + fullFileName, e);
        }
        LOG.debug("End saveFile");
    }
}
