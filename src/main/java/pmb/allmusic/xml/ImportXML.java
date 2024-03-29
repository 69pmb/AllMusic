/** */
package pmb.allmusic.xml;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.JTextArea;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import pmb.allmusic.model.Composition;
import pmb.allmusic.model.Fichier;
import pmb.allmusic.model.RecordType;
import pmb.allmusic.utils.CompositionUtils;
import pmb.allmusic.utils.Constant;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;

/**
 * Classe pour manipuler les fichiers XML.
 *
 * @see #importXML(String)
 * @see #fusionFiles(String, boolean)
 */
public final class ImportXML {

  private static final Logger LOG = LogManager.getLogger(ImportXML.class);

  private ImportXML() {}

  /**
   * Import le fichier donné et extrait les {@link Composition}.
   *
   * @param filePath {@link String} le chemin absolu du fichier
   * @return les compos extraites
   */
  public static List<Composition> importXML(String filePath) {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parseur = null;
    try {
      parseur = factory.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      LOG.error("Erreur lors de la création du parseur", e);
    }

    File fichier = new File(filePath);
    CompoHandler handler = new CompoHandler();
    if (fichier.length() > 0 && parseur != null) {
      try {
        parseur.parse(fichier, handler);
      } catch (SAXException | IOException e) {
        LOG.error("Erreur lors de la lecture du fichier", e);
        LOG.error(filePath);
      } catch (Exception e2) {
        LOG.error("Runtime error when readind file", e2);
        LOG.error(filePath);
        throw e2;
      }
    }
    return handler.getCompoList();
  }

  /**
   * Fusionne tous les fichiers à l'endroit indiqué dans {@code final.xml}. Export le fichier final
   * et renvoie la liste de Composition.
   *
   * @param dirName le dossier où se situe les fichiers
   * @param resultLabel {@link JTextArea} la zone de texte pour afficher
   * @return la liste des {@link Composition} des fichiers
   * @throws MajorException if error when exporting final file
   */
  public static List<Composition> fusionFiles(String dirName, final JTextArea resultLabel)
      throws MajorException {
    LOG.debug("Start fusionFiles");
    double startTime = System.currentTimeMillis();
    File dir = new File(dirName);
    // On récupère tous les fichiers xml du dossier XML
    List<Composition> compoFusionSong =
        new ArrayList<>(); // Contiendra toutes les compositions de chanson
    List<Composition> compoFusionAlbum =
        new ArrayList<>(); // Contiendra toutes les compositions d'album
    String finalFile = Constant.getFinalFile();
    MyFileUtils.listFilesInFolder(dir, MyConstant.XML_EXTENSION, false)
        .forEach(
            fileXML -> {
              // On récupère les compositions de chaque fichier xml, excepté le fichier
              // final.xml
              if (!finalFile.equalsIgnoreCase(fileXML.getName())) {
                List<Composition> importXML = ImportXML.importXML(fileXML.getAbsolutePath());
                if (RecordType.SONG == importXML.get(0).getRecordType()) {
                  compoFusionSong.addAll(importXML);
                } else {
                  compoFusionAlbum.addAll(importXML);
                }
              }
            });
    List<Composition> compoFinal = new ArrayList<>();
    BigDecimal sizeBG = new BigDecimal(compoFusionAlbum.size() + compoFusionSong.size());
    int modulo = Math.round(sizeBG.divide(BigDecimal.valueOf(10000D)).floatValue());
    LOG.debug(modulo);
    LOG.debug(sizeBG.intValue());
    fusion(resultLabel, compoFusionAlbum, compoFinal, 0, modulo, sizeBG);
    fusion(resultLabel, compoFusionSong, compoFinal, compoFusionAlbum.size(), modulo, sizeBG);
    ExportXML.exportXML(compoFinal, finalFile); // On exporte le resultat dans le fichier final.xml
    double endTime = System.currentTimeMillis();
    LOG.debug("Time: {} secondes", (endTime - startTime) / 1000);
    LOG.debug(
        "Time: {} minutes et {} secondes",
        Math.round((endTime - startTime) / 60000),
        Math.round(((endTime - startTime) / 1000) % 60));
    LOG.debug("End fusionFiles");
    return compoFinal;
  }

