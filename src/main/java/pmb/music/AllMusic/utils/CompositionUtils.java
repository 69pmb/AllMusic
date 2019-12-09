package pmb.music.AllMusic.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.exception.MajorException;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Classe utilitaire pour les {@link Composition}.
 *
 * @author pmbroca
 */
public final class CompositionUtils {

    private static final Logger LOG = LogManager.getLogger(CompositionUtils.class);

    private CompositionUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Détermine si la compo existe dans la liste donnée. C'est à dire, si le {@link RecordType} est le même et si le titre et l'artiste sont
     * similaires en utilisant leur score de JaroWinkler.
     *
     * @param compos la liste
     * @param c la compo à chercher
     * @return {@code null} rien trouvé, la 1ère {@link Composition} trouvée sinon
     */
    public static Composition compoExist(List<Composition> compos, Composition c) {
        Composition res = null;
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        Iterator<Composition> iterator = compos.iterator();
        while (iterator.hasNext()) {
            Composition composition = iterator.next();
            if (composition == null) {
                LOG.error("composition null");
            } else if (c.getRecordType() == composition.getRecordType()) {
                // Suppression de la ponctuation
                String compoTitre = MiscUtils.removePunctuation(composition.getTitre());
                if (StringUtils.isBlank(compoTitre)) {
                    // Si le titre n'est constitué que de ponctuation
                    compoTitre = composition.getTitre().toLowerCase();
                }
                // Suppression de la ponctuation
                String cTitre = MiscUtils.removePunctuation(c.getTitre());
                if (StringUtils.isBlank(cTitre)) {
                    // Si le titre n'est constitué que de ponctuation
                    cTitre = c.getTitre().toLowerCase();
                }
                // Si le titre et l'artist sont similaires, on sort
                if (SearchUtils.isEqualsJaro(jaro, compoTitre, cTitre, Constant.SCORE_LIMIT_TITLE_FUSION)
                        && artistJaroEquals(composition.getArtist(), c.getArtist(), jaro,
                                Constant.SCORE_LIMIT_ARTIST_FUSION) != null) {
                    res = composition;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Détermine si 2 artistes sont égaux en utilisant la distance de JaroWinkler.
     *
     * @param artist un artist
     * @param a un autre artist
     * @param jaro une instance de {@link JaroWinklerDistance}
     * @param scoreLimit the jaro score limit
     * @return {@code null} rien trouvé, le 1er artiste sinon
     */
    public static String artistJaroEquals(String artist, String a, JaroWinklerDistance jaro, BigDecimal scoreLimit) {
        // Suppression de la ponctuation
        String compoArtist = MiscUtils.removePunctuation(artist);
        if (StringUtils.startsWith(compoArtist, "the")) {
            // Si l'artist commence par The, on supprime le The
            compoArtist = StringUtils.substringAfter(compoArtist, "the");
        }
        if (StringUtils.isBlank(compoArtist)) {
            // Si l'artiste est constitué que de ponctuation ou de The
            compoArtist = artist.toLowerCase();
        }
        // Suppression de la ponctuation
        String cArtist = MiscUtils.removePunctuation(a);
        if (StringUtils.startsWith(cArtist, "the")) {
            // Si l'artist commence par The, on supprime le The
            cArtist = StringUtils.substringAfter(cArtist, "the");
        }
        if (StringUtils.isBlank(cArtist)) {
            // Si l'artiste est constitué que de ponctuation ou de The
            cArtist = a.toLowerCase();
        }
        if (SearchUtils.isEqualsJaro(jaro, compoArtist, cArtist, scoreLimit)) {
            // Si les artistes sont similaires on le retourne
            return artist;
        }
        return null;
    }

    /**
     * Convertit une liste de {@link Composition} en {@link Vector}.
     *
     * @param compoList {@code List<Composition>} la liste de composition à convertir
     * @param fichier le nom du fichier à utiliser pour le classement
     * @param displayClassement si on affiche le classement de la composition
     * @param displayFileSize si on affiche le nombre de fichiers de la composition
     * @param addBoolean si on ajoute une colonne de boolean remplie à false
     * @param score boolean if true, calculates the composition score
     * @param lineNumber if true add a column for counting line number
     * @return {@code Vector<Vector<Object>>} la liste de vecteur convertie
     */
    public static Vector<Vector<Object>> convertCompositionListToVector(List<Composition> compoList, String fichier,
            boolean displayClassement, boolean displayFileSize, boolean addBoolean, boolean score, boolean lineNumber) {
        LOG.debug("convertCompositionListToVector");
        if (CollectionUtils.isEmpty(compoList)) {
            LOG.debug("Empty list in convertCompositionListToVector");
            return new Vector<>();
        }
        return IntStream.range(0, compoList.size()).parallel().mapToObj(i -> {
            Composition composition = compoList.get(i);
            Vector<Object> v = new Vector<>();
            if (lineNumber) {
                v.addElement(i);
            }
            v.addElement(composition.getArtist());
            v.addElement(composition.getTitre());
            v.addElement(composition.getRecordType().name());
            if (displayClassement) {
                if (fichier != null) {
                    v.addElement(composition.getFiles().stream()
                            .filter(f -> StringUtils.equalsIgnoreCase(fichier, f.getFileName())).findFirst()
                            .map(Fichier::getClassement).orElse(0));
                } else {
                    v.addElement(composition.getFiles().get(0).getClassement());
                }
            }
            if (displayFileSize) {
                v.addElement(composition.getFiles().size());
            }
            if (score) {
                long calculatedScore = ScoreUtils.getCompositionScore(
                        OngletPanel.getScore().getLogMax(composition.getRecordType()),
                        OngletPanel.getScore().getDoubleMedian(composition.getRecordType()), composition);
                v.addElement(calculatedScore);
                v.addElement(ScoreUtils.getDecile(composition.getRecordType(), calculatedScore));
            }
            if (addBoolean) {
                v.addElement(Boolean.valueOf(false));
            }
            v.addElement(Boolean.toString(composition.isDeleted()));
            v.addElement(MiscUtils.uuidsToString(composition.getUuids()));
            return v;
        }).collect(MiscUtils.toVector());
    }

    /**
     * Group by artist the given list of compositions.
     *
     * @param compoList {@code List<Composition>} a compositions list
     * @return
     *         <ul>
     *         {@code Map<String, List<Composition>>}
     *         <li>{@code key}: the artist</li>
     *         <li>{@code value}: a list of compositions of this artist</li>
     *         </ul>
     */
    public static Map<String, List<Composition>> groupCompositionByArtist(List<Composition> compoList) {
        LOG.debug("Start groupCompositionByArtist");
        Map<String, List<Composition>> result = new HashMap<>();
        JaroWinklerDistance jaro = new JaroWinklerDistance();
        for (Composition c : compoList) {
            if (Thread.currentThread().isInterrupted()) {
                LOG.debug("Thread interrupted, End convertCompositionListToArtistVector");
                return result;
            }
            Optional<String> foundArtist = findArtistKey(result, jaro, c.getArtist());
            if (foundArtist.isPresent()) {
                // If the artist already exist in the map result
                List<Composition> list = result.get(foundArtist.get());
                list.add(c);
                result.put(foundArtist.get(), list);
            } else {
                // New entry
                result.put(c.getArtist(), new ArrayList<>(Arrays.asList(c)));
            }
        }
        LOG.debug("End groupCompositionByArtist");
        return result;
    }

    /**
     * For a {@code Map<String, List<Composition>>} with the keys consisting of artist, finds the first similar key inside the keySet.
     *
     * @param map the map to search into its keySet.
     * @param jaro a instance of {@link JaroWinklerDistance}
     * @param artist the artist to search
     * @return an Optional of the key found
     */
    public static Optional<String> findArtistKey(Map<String, List<Composition>> map, JaroWinklerDistance jaro,
            String artist) {
        return map.keySet().stream().parallel()
                .filter(key -> artistJaroEquals(key, artist, jaro, Constant.SCORE_LIMIT_ARTIST_FUSION) != null)
                .findFirst();
    }

    /**
     * Converts a map to Vector, counting the number of occurences for each artist by total, album and song, and with a score for this artist.
     *
     * @param map {@code Map<String, List<Composition>>} with key an artist and value its compositions
     * @param lineNumber if true add a column for line number
     * @return
     *         <ul>
     *         {@code Vector<Vector<Object>>} with :
     *         <li>1st column the artist</li>
     *         <li>2nd total occurences</li>
     *         <li>3td total occurences by album</li>
     *         <li>4th total occurences by song</li>
     *         <li>5th sum of the score of each compositions</li>
     *         </ul>
     */
    public static Vector<Vector<Object>> convertArtistPanelResultToVector(Map<String, List<Composition>> map,
            boolean lineNumber) {
        LOG.debug("convertArtistPanelResultToVector");
        return map.entrySet().parallelStream().map(e -> {
            Vector<Object> v = new Vector<>();
            if (lineNumber) {
                v.addElement(0);
            }
            // Artist
            v.addElement(e.getKey());
            // Occurence total
            int sum = e.getValue().stream().mapToInt(c -> c.getFiles().size()).sum();
            v.addElement(sum);
            // Occurence by album
            v.addElement(e.getValue().stream().filter(c -> c.getRecordType() == RecordType.ALBUM)
                    .mapToInt(c -> c.getFiles().size()).sum());
            // Occurence by song
            v.addElement(e.getValue().stream().filter(c -> c.getRecordType() == RecordType.SONG)
                    .mapToInt(c -> c.getFiles().size()).sum());
            // Percentage of deleted
            v.addElement(Math.round(100 * Double.valueOf(
                    e.getValue().stream().filter(Composition::isDeleted).mapToInt(c -> c.getFiles().size()).sum())
                    / Double.valueOf(sum)) + " %");
            // Score total
            long sumScore = e.getValue().stream()
                    .map(c -> ScoreUtils.getCompositionScore(
                            OngletPanel.getScore().getLogMax(c.getRecordType()),
                            OngletPanel.getScore().getDoubleMedian(c.getRecordType()), c))
                    .mapToLong(x -> x).sum();
            v.addElement(sumScore);
            // Score by Album
            v.addElement(e.getValue().stream().filter(c -> c.getRecordType() == RecordType.ALBUM)
                    .map(c -> ScoreUtils.getCompositionScore(
                            OngletPanel.getScore().getLogMax(c.getRecordType()),
                            OngletPanel.getScore().getDoubleMedian(c.getRecordType()), c))
                    .mapToLong(x -> x).sum());
            // Score by song
            v.addElement(e.getValue().stream().filter(c -> c.getRecordType() == RecordType.SONG)
                    .map(c -> ScoreUtils.getCompositionScore(
                            OngletPanel.getScore().getLogMax(c.getRecordType()),
                            OngletPanel.getScore().getDoubleMedian(c.getRecordType()), c))
                    .mapToLong(x -> x).sum());
            // Score deleted
            v.addElement(Math.round(100 * Double.valueOf(e.getValue().stream().filter(Composition::isDeleted)
                    .map(c -> ScoreUtils.getCompositionScore(
                            OngletPanel.getScore().getLogMax(c.getRecordType()),
                            OngletPanel.getScore().getDoubleMedian(c.getRecordType()), c))
                    .mapToLong(x -> x).sum()) / Double.valueOf(sumScore)) + " %");
            return v;
        }).collect(Collector.of(Vector<Vector<Object>>::new, (result, newElement) -> result.addElement(newElement),
                (result1, result2) -> {
                    result1.addAll(result2);
                    return result1;
                }, Collector.Characteristics.CONCURRENT));
    }

    /**
     * Find a composition with given uuids.
     *
     * @param compoList {@link List<Composition>} a composition list
     * @param uuids {@link List<String>} a list of uuids
     * @return a composition with all the uuids
     */
    public static Optional<Composition> findByUuid(List<Composition> compoList, List<String> uuids) {
        return compoList.stream().filter(c -> c.getUuids().stream().anyMatch(uuids::contains)).findFirst();
    }

    /**
     * Filter compositions list having the given uuids.
     *
     * @param compoList {@link List<Composition>} a composition list to filter
     * @param uuids {@link List<String>} a list of uuids to find
     * @return a list without the compositions having the uuids
     */
    public static List<Composition> filterByUuid(List<Composition> compoList, List<String> uuids) {
        return compoList.stream().filter(c -> c.getUuids().stream().noneMatch(uuids::contains)).collect(Collectors.toList());
    }

    /**
     * Delete in Xml files the given composition.
     *
     * @param toRemove la {@link Composition} à supprimer des fichiers
     * @throws MajorException if the deletion of the composition failed in the xml files or final file
     */
    public static void removeCompositionInFiles(Composition toRemove) throws MajorException {
        LOG.debug("Start removeCompositionInFiles");
        for (Fichier file : toRemove.getFiles()) {
            // Récupération des compositions du fichier XML
            String fileName = file.getFileName();
            List<Composition> importXML = ImportXML.importXML(FilesUtils.buildXmlFilePath(fileName)
                    .orElseThrow(() -> new MajorException("Can't rebuild xml file path: " + fileName)));
            if (importXML.isEmpty()) {
                LOG.error("Empty file ! {}", fileName);
                continue;
            }
            // Suppresion de la liste de la composition à enlever
            Optional<Composition> compoFromFile = CompositionUtils.findByUuid(importXML, toRemove.getUuids());
            if (compoFromFile.isPresent()) {
                compoFromFile.get().setDeleted(true);
            } else {
                LOG.error("{}{}", fileName, Constant.NEW_LINE);
                throw new MajorException("Can't find composition to remove: " + toRemove.getArtist() + " " + toRemove.getTitre() + " "
                        + toRemove.getRecordType() + " in file: " + fileName);
            }
            try {
                // Sauvegarde des modifications
                ExportXML.exportXML(importXML, fileName);
            } catch (MajorException e) {
                throw new MajorException("Error when exporting file: " + fileName, e);
            }
        }
        LOG.debug("End removeCompositionInFiles");
    }

    /**
     * Modifie dans les fichiers XML, la composition donnée.
     *
     * @param edited la {@link Composition} à modifier dans les fichiers
     * @param isDeleted {@code boolean} si la composition est supprimée
     * @throws MajorException if the composition can't be found in the given file
     */
    public static void editCompositionsInFiles(Composition edited, boolean isDeleted) throws MajorException {
        LOG.debug("Start editCompositionsInFiles");
        for (Fichier file : edited.getFiles()) {
            // Récupération des compositions du fichier XML
            String fileName = file.getFileName();
            String path = FilesUtils.buildXmlFilePath(fileName)
                    .orElseThrow(() -> new MajorException("File: " + fileName + " doesn't exist"));
            List<Composition> importXML = ImportXML.importXML(path);
            if (importXML.isEmpty()) {
                throw new MajorException("Empty file: " + path);
            }
            // Modificaton de la liste de la composition à enlever
            Optional<Composition> toEditFromFile = CompositionUtils.findByUuid(importXML, edited.getUuids());
            if (toEditFromFile.isPresent()) {
                toEditFromFile.get().setArtist(edited.getArtist());
                toEditFromFile.get().setTitre(edited.getTitre());
                toEditFromFile.get().setRecordType(edited.getRecordType());
                toEditFromFile.get().setDeleted(isDeleted);
                try {
                    // Sauvegarde des modifications
                    ExportXML.exportXML(importXML, fileName);
                } catch (MajorException e) {
                    throw new MajorException(
                            "Erreur lors de la modification d'une composition dans le fichier: " + fileName,
                            e);
                }
            } else {
                LOG.error("{}{}", path, Constant.NEW_LINE);
                throw new MajorException("Impossible de trouver la composition à modifier: " + edited.getArtist() + " " + file
                        + " " + edited.getTitre() + " " + edited.getRecordType());
            }
        }
        LOG.debug("End editCompositionsInFiles");
    }

    /**
     * Copy all infos of a composition into another.
     *
     * @param destination where the informations will go
     * @param source from where the informations are
     */
    public static void copy(Composition source, Composition destination) {
        destination.setArtist(source.getArtist());
        destination.setFiles(source.getFiles().stream().map(Fichier::new).collect(Collectors.toList()));
        destination.setTitre(source.getTitre());
        destination.setRecordType(source.getRecordType());
        destination.setCanBeMerged(source.isCanBeMerged());
        destination.setDeleted(source.isDeleted());
        destination.setUuids(source.getUuids().stream().map(String::new).collect(Collectors.toList()));
    }

    /**
     * Sort a list of composition by the rank of its file. <b>There must be only one file !</b>
     *
     * @param list to sort
     * @return sorted
     */
    public static List<Composition> sortByRank(List<Composition> list) {
        return list.stream().sorted((c1, c2) -> c1.getFiles().get(0).getClassement().compareTo(c2.getFiles().get(0).getClassement())).collect(Collectors.toList());
    }
}
