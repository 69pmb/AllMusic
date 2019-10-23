/**
 *
 */
package pmb.music.AllMusic.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.exception.MinorException;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.view.ColumnIndex;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.panel.ImportPanel;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Classe utilitaire pour la gestion des {@link Fichier}.
 *
 */
public final class FichierUtils {

    private static final Logger LOG = LogManager.getLogger(FichierUtils.class);

    private FichierUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Convertit une liste de {@link Composition} en {@link Vector<Vector<Object>>}
     * de Fichier.
     *
     * @param compoList {@code List<Composition>} the list to convert
     * @param getComposition if true recovers the associated composition for each
     *            file
     * @param lineNumber if true add a columns for line number
     * @return Vector<Vector<Object>> the list converted
     */
    public static Vector<Vector<Object>> convertCompositionListToFichierVector(List<Composition> compoList,
            boolean getComposition, boolean lineNumber) {
        LOG.debug("Start convertCompositionListToFichierVector, getComposition: {}, lineNumber: {}", getComposition, lineNumber);
        Vector<Vector<Object>> result = new Vector<>();
        compoList.parallelStream().forEach(c -> c.getFiles().parallelStream().forEach(f -> {
            Vector<Object> v = new Vector<>();
            if (lineNumber) {
                // lineNumber
                v.addElement(0);
            }
            List<Composition> importXML = ImportXML
                    .importXML(Constant.getXmlPath() + f.getFileName() + Constant.XML_EXTENSION);
            Optional<Composition> optCompo = null;
            if (getComposition) {
                // If fetch composition details
                optCompo = CompositionUtils.findByUuid(importXML, c.getUuids());
                if (optCompo.isPresent()) {
                    Composition compo = optCompo.get();
                    // Artist
                    v.addElement(compo.getArtist());
                    // Titre
                    v.addElement(compo.getTitre());
                    // Type
                    v.addElement(compo.getRecordType().toString());
                } else {
                    LOG.warn("No result when searching composition by its file: {}, {}, {}", f, c.getArtist(), c.getTitre());
                    v.addElement(c.getArtist());
                    v.addElement(c.getTitre());
                    v.addElement(c.getRecordType().toString());
                    v.addElement(0L);
                }
            }
            // Author
            v.addElement(f.getAuthor());
            // File name
            v.addElement(f.getFileName());
            if (!getComposition) {
                // Type
                v.addElement(importXML.get(0).getRecordType().toString());
            }
            v.addElement(f.getPublishYear());
            v.addElement(f.getCategorie().getCat());
            v.addElement(f.getRangeDateBegin() + " - " + f.getRangeDateEnd());
            // % of deleted
            int reduce = importXML.stream().reduce(0,
                    (sum, item) -> item.isDeleted() ? sum + 1 : sum, (sumA, sumB) -> sumA + sumB);
            BigDecimal numberOfDeleted = new BigDecimal(reduce);
            BigDecimal size = new BigDecimal(f.getSize() == 0 ? importXML.size() : f.getSize());
            v.addElement(BigDecimal.valueOf(100D).setScale(2).multiply(numberOfDeleted)
                    .divide(size, RoundingMode.HALF_UP).doubleValue() + " %");
            if (!getComposition) {
                v.addElement(f.getCreationDate());
            }
            v.addElement(f.getSize());
            if (getComposition) {
                Composition compo = optCompo.get();
                // Score
                v.addElement(ScoreUtils.getCompositionScore(
                        OngletPanel.getScore().getLogMax(compo.getRecordType()),
                        OngletPanel.getScore().getDoubleMedian(compo.getRecordType()), compo));
                // Rank
                v.addElement(f.getClassement());
                // Deleted
                v.addElement(Boolean.toString(compo.isDeleted()));
                // Uuid
                v.addElement(MiscUtils.uuidsToString(compo.getUuids()));
            }
            v.addElement(f.getSorted() ? "Oui" : "Non");
            result.add(v);
        }));
        LOG.debug("End convertCompositionListToFichierVector");
        return result;
    }