  private static void fusion(
      final JTextArea resultLabel,
      List<Composition> compoFusion,
      List<Composition> compoFinal,
      int init,
      int modulo,
      BigDecimal sizeBG) {
    final AtomicInteger count = new AtomicInteger(init);
    compoFusion.parallelStream()
        .forEach(
            compo -> {
              findAndMergeComposition(compoFinal, compo, true);
              if (count.incrementAndGet() % modulo == 0) {
                // Affiche dans l'ihm le pourcentage du calcul de fusion
                updateResultLabel(
                    Arrays.asList(
                        "Fusion à "
                            + BigDecimal.valueOf(100D)
                                .setScale(2)
                                .multiply(new BigDecimal(count.get()))
                                .divide(sizeBG, RoundingMode.HALF_UP)
                                .doubleValue()
                            + "%"),
                    resultLabel);
              }
            });
  }

  /**
   * Search if the given composition exists in the given list. Then merge into it: simply add it if
   * doesn't exist, merge it if already exist
   *
   * @param list a list
   * @param compo a composition
   * @param addIfNotExist if true, add given composition to list if not found
   * @return {@code Optional.empty()} if not exist, the merged composition otherwise
   */
  public static Optional<Composition> findAndMergeComposition(
      List<Composition> list, Composition compo, boolean addIfNotExist) {
    Composition compoExist = CompositionUtils.compoExist(new ArrayList<>(list), compo);
    if (compoExist == null && addIfNotExist) {
      list.add(compo);
    } else if (compoExist != null) {
      compoExist.getFiles().addAll(compo.getFiles());
      compoExist.setDeleted(compoExist.isDeleted() || compo.isDeleted());
      compoExist.getUuids().addAll(compo.getUuids());
    }
    return Optional.ofNullable(compoExist);
  }

  private static void updateResultLabel(List<String> result2, final JTextArea resultLabel) {
    StringBuilder s = new StringBuilder();
    for (String string : result2) {
      s.append(string).append(MyConstant.NEW_LINE);
    }
    resultLabel.setText(s.toString());
    resultLabel.setForeground(new Color(243, 16, 16));
    Font labelFont = resultLabel.getFont();
    resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
  }

  /**
   * Verifies that all deleted compositions from final file are also deleted in xml files.
   *
   * @throws MinorException if an export of a xml file goes wrong
   */
  public static void synchroDeletedWithFinal() {
    LOG.debug("Start synchroDeletedWithFinal");
    List<Composition> allDeletedComposition =
        ImportXML.importXML(Constant.getFinalFilePath()).stream()
            .filter(Composition::isDeleted)
            .collect(Collectors.toList());
    for (Composition composition : allDeletedComposition) {
      for (Fichier fichier : composition.getFiles()) {
        List<Composition> xml =
            ImportXML.importXML(
                Constant.getXmlPath() + fichier.getFileName() + MyConstant.XML_EXTENSION);
        CompositionUtils.findByUuid(xml, composition.getUuids())
            .filter(c -> !c.isDeleted())
            .ifPresent(
                found -> {
                  LOG.debug(
                      "Composition not deleted: {} - {}",
                      composition.getArtist(),
                      composition.getTitre());
                  found.setDeleted(true);
                  try {
                    ExportXML.exportXML(xml, fichier.getFileName());
                  } catch (MajorException e) {
                    throw new MinorException(
                        "Erreur lors de l'export du fichier: " + fichier.getFileName(), e);
                  }
                });
      }
    }
    LOG.debug("End synchroDeletedWithFinal");
  }
}
