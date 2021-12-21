package pmb.allmusic.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.allmusic.utils.Constant;
import pmb.allmusic.utils.FilesUtils;
import pmb.allmusic.view.ActionPanel;
import pmb.allmusic.view.ComponentBuilder;
import pmb.allmusic.view.PanelUtils;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.MyConstant;

/** Panel hosting batch related panel. Providing a panel, a text area and file result handling. */
public class BatchPanel extends JPanel implements ActionPanel {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(BatchPanel.class);

  /** Les messages. */
  private JTextArea resultLabel;

  private List<String> resultLabelData;
  private String fileResult = Constant.BATCH_FILE;
  private JPanel root;

  /** Open the result file in notepad. */
  private JButton batchFileBtn;

  /**
   * Constructor of {@link BatchPanel}.
   *
   * @param rowSize how many rows there will be in the batch panel
   */
  public BatchPanel(int rowSize) {
    super();
    LOG.debug("Start BatchPanel");
    this.setLayout(new BorderLayout());
    root = new JPanel();
    this.add(new JScrollPane(root), BorderLayout.CENTER);
    root.setLayout(new GridLayout(rowSize + 1, 1));
    resultLine();
    LOG.debug("End BatchPanel");
  }

  protected static CompletableFuture<JFileChooser> buildFileChooser(
      JPanel parent, String label, JLabel selectedFile, FileFilter fileFilter) {
    LOG.debug("Start buildFileChooser");
    JPanel choose = PanelUtils.createBoxLayoutPanel(BoxLayout.Y_AXIS);
    JButton browse = ComponentBuilder.buildJButton("Parcourir", 220, Constant.ICON_FOLDER);
    browse.setToolTipText(label);
    CompletableFuture<JFileChooser> futureFileChooser =
        CompletableFuture.supplyAsync(
            () -> {
              JFileChooser jfile = new JFileChooser(Constant.getResourcesDir());
              jfile.setApproveButtonText("Ouvrir");
              jfile.setPreferredSize(new Dimension(1200, 600));
              jfile.addChoosableFileFilter(fileFilter);
              jfile.setFileFilter(fileFilter);
              browse.addActionListener(
                  (ActionEvent arg0) -> {
                    LOG.debug("Start browse");
                    Optional.ofNullable(jfile.getActionMap().get("viewTypeDetails"))
                        .ifPresent(
                            a ->
                                a.actionPerformed(
                                    new ActionEvent(
                                        arg0.getSource(), arg0.getID(), "viewTypeDetails")));
                    if (jfile.showOpenDialog(new JDialog()) == JFileChooser.APPROVE_OPTION) {
                      selectedFile.setText(jfile.getSelectedFile().getAbsolutePath());
                    }
                  });
              PanelUtils.addComponent(choose, browse, Component.LEFT_ALIGNMENT, 0);
              PanelUtils.addComponent(choose, selectedFile, Component.LEFT_ALIGNMENT, 0);
              PanelUtils.addComponent(parent, choose, Component.LEFT_ALIGNMENT, 0);
              return jfile;
            });
    LOG.debug("End buildFileChooser");
    return futureFileChooser;
  }

  /** Initialise la dernière ligne de composant. */
  private void resultLine() {
    JPanel lastLine = new JPanel(new GridLayout(0, 2));

    // result
    JPanel resultPanel = new JPanel(new BorderLayout());
    resultLabel = new JTextArea();
    resultLabel.setWrapStyleWord(true);
    resultLabel.setLineWrap(true);
    resultLabel.setOpaque(false);
    resultLabel.setEditable(false);
    resultLabel.setBackground(UIManager.getColor("Label.background"));
    resultLabel.setFont(UIManager.getFont("Label.font"));
    resultLabel.setBorder(UIManager.getBorder("Label.border"));
    resultPanel.add(new JScrollPane(resultLabel), BorderLayout.CENTER);
    lastLine.add(resultPanel);

    // Boutons
    JPanel btnPanel = new JPanel();
    // Clear
    JButton clearBtn =
        ComponentBuilder.buildJButton("Vider la zone de résultat", 200, Constant.ICON_ERASE);
    clearBtn.addActionListener(
        (ActionEvent arg0) -> {
          resultLabelData = null;
          displayText(null, false);
        });
    btnPanel.add(clearBtn);
    // Notepad
    batchFileBtn =
        ComponentBuilder.buildJButton("Ouvrir le fichier de résultat", 200, Constant.ICON_TXT_FILE);
    batchFileBtn.addActionListener((ActionEvent arg0) -> openResultFile());
    btnPanel.add(batchFileBtn);

    lastLine.add(btnPanel);
    root.add(lastLine);
  }

  /** Ouvrir le fichier de resultat dans notepad. */
  private void openResultFile() {
    LOG.debug("Start openResultFileInNotepad");
    try {
      if (StringUtils.endsWith(fileResult, MyConstant.CSV_EXTENSION)) {
        FilesUtils.openFileInExcel(fileResult);
      } else {
        FilesUtils.openFileInNotepad(Optional.ofNullable(fileResult).orElse(null), null);
      }
    } catch (MajorException e) {
      displayText(e.toString(), false);
      LOG.error("Erreur lors de l'ouverture du fichier: {}", fileResult, e);
    }
    LOG.debug("End openResultFileInNotepad");
  }

  /**
   * Ajoute un message dans la zone de texte resultLabel.
   *
   * @param text un nouveau texte à afficher
   * @param replaceLast true replace previous line, false append it
   */
  public void displayText(String text, boolean replaceLast) {
    if (resultLabelData == null) {
      resultLabelData = new ArrayList<>();
    }
    if (!replaceLast) {
      resultLabelData.add(text);
    } else {
      resultLabelData.set(resultLabelData.size() - 1, text);
    }
    resultLabel.setText(StringUtils.join(resultLabelData, MyConstant.NEW_LINE));
    resultLabel.setForeground(new Color(243, 16, 16));
    Font labelFont = resultLabel.getFont();
    resultLabel.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 20));
  }

  public void setFileResult(String fileResult) {
    this.fileResult = fileResult;
  }

  public JPanel getRoot() {
    return root;
  }

  @Override
  public JButton getActionButton() {
    return batchFileBtn;
  }
}
