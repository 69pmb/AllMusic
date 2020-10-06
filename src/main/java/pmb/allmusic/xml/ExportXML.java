/**
 *
 */
package pmb.allmusic.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import pmb.allmusic.exception.MajorException;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.utils.MiscUtils;

/**
 * Classe pour exporter des fichiers au format XML.
 *
 * @author pmbroca
 */
public class ExportXML {

    private static final Logger LOG = LogManager.getLogger(ExportXML.class);

    private static boolean finalFileChanged;
    private static final Comparator<Composition> BY_RANK = (c1, c2) -> c1.getFiles().get(0).getClassement().compareTo(c2.getFiles().get(0).getClassement());

    protected ExportXML() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Converts the given Compositions list in XML and then saves it in a file.
     *
     * @param compoList List<{@link Composition}> compositions to save
     * @param fileName {@link String} file name
     * @throws MajorException if error the file when saving file
     */
    public static void exportXML(List<Composition> compoList, String fileName) throws MajorException {
        LOG.debug("Start exportXML: {}", fileName);
        List<Composition> sorted = compoList.stream().sorted(BY_RANK).collect(Collectors.toList());
        Document doc = DocumentHelper.createDocument();
        Element listComp = doc.addElement(CompoHandler.TAG_ROOT);
        DateTimeFormatter fullDTF = new Constant().getFullDTF();

        // Export file in Dropbox if final file
        if (Constant.getFinalFile().equals(fileName)) {
            finalFileChanged = true;
        }

        for (Composition composition : sorted) {
            Element comp = listComp.addElement(CompoHandler.TAG_COMPOSITION);
            comp.addAttribute(CompoHandler.TAG_ARTIST, String.valueOf(composition.getArtist()));
            comp.addAttribute(CompoHandler.TAG_TITRE, String.valueOf(composition.getTitre()));
            comp.addAttribute(CompoHandler.TAG_TYPE, String.valueOf(composition.getRecordType()));
            comp.addAttribute(CompoHandler.TAG_UUID, MiscUtils.uuidsToString(composition.getUuids()));
            comp.addAttribute(CompoHandler.TAG_CAN_BE_MERGED, String.valueOf(composition.isCanBeMerged()));
            comp.addAttribute(CompoHandler.TAG_DELETED, String.valueOf(composition.isDeleted()));
            exportFichier(composition.getFiles(), fullDTF, comp);
        }
        saveFile(fileName, doc);
        LOG.debug("End exportXML");
    }

    /**
     * Converts the {@link Fichier} list of the given composition to xml.
     *
     * @param files files that will be converted
     * @param fullDTF date time formatter
     * @param comp the xml element where files will be added
     * @return the modified tag element
     */
    protected static Element exportFichier(List<Fichier> files, DateTimeFormatter fullDTF, Element comp) {
        for (Fichier fichier : files) {
            Element file = comp.addElement(CompoHandler.TAG_FILE);
            try {
                if (StringUtils.equalsIgnoreCase(fichier.getAuthor(), Constant.VARIOUS_AUTHOR)) {
                    file.addAttribute(CompoHandler.TAG_AUTHOR, String.valueOf(fichier.getAuthor()));
                    file.addAttribute(CompoHandler.TAG_PUBLISH_YEAR, String.valueOf(fichier.getPublishYear()));
                }
                file.addAttribute(CompoHandler.TAG_FILENAME,
                        String.valueOf(fichier.getFileName()));
                file.addAttribute(CompoHandler.TAG_CATEGORIE,
                        String.valueOf(fichier.getCategorie()));
                file.addAttribute(CompoHandler.TAG_RANGE_DATE_BEGIN,
                        String.valueOf(fichier.getRangeDateBegin()));
                file.addAttribute(CompoHandler.TAG_RANGE_DATE_END,
                        String.valueOf(fichier.getRangeDateEnd()));
                file.addAttribute(CompoHandler.TAG_SORTED, String.valueOf(fichier.getSorted()));
                file.addAttribute(CompoHandler.TAG_CLASSEMENT,
                        String.valueOf(fichier.getClassement()));
                file.addAttribute(CompoHandler.TAG_CREATION_DATE,
                        fullDTF.format(fichier.getCreationDate()));
                file.addAttribute(CompoHandler.TAG_SIZE, String.valueOf(fichier.getSize()));
            } catch (NullPointerException e) {
                LOG.error("comp: {}", comp, e);
                LOG.error("file: {}", file);
                LOG.error("composition.getFiles(): {}", files);
            }
        }
        return comp;
    }

    /**
     * Sauvegarde du document dans un fichier.
     *
     * @param fileName Le nom du fichier
     * @param doc Le document a enregistrer
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static void saveFile(String fileName, Document doc) throws MajorException {
        LOG.debug("Start saveFile");
        // Création du dossier resources
        FilesUtils.createFolderIfNotExists(Constant.getResourcesDir());

        // Création du dossier xml dans le dossier resources
        FilesUtils.createFolderIfNotExists(Constant.getXmlPath());

        // Nom des fichiers
        String fullFileName = fileName;
        if (!StringUtils.endsWith(fileName, Constant.XML_EXTENSION)) {
            fullFileName += Constant.XML_EXTENSION;
        }
        // Sauvegarde du document dans le fichier
        writeCompositionInFile(doc, fullFileName);
        LOG.debug("End saveFile");
    }

    /**
     * Saves the xml document in a file in the xml directory.
     *
     * @param doc the xml to save
     * @param fullFileName the name of the file
     * @throws IOException if something went wrong (file not found, encoding error..)
     */
    protected static void writeCompositionInFile(Document doc, String fullFileName) throws MajorException {
        try (FileOutputStream fos = new FileOutputStream(Constant.getXmlPath() + fullFileName)) {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndent(true);
            format.setNewlines(true);
            XMLWriter xmlOut = new XMLWriter(fos, format);
            xmlOut.write(doc);
        } catch (IOException e) {
            throw new MajorException("Error when writing compositions in file: " + fullFileName, e);
        }
    }

    public static boolean isFinalFileChanged() {
        return finalFileChanged;
    }
}
