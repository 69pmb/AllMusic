package pmb.allmusic.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.view.ColumnIndex;
import pmb.allmusic.view.ColumnIndex.Index;
import pmb.allmusic.view.panel.ImportPanel;
import pmb.allmusic.xml.ExportXML;
import pmb.allmusic.xml.ImportXML;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.VariousUtils;

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
     * @param compoList      {@code List<Composition>} the list to convert
     * @param getComposition if true recovers the associated composition for each
     *                       file
     * @param lineNumber     if true add a columns for line number
     * @return Vector<Vector<Object>> the list converted
     */
    public static Vector<Vector<Object>> convertCompositionListToFichierVector(List<Composition> compoList,
            boolean getComposition, boolean lineNumber) {
        LOG.debug("Start convertCompositionListToFichierVector, getComposition: {}, lineNumber: {}", getComposition,
                lineNumber);
        return compoList.parallelStream().map(c -> c.getFiles().parallelStream().map(f -> {
            Vector<Object> v = new Vector<>();
            if (lineNumber) {
                // lineNumber
                v.addElement(0);
            }
            List<Composition> importXML = ImportXML
                    .importXML(Constant.getXmlPath() + f.getFileName() + MyConstant.XML_EXTENSION);
            Composition compo = new Composition();
            if (getComposition) {
                compo  = CompositionUtils.findByUuid(importXML, c.getUuids()).orElseGet(() -> {
                    LOG.warn("No result when searching composition by its file: {}, {}, {}", f, c.getArtist(),
                            c.getTitre());
                    return c;
                });
                // If fetch composition details
                MiscUtils.addElements(v, compo.getArtist(), compo.getTitre(), compo.getRecordType().name());
            }
            MiscUtils.addElements(v, f.getAuthor(), f.getFileName());
            if (!getComposition) {
                // Type
                v.addElement(importXML.get(0).getRecordType().toString());
            }
            MiscUtils.addElements(v, f.getPublishYear(), f.getCategorie().getValue(),
                    f.getRangeDateBegin() + " - " + f.getRangeDateEnd(), getPercentOfDeleted(f, importXML));
            if (!getComposition) {
                v.addElement(f.getCreationDate());
            }
            v.addElement(f.getSize());
            if (getComposition) {
                MiscUtils.addElements(v, ScoreUtils.getCompositionScore(compo), f.getClassement(),
                        Boolean.toString(compo.isDeleted()), VariousUtils.uuidsToString(compo.getUuids()));
            }
            v.addElement(BooleanUtils.isTrue(f.getSorted()) ? "Oui" : "Non");
            return v;
        }).collect(MiscUtils.toVector())).flatMap(Vector::stream).collect(MiscUtils.toVector());
    }

    private static String getPercentOfDeleted(Fichier f, List<Composition> importXML) {
        BigDecimal numberOfDeleted = new BigDecimal(importXML.stream().reduce(0,
                (sum, item) -> item.isDeleted() ? sum + 1 : sum, (sumA, sumB) -> sumA + sumB));
        BigDecimal size = new BigDecimal(f.getSize() == 0 ? importXML.size() : f.getSize());
        return BigDecimal.valueOf(100D).setScale(2).multiply(numberOfDeleted).divide(size, RoundingMode.HALF_UP)
                .doubleValue() + " %";
    }

    /**
     * Modifie un fichier, dans le fichier final.xml, dans son fichier xml et
     * renomme si besoin les fichiers XML et TXT.
     *
     * @param fileName    l'ancien nom du fichier
     * @param newFileName le nouveau nom du fichier
     * @param newPublish  la nouvelle date de publication
     * @param newRange    le nouveau range
     * @param newCat      la nouvelle catégorie
     * @param newSize     la nouvelle taille
     * @param newSorted   le nouveau sort
     * @return le nouveau fichier
     * @throws MajorException si une erreur surviens pendant les exports xml
     */
    public static Fichier modifyFichier(String fileName, String newFileName, String newPublish, String newRange,
            String newCat, String newSize, String newSorted) throws MajorException {
        // Modification du fichier xml
        List<Composition> compoList = ImportXML.importXML(Constant.getXmlPath() + fileName + MyConstant.XML_EXTENSION);
        compoList.stream()
        .forEach(modifyOneFichier(fileName, newFileName, newPublish, newRange, newCat, newSize, newSorted));
        Fichier result = compoList.get(0).getFiles().get(0);
        try {
            // Sauvegarde des modifications sous le nouveau nom de fichier
            ExportXML.exportXML(compoList, newFileName);
        } catch (MajorException e) {
            throw new MajorException("Erreur lors de la modification d'une composition dans le fichier: " + fileName,
                    e);
        }
        // Supprime l'ancien fichier
        if (!StringUtils.equals(fileName, newFileName)) {
            try {
                Files.delete(Paths.get(Constant.getXmlPath() + fileName + MyConstant.XML_EXTENSION));
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
        } catch (MajorException e) {
            throw new MajorException("Erreur lors de la modification d'une composition dans le fichier final", e);
        }
        // Renomme le fichier txt
        String txtPath = FilesUtils.buildTxtFilePath(fileName, result.getAuthor()).orElseThrow(() -> new MajorException(
                "Can't build txt file path of: " + fileName + " with author: " + result.getAuthor()));
        String newTxt = StringUtils
                .substringBeforeLast(StringUtils.substringBeforeLast(txtPath, MyConstant.TXT_EXTENSION), MyConstant.FS)
                + MyConstant.FS + newFileName + MyConstant.TXT_EXTENSION;
        if (!new File(txtPath).renameTo(new File(newTxt))) {
            throw new MinorException("Failed to rename " + txtPath + " to " + newTxt);
        }
        // Modifie ses import params
        String firstLine = MyFileUtils.readFirstLine(newTxt);
        if (StringUtils.startsWith(firstLine, Constant.IMPORT_PARAMS_PREFIX)) {
            Map<String, String> value = new HashMap<>();
            try {
                value = VariousUtils.<String>readValueAsMap(
                        StringUtils.substringAfter(firstLine, Constant.IMPORT_PARAMS_PREFIX));
            } catch (IOException e) {
                LOG.error("Error while decoding import params: {} in file {}", firstLine, newTxt, e);
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
     * @param fileName    file name of the file to edit
     * @param newFileName new file name
     * @param newPublish  new publish year
     * @param newRange    range date begin and end combined with a {@code " - "}
     * @param newCat      new category
     * @param newSize     new size
     * @param newSorted   {@code oui} or {@code non} for new sort value
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
                fichier.setCategorie(Cat.getByValue(newCat));
                fichier.setSize(Integer.valueOf(newSize));
                fichier.setSorted(StringUtils.equalsIgnoreCase(newSorted, "oui") ? Boolean.TRUE : Boolean.FALSE);
            });
        };
    }

    /**
     * Builds a predicate to filter {@link Fichier} stream with a row.
     *
     * @param row   {@code Vector<Object>} a row
     * @param index {@link ColumnIndex} of the row
     * @return a predicate
     */
    public static Predicate<Fichier> filterFichier(Vector<Object> row, ColumnIndex index) {
        return f -> !(f.getClassement().equals(row.get(index.get(Index.RANK)))
                && StringUtils.equals(f.getFileName(), (String) row.get(index.get(Index.FILE_NAME)))
                && f.getSize().equals(row.get(index.get(Index.FILE_SIZE))));
    }
}
