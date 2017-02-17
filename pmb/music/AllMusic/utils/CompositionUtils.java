package pmb.music.AllMusic.utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.model.Composition;

public class CompositionUtils {

    public static void listFilesForFolder(final File folder, List<File> files, String extension, boolean recursive) {
        for (final File fileEntry : folder.listFiles()) {
            if (recursive && fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, files, extension, recursive);
            } else if (StringUtils.endsWith(fileEntry.getName(), extension)) {
                files.add(fileEntry);
            }
        }
    }

    public static void printCompoList(List<Composition> allCompo) {
        System.out.println("Compo: " + allCompo.size());
        for (Composition composition : allCompo) {
            System.out.println(composition);
            // List<Fichier> files3 = composition.getFiles();
            // for (Fichier fichier : files3) {
            // System.out.println(fichier);
            // }
        }
    }

    public static Composition compoExist(List<Composition> compos, Composition c) {
        Composition res = null;
        for (Composition composition : compos) {
            if (c.getRecordType().equals(composition.getRecordType()) && c.getArtist().equalsIgnoreCase(composition.getArtist())
                    && c.getOeuvre().equalsIgnoreCase(composition.getOeuvre())) {
                res = composition;
                break;
            }
        }
        return res;
    }

    public static Object[][] convertListForJTable(List<Composition> compoList) {
        Object[][] result = new String[compoList.size()][5];
        for (int i = 0; i < compoList.size(); i++) {
            Composition composition = compoList.get(i);
            result[i][0] = composition.getArtist();
            result[i][1] = composition.getOeuvre();
            result[i][2] = composition.getRecordType().toString();
            result[i][3] = String.valueOf(composition.getFiles().size());
            result[i][4] = new Boolean(false).toString();
        }
        return result;
    }
    
    public static Composition findByArtistOeuvreAndType(List<Composition> compoList, String artist, String oeuvre, String type) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put("artist", artist);
        criteria.put("oeuvre", oeuvre);
        criteria.put("type", type);

        List<Composition> search = SearchUtils.search(compoList, criteria);
        if(!search.isEmpty()) {
            return search.get(0);
        } else {
            return new Composition();
        }
    }
}
