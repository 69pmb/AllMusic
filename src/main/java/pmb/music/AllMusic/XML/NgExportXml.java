/**
 *
 */
package pmb.music.AllMusic.XML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.dropbox.core.v2.files.WriteMode;

import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.exception.MinorException;
import pmb.music.AllMusic.file.CleanFile;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.DropBoxUtils;
import pmb.music.AllMusic.utils.FilesUtils;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.ScoreUtils;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Classe pour exporter des fichiers au format XML pour être utilisé par une
 * application Angular.
 *
 */
public final class NgExportXml extends ExportXML {

    private static final Logger LOG = LogManager.getLogger(NgExportXml.class);

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
        Document doc = DocumentHelper.createDocument();
        Element listComp = doc.addElement(CompoHandler.TAG_ROOT);
        DateTimeFormatter fullDTF = new Constant().getFullDTF();
        Set<Entry<String, String>> entrySet = CleanFile.getModifSet();

        for (int i = 0; i < compList.size(); i++) {
            // Ajout element <Order/>
            Element comp = listComp.addElement(CompoHandler.TAG_COMPOSITION);
            Composition composition = compList.get(i);
            comp.addAttribute(CompoHandler.TAG_ARTIST, String.valueOf(composition.getArtist()));
            comp.addAttribute(CompoHandler.TAG_TITRE, String.valueOf(composition.getTitre()));

            // Clean artist and title
            String stripArtist = StringUtils
                    .substringBefore(
                            MiscUtils.removeParentheses(CleanFile.removeDiactriticals(
                                    MiscUtils.cleanLine(composition.getArtist().toLowerCase(), entrySet))),
                            " and ");
            if (StringUtils.startsWith(stripArtist, "the ")) {
                stripArtist = StringUtils.substringAfter(stripArtist, "the ");
            }
            comp.addAttribute("s" + CompoHandler.TAG_ARTIST, MiscUtils.removePunctuation(stripArtist));
            comp.addAttribute("s" + CompoHandler.TAG_TITRE,
                    MiscUtils.removePunctuation(MiscUtils.removeParentheses(CleanFile.removeDiactriticals(
                            MiscUtils.cleanLine(composition.getTitre().toLowerCase(), entrySet)))));

            comp.addAttribute(CompoHandler.TAG_TYPE, String.valueOf(composition.getRecordType()));
            comp.addAttribute(CompoHandler.TAG_DELETED, String.valueOf(composition.isDeleted()));
            comp.addAttribute("size", String.valueOf(composition.getFiles().size()));
            long calculatedScore = ScoreUtils.getCompositionScore(
                    OngletPanel.getScore().getLogMax(composition.getRecordType()),
                    OngletPanel.getScore().getDoubleMedian(composition.getRecordType()), composition);
            comp.addAttribute("score", String.valueOf(calculatedScore));
            comp.addAttribute("decile",
                    String.valueOf(ScoreUtils.getDecile(composition.getRecordType(), calculatedScore)));

            exportFichier(composition, fullDTF, comp);
        }
        saveFile(fileName, doc);
        LOG.debug("End ngExportXml");
    }

    /**
     * Sauvegarde du document dans un fichier.
     *
     * @param fileName Le nom du fichier
     * @param doc Le document a enregistrer
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