    /**
     * Modifie un fichier, dans le fichier final.xml, dans son fichier xml et
     * renomme si besoin les fichiers XML et TXT.
     *
     * @param fileName l'ancien nom du fichier
     * @param newFileName le nouveau nom du fichier
     * @param newPublish la nouvelle date de publication
     * @param newRange le nouveau range
     * @param newCat la nouvelle catégorie
     * @param newSize la nouvelle taille
     * @param newSorted le nouveau sort
     * @return le nouveau fichier
     * @throws MajorException si une erreur surviens pendant les exports xml
     */
    public static Fichier modifyFichier(String fileName, String newFileName, String newPublish, String newRange,
            String newCat, String newSize, String newSorted) throws MajorException {
        // Modification du fichier xml
        List<Composition> compoList = ImportXML.importXML(Constant.getXmlPath() + fileName + Constant.XML_EXTENSION);
        compoList.stream()
        .forEach(modifyOneFichier(fileName, newFileName, newPublish, newRange, newCat, newSize, newSorted));
        Fichier result = compoList.get(0).getFiles().get(0);
        try {
            // Sauvegarde des modifications sous le nouveau nom de fichier
            ExportXML.exportXML(compoList, newFileName);
        } catch (IOException e) {
            throw new MajorException("Erreur lors de la modification d'une composition dans le fichier: " + fileName, e);
        }
        // Supprime l'ancien fichier
        if (!StringUtils.equals(fileName, newFileName)) {
            try {
                Files.delete(Paths.get(Constant.getXmlPath() + fileName + Constant.XML_EXTENSION));
            } catch (IOException e) {
                LOG.warn("Error when deleting file: {}", fileName, e);
            }
        }
        // Modification du fichier final.xml
        List<Composition> finalList = ImportXML.importXML(Constant.getFinalFilePath());
        finalList.stream()
        .filter(c -> c.getFiles().stream().anyMatch(f -> StringUtils.equals(f.getFileName(), fileName)))
        .forEach(modifyOneFichier(fileName, newFileName, newPublish, newRange, newCat, newSize, newSorted));
        try {
            // Sauvegarde des modifications
            ExportXML.exportXML(finalList, Constant.getFinalFile());
        } catch (IOException e) {
            throw new MajorException("Erreur lors de la modification d'une composition dans le fichier final", e);
        }
        // Renomme le fichier txt
        String txtPath = FilesUtils.buildTxtFilePath(fileName, result.getAuthor()).orElseThrow(() -> new MajorException(
                "Can't build txt file path of: " + fileName + " with author: " + result.getAuthor()));
        String newTxt = StringUtils
                .substringBeforeLast(StringUtils.substringBeforeLast(txtPath, Constant.TXT_EXTENSION), FileUtils.FS)
                + FileUtils.FS + newFileName + Constant.TXT_EXTENSION;
		if (!new File(txtPath).renameTo(new File(newTxt))) {
			throw new MinorException("Failed to rename " + txtPath + " to " + newTxt);
		}
        // Modifie ses import params
        Optional<String> firstLine = FilesUtils.readFirstLine(newTxt);
        if (firstLine.isPresent() && StringUtils.startsWith(firstLine.get(), Constant.IMPORT_PARAMS_PREFIX)) {
            Map<String, String> value = new HashMap<>();
            try {
                value = MiscUtils.<String>readValueAsMap(
                        StringUtils.substringAfter(firstLine.get(), Constant.IMPORT_PARAMS_PREFIX));
            } catch (IOException e) {
                LOG.error("Error while decoding import params:" + firstLine + " in file " + newTxt, e);
            }
            String[] split = StringUtils.split(newRange, " - ");
            value.put(ImportPanel.IMPORT_PARAM_NAME, newFileName);
            value.put(ImportPanel.IMPORT_PARAM_CATEGORIE, newCat);
            value.put(ImportPanel.IMPORT_PARAM_RANGE_BEGIN, split[0]);
            value.put(ImportPanel.IMPORT_PARAM_RANGE_END, split[1]);
            value.put(ImportPanel.IMPORT_PARAM_SORTED,
                    StringUtils.equalsIgnoreCase(newSorted, "oui") ? Boolean.TRUE.toString()
                            : Boolean.FALSE.toString());
            value.put(ImportPanel.IMPORT_PARAM_PUBLISH_YEAR, newPublish);
            value.put(ImportPanel.IMPORT_PARAM_SIZE, String.valueOf(newSize));
            FilesUtils.writeMapInTxtFile(new File(newTxt), value);
        }
        return result;
    }

    /**
     * Edits entry composition file with given filename with new values.
     *
     * @param fileName file name of the file to edit
     * @param newFileName new file name
     * @param newPublish new publish year
     * @param newRange range date begin and end combined with a {@code " - "}
     * @param newCat new category
     * @param newSize new size
     * @param newSorted {@code oui} or {@code non} for new sort value
     * @return a consumer making the described action
     */
    public static Consumer<Composition> modifyOneFichier(String fileName, String newFileName, String newPublish,
            String newRange, String newCat, String newSize, String newSorted) {
        return c -> {
            List<Fichier> list = c.getFiles().stream().filter(f -> StringUtils.equals(f.getFileName(), fileName))
                    .collect(Collectors.toList());
            list.forEach(fichier -> {
                fichier.setFileName(newFileName);
                fichier.setPublishYear(Integer.valueOf(newPublish));
                String[] split = StringUtils.split(newRange, " - ");
                fichier.setRangeDateBegin(Integer.valueOf(split[0]));
                fichier.setRangeDateEnd(Integer.valueOf(split[1]));
                fichier.setCategorie(Cat.valueOf(newCat));
                fichier.setSize(Integer.valueOf(newSize));
                fichier.setSorted(StringUtils.equalsIgnoreCase(newSorted, "oui") ? Boolean.TRUE : Boolean.FALSE);
            });
        };
    }

    /**
     * Builds a predicate to filter {@link Fichier} stream with a row.
     * @param row {@code Vector<Object>} a row
     * @param index {@link ColumnIndex} of the row
     * @return a predicate
     */
    public static Predicate<Fichier> filterFichier(Vector<Object> row, ColumnIndex index) {
        return f -> !(f.getClassement().equals(row.get(index.get(Index.RANK)))
                && StringUtils.equals(f.getFileName(), (String) row.get(index.get(Index.FILE_NAME)))
                && f.getSize().equals(row.get(index.get(Index.FILE_SIZE))));
    }
}
