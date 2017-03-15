package pmb.music.AllMusic.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pmb.music.AllMusic.utils.Constant;

public class CleanFile {

    public static File clearFile(File file, boolean isSorted, String sep, String characterToRemove) throws IOException {
        System.out.println("Start clearFile");
        List<String> sepAsList = new LinkedList<String>(Arrays.asList(Constant.SEPARATORS));
        if (StringUtils.isNotBlank(sep)) {
            sepAsList.add(sep);
        }
        BufferedReader br = null;
        String line = "";
        BufferedWriter writer = null;
        String exitFile = file.getParentFile().getAbsolutePath() + "\\" + StringUtils.substringBeforeLast(file.getName(),".") + " - Cleaned." + StringUtils.substringAfterLast(file.getName(),".");

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.ANSI_ENCODING));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exitFile), Constant.ANSI_ENCODING));
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
    
    public static void main(String[] args) {
        System.out.println("Start clearFile");
        File file = new File(Constant.FINAL_FILE_PATH);
        List<String> sepAsList = new LinkedList<String>(Arrays.asList(Constant.SEPARATORS));
        BufferedReader br = null;
        String line = "";
        BufferedWriter writer = null;
        String exitFile = file.getParentFile().getAbsolutePath() + "\\" + StringUtils.substringBeforeLast(file.getName(),".") + " - Cleaned." + StringUtils.substringAfterLast(file.getName(),".");

        try {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.UTF8_ENCODING));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exitFile), Constant.UTF8_ENCODING));
            } catch (UnsupportedEncodingException | FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                while ((line = br.readLine()) != null) {
                    writer.append(Normalizer.normalize(line, Form.NFKD)
                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    }

}
