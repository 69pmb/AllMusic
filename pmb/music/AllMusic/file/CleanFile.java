package pmb.music.AllMusic.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.App;

public class CleanFile {

    public static final String REGULAR_DASH = "-";
    public static final String DOT = ". ";
    public static final String[] SEPARATORS = { "-", "-", "‒", "–", "—", "―", "-", " - ", " - ", " – "," — ", " - " };

    public static File clearFile(File file, boolean isSorted, String sep, String characterToRemove) throws IOException {
        System.out.println("Start clearFile");
        List<String> sepAsList = new LinkedList<String>(Arrays.asList(SEPARATORS));
        if(StringUtils.isNotBlank(sep)){
            sepAsList.add(sep);
        }
        BufferedReader br = null;
        String line = "";
        FileWriter writer = null;
        String exitFile = file.getParentFile().getAbsolutePath()+"\\"+"Cleaned - " + file.getName();

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), App.ANSI_ENCODING));
            writer = new FileWriter(exitFile);
            while ((line = br.readLine()) != null) {
                boolean isDigit = true;
                if (isSorted) {
                    isDigit = isBeginByDigit(line);
                }
                if (isDigit) {
                    for (String separator : sepAsList) {
                        if (StringUtils.containsIgnoreCase(line, separator)) {
                            if (StringUtils.isNotBlank(characterToRemove)) {
                                line = StringUtils.substringAfter(line, characterToRemove);
                            }
                            writer.append(line).append("\n");
                            break;
                        }
                    }
                }
            }
            writer.flush();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("End clearFile");
        return new File(exitFile);
    }

    private static boolean isBeginByDigit(String line) {
        boolean isDigit = true;
        String digitToTest = StringUtils.substring(line, 0, 1);
        try {
            Integer.parseInt(digitToTest);
        } catch (NumberFormatException e) {
            isDigit = false;
        }
        return isDigit;
    }
}
