package pmb.music.AllMusic;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import pmb.music.AllMusic.file.ImportFile;
import pmb.music.AllMusic.model.Cat;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.SearchUtils;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) {
        List<File> files = new ArrayList<>();
        String pathname = App.MUSIC_DIRECTORY + "NME";
        CompositionUtils.listFilesForFolder(new File(pathname), files, ".txt", true);
        for (File file : files) {
            System.out.println(file.getName());
            Fichier fichier = ImportFile.convertOneFile(file);
            List<String> randomLine = ImportFile.randomLineAndLastLines(file);
            fichier.setSorted(ImportFile.isSorted(randomLine.get(0)));
            for (String line : randomLine) {
                System.out.println(line);
            }
//            System.out.println(randomLine);
//            System.out.println("Fichier: "+fichier);
//            System.out.println("Type: "+ImportFile.determineType(file.getName()));
//            System.out.println("Separateur: "+ImportFile.getSeparator(randomLine.get(0)));
//            System.out.println(
//                    "############################################################################################################################################################################################################################################################\n");
        }
    }

    @Test
    public void testSearch() {
        List<Fichier> files = new ArrayList<Fichier>();
        Fichier file1 = new Fichier("NME", "Best albums of the 60's", 2000, Cat.DECADE, 1960, 1969, false, 0, new Date(),50);
        Fichier file2 = new Fichier("Pitchfork", "The best albums of The Beatles", 2010, Cat.MISCELLANEOUS, 1960, 1970, true, 2, new Date(),10);
        files.add(file1);
        files.add(file2);
        Composition compo = new Composition("The Beatles", files, "Revolver", RecordType.ALBUM);

        List<Fichier> files2 = new ArrayList<Fichier>();
        Fichier file3 = new Fichier("NME", "Best albums of the 90's", 2000, Cat.DECADE, 1990, 1999, true, 3, new Date(),100);
        Fichier file4 = new Fichier("GQ", "The best of the Rock", 2010, Cat.GENRE, 0, 0, true, 10, new Date(),300);
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
        List<Composition> search = SearchUtils.search(compList, criteria);
        CompositionUtils.printCompoList(search);
    }
}
