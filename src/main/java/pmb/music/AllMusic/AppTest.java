package pmb.music.AllMusic;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) {
        List<File> files = new ArrayList<>();
        String pathname = Constant.MUSIC_ABS_DIRECTORY + "Pitchfork";
        CompositionUtils.listFilesForFolder(new File(pathname), files, ".txt", true);
        for (File file : files) {
            System.out.println(file.getName());
            Fichier fichier = ImportFile.convertOneFile(file);
            List<String> randomLine = ImportFile.randomLineAndLastLines(file);
            fichier.setSorted(ImportFile.isSorted(randomLine.get(3)));
            fichier.setSize(ImportFile.determineSize(fichier, randomLine, file.getAbsolutePath()));
            RecordType determineType = ImportFile.determineType(file.getName());
            if (Cat.MISCELLANEOUS.equals(fichier.getCategorie()) && !RecordType.UNKNOWN.equals(determineType) && fichier.getPublishYear() != 0 && fichier.getRangeDateBegin() == 0
                    && fichier.getRangeDateEnd() == 0) {
                fichier.setCategorie(Cat.YEAR);
                fichier.setRangeDateBegin(fichier.getPublishYear());
                fichier.setRangeDateEnd(fichier.getPublishYear());
            }
            System.out.println(randomLine);
            System.out.println("Fichier: " + fichier);
            System.out.println("Type: " + determineType);
            System.out.println("Separateur: " + ImportFile.getSeparator(randomLine.get(0)));
            System.out.println(
                    "############################################################################################################################################################################################################################################################\n");
        }
    }

    // @Test
    public void testSearch() {
        List<Fichier> files = new ArrayList<Fichier>();
        Fichier file1 = new Fichier("NME", "Best albums of the 60's", 2000, Cat.DECADE, 1960, 1969, false, 0, new Date(), 50);
        Fichier file2 = new Fichier("Pitchfork", "The best albums of The Beatles", 2010, Cat.MISCELLANEOUS, 1960, 1970, true, 2, new Date(), 10);
        files.add(file1);
        files.add(file2);
        Composition compo = new Composition("The Beatles", files, "Revolver", RecordType.ALBUM);

        List<Fichier> files2 = new ArrayList<Fichier>();
        Fichier file3 = new Fichier("NME", "Best albums of the 90's", 2000, Cat.DECADE, 1990, 1999, true, 3, new Date(), 100);
        Fichier file4 = new Fichier("GQ", "The best of the Rock", 2010, Cat.GENRE, 0, 0, true, 10, new Date(), 300);
        files2.add(file3);
        files2.add(file4);
        Composition compo2 = new Composition("Oasis", files2, "Definitively Maybe", RecordType.ALBUM);

        Composition compo3 = new Composition("Oasis", null, "What's The Story Morning Glory", RecordType.ALBUM);

        List<Composition> compList = new ArrayList<Composition>();
        compList.add(compo);
        compList.add(compo2);
        compList.add(compo3);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("auteur", "nme");
        List<Composition> search = SearchUtils.searchStrictly(compList, criteria);
        CompositionUtils.printCompoList(search);
    }

    @Test
    public void searchArtist() {
        System.out.println(StringUtils.containsIgnoreCase("Beatles", "beat"));
    }

    @Test
    public void detectUpper() {
        String test = "05. GERRY & THE PACEMAKERS Ferry Cross the Mersey";
        boolean sorted = true;
        char[] array = test.toCharArray();
        int cut = 0;
        for (int i = 0; i < array.length; i++) {
            if (Character.isUpperCase(array[i]) || !Character.isAlphabetic(array[i])) {
                System.out.println("upper: " + array[i]);
            } else {
                cut = i - 1;
                System.out.println("else: " + array[i]);
                break;
            }
        }
        String artist = StringUtils.substring(test, 0, cut);
        String titre = StringUtils.substring(test, cut);
        System.out.println("cut: " + cut);
        System.out.println("artist: " + artist);
        System.out.println("titre: " + titre);
        int rank = 0;
        if (sorted) {
            String res = StringUtils.substringBefore(artist, ".");
            if (StringUtils.isNumeric(res)) {
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfter(artist, ".");
            } else {
                res = artist.split(" ")[0];
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfterLast(artist, res);
            }
        } else {
            rank = 1;
        }
        System.out.println("rank: " + rank);
    }

    @Test
    public void detectQuote() {
        String test = "05. GERRY & THE PACEMAKERS Ferry Cross the Mersey";
        boolean sorted = true;
        char[] array = test.toCharArray();
        int cut = 0;
        for (int i = 0; i < array.length; i++) {
            if (Character.isUpperCase(array[i]) || !Character.isAlphabetic(array[i])) {
                System.out.println("upper: " + array[i]);
            } else {
                cut = i - 1;
                System.out.println("else: " + array[i]);
                break;
            }
        }
        String artist = StringUtils.substring(test, 0, cut);
        String titre = StringUtils.substring(test, cut);
        System.out.println("cut: " + cut);
        System.out.println("artist: " + artist);
        System.out.println("titre: " + titre);
        int rank = 0;
        if (sorted) {
            String res = StringUtils.substringBefore(artist, ".");
            if (StringUtils.isNumeric(res)) {
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfter(artist, ".");
            } else {
                res = artist.split(" ")[0];
                rank = Integer.parseInt(res);
                artist = StringUtils.substringAfterLast(artist, res);
            }
        } else {
            rank = 1;
        }
        System.out.println("rank: " + rank);
    }
}
