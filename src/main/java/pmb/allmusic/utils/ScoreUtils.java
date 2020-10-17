package pmb.allmusic.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import pmb.allmusic.model.Cat;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.model.Score;
import pmb.allmusic.view.panel.OngletPanel;
import pmb.my.starter.utils.VariousUtils;

/**
 * Utility class to handle score related process.
 *
 */
public final class ScoreUtils {

    private ScoreUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Finds decile for given score and type.
     *
     * @param type the record type
     * @param calculatedScore the score
     * @return the decile (between 1 and 10)
     */
    public static int getDecile(RecordType type, long calculatedScore) {
        BigDecimal scoreBD = BigDecimal.valueOf(calculatedScore);
        List<Double> decileLimit = OngletPanel.getScore().getDecileLimit(type);
        int decile = 0;
        for (int j = 0; j < decileLimit.size(); j++) {
            if (scoreBD.compareTo(BigDecimal.valueOf(decileLimit.get(j))) <= 0) {
                decile = j + 1;
                break;
            }
        }
        return decile;
    }

    /**
     * Calculates the {@link Score} decile limit for the given type.
     *
     * @param list {@code List<Composition>}
     * @return {@link List<Integer>}
     */
    public static List<Double> getDecileLimit(List<Composition> list) {
        Map<Composition, Long> collectedMap = list.stream()
                .collect(Collectors.toMap(Function.identity(), ScoreUtils::getCompositionScore));
        double[] prob = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
        int size = prob.length;
        Percentile[] q = new Percentile[size];
        for (int i = 0; i < size; i++) {
            q[i] = new Percentile();
        }
        double[] value = new double[size];
        for (int i = 0; i < size; i++) {
            value[i] = q[i].evaluate(collectedMap.values().stream().mapToDouble(Long::doubleValue).toArray(), prob[i]);
        }
        return Arrays.asList(ArrayUtils.toObject(value));
    }

    /**
     * <ul>
     * Calculates the score for a file.
     * <li>ALL_TIME: score * 2</li>
     * <li>LONG_PERIOD: score * 1.5</li>
     * <li>DECADE: score * 1.3</li>
     * <li>YEAR: score / 1.5</li>
     * <li>THEME: score / 1.3</li>
     * <li>GENRE: score * 1</li>
     * <li>MISCELLANEOUS: score * 1</li>
     * </ul>
     *
     * @param logMax @see {@link Score#getLogMax(RecordType)}
     * @param doubleMedian @see {@link Score#getDoubleMedian(RecordType)}
     * @param fichier the file
     * @return {@link BigDecimal} the score
     */
    private static BigDecimal getFileScore(BigDecimal logMax, BigDecimal doubleMedian, Fichier fichier) {
        BigDecimal points;
        if (BooleanUtils.isTrue(fichier.getSorted()) && fichier.getClassement() != 0) {
            // Log10(doubleMedian/rank + 3) * logMax
            points = BigDecimal.valueOf(Math
                    .log10(doubleMedian.divide(BigDecimal.valueOf(fichier.getClassement()), 10, RoundingMode.HALF_UP)
                            .add(BigDecimal.valueOf(3)).doubleValue()))
                    .multiply(logMax);
        } else {
            // Log10(5) * logMax
            points = BigDecimal.valueOf(Math.log10(5)).multiply(logMax);
        }
        if (fichier.getCategorie() == Cat.ALL_TIME) {
            points = points.multiply(BigDecimal.valueOf(2));
        } else if (fichier.getCategorie() == Cat.LONG_PERIOD) {
            points = points.multiply(BigDecimal.valueOf(1.5));
        } else if (fichier.getCategorie() == Cat.DECADE) {
            points = points.multiply(BigDecimal.valueOf(1.3));
        } else if (fichier.getCategorie() == Cat.YEAR) {
            points = points.divide(BigDecimal.valueOf(1.5), RoundingMode.HALF_UP);
        } else if (fichier.getCategorie() == Cat.THEME) {
            points = points.divide(BigDecimal.valueOf(1.3), RoundingMode.HALF_UP);
        }
        return points;
    }

    /**
     * Calculates the score for a composition, it's depending on the
     * {@link RecordType} of the composition. It's the sum of the score of its
     * files.
     *
     * @param c the composition
     * @return the score
     * @see {@link Score#getLogMax(RecordType)}
     * @see {@link Score#getDoubleMedian(RecordType)}
     */
    public static long getCompositionScore(Composition c) {
        BigDecimal sumPts = BigDecimal.ZERO;
        BigDecimal logMax = OngletPanel.getScore().getLogMax(c.getRecordType());
        BigDecimal doubleMedian = OngletPanel.getScore().getDoubleMedian(c.getRecordType());
        for (Fichier fichier : c.getFiles()) {
            sumPts = sumPts.add(getFileScore(logMax, doubleMedian, fichier));
        }
        return Math.round(sumPts.doubleValue());
    }

    /**
     * Calculates the {@link Score} logMax for the given type. Max is the biggest
     * size of all files. LogMax is {@code Log10(max) * max}.
     *
     * @param list {@code List<Composition>}
     *
     * @return {@link BigDecimal}
     */
    public static BigDecimal getLogMax(List<Composition> list) {
        // Maximum ranking of given files
        List<Integer> rankList = list.parallelStream().map(Composition::getFiles).flatMap(List::stream)
                .map(Fichier::getClassement).collect(Collectors.toList());
        BigDecimal max = new BigDecimal(rankList.parallelStream().mapToInt(Integer::intValue).max().orElse(1));
        // log10(max)*max
        return BigDecimal.valueOf(Math.log10(max.doubleValue())).multiply(max);
    }

    /**
     * Calculates the {@link Score} doubleMedian. The median of all files ranking multiplied
     * by 2.
     *
     * @param list {@code List<Composition>}
     * @return {@link BigDecimal}
     */
    public static BigDecimal getDoubleMedianRanking(List<Composition> list) {
        return BigDecimal.valueOf(VariousUtils.median(list.parallelStream().map(Composition::getFiles).flatMap(List::stream)
                .map(f -> BigDecimal.valueOf(f.getClassement())).collect(Collectors.toList()))).multiply(BigDecimal.valueOf(2));
    }
}
