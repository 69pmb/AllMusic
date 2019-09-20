package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.music.AllMusic.XML.ExportXML;
import pmb.music.AllMusic.XML.ImportXML;
import pmb.music.AllMusic.model.Composition;
import pmb.music.AllMusic.model.Fichier;
import pmb.music.AllMusic.model.RecordType;
import pmb.music.AllMusic.utils.CompositionUtils;
import pmb.music.AllMusic.utils.Constant;
import pmb.music.AllMusic.utils.MiscUtils;
import pmb.music.AllMusic.utils.MyException;
import pmb.music.AllMusic.view.ColumnIndex.Index;
import pmb.music.AllMusic.view.dialog.ModifyCompositionDialog;
import pmb.music.AllMusic.view.model.AbstractModel;
import pmb.music.AllMusic.view.panel.OngletPanel;

/**
 * Utility class for application panels.
 *
 * @author PBR
 *
 */
public final class PanelUtils {

    private static final Logger LOG = LogManager.getLogger(PanelUtils.class);
    private static final BigDecimal widthBG = new BigDecimal(Toolkit.getDefaultToolkit().getScreenSize().width);
    private static final BigDecimal heightBG = new BigDecimal(Toolkit.getDefaultToolkit().getScreenSize().height);

    public static final FocusListener selectAll = new FocusListener() {
        @Override
        public void focusLost(FocusEvent e) {
            // Nothing to do
        }

        @Override
        public void focusGained(FocusEvent e) {
            JTextField source = (JTextField) e.getSource();
            source.selectAll();
        }
    };

    private PanelUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Dimensionne les colonnes du tableau et ajoute des couleurs aux lignes.
     *
     * @see {@link EvenOddRenderer}
     * @param table le tableau
     * @param lastColumn si la dernière colonne doit être colorisée ou non
     * @param deletedIndex index of the deleted column, use it to draw deleted row
     *            with specific color
     * @param typeIndex index of the record type column, use it to draw type cell
     *            with a specific color
     * @param catIndex index of the category column, use it to draw type cell with a
     *            specific color
     * @param decileIndex index of the decile column, used to add tooltip
     * @param scoreIndex index of the score column, used to add color scale of
     *            purple
     * @param sortedIndex index of the sorted column, used to add color if sorted
     * @param rankIndex index of the rank column, used to add color if sorted
     */
    public static void colRenderer(JTable table, boolean lastColumn, Integer deletedIndex, Integer typeIndex,
            Integer catIndex, Integer decileIndex, Integer scoreIndex, Integer sortedIndex, Integer rankIndex) {
        LOG.debug("Start colRenderer");
        Component topComponent = SwingUtilities.getWindowAncestor(table);
        setColumnsWidth(table, topComponent != null ? topComponent.getWidth() : table.getWidth(), "Init");

        DefaultTableCellRenderer renderer = new EvenOddRenderer(deletedIndex, typeIndex, catIndex, decileIndex,
                scoreIndex, sortedIndex, rankIndex);
        int columnCount = table.getColumnCount();
        if (!lastColumn) {
            columnCount--;
            if (deletedIndex != null) {
                columnCount--;
            }
        }
        for (int i = 0; i < columnCount; i++) {
            renderer.setHorizontalAlignment(JLabel.CENTER);
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        LOG.debug("End colRenderer");
    }

    /**
     * Fixe la largeur de chaques colonnes à la longueur maximum pour de chaques
     * colonnes.
     *
     * @param table le tableau
     * @param width la largeur du composant parent quand il est redimensionné
     * @param name name of the table
     */
    public static void setColumnsWidth(JTable table, int width, String name) {
        LOG.trace("Start setColumnsWidth: " + name);
        boolean isTableWiderThanScreen = table.getWidth() != 0 && table.getWidth() > (width + 10);
        TableColumnModel columnModel = table.getColumnModel();
        Map<Integer, Integer> colWidth = new HashMap<>();
        Double widthDouble = Double.valueOf(width);
        int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            int maximum = 0;
            Object currentValue = null;
            for (int j = 0; j < table.getRowCount(); j++) {
                currentValue = table.getValueAt(j, i);
                if (currentValue == null) {
                    continue;
                }
                int longueurCourante = currentValue.toString().length();
                if (longueurCourante > maximum) {
                    maximum = longueurCourante;
                }
            }
            Double columnWidth;
            if (currentValue != null) {
                FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
                int widthFactor = fontMetrics.stringWidth(new JLabel(currentValue.toString()).getText());
                columnModel.getColumn(i).setPreferredWidth(widthFactor + 2); // valeur arbitraire
                columnWidth = Double.valueOf(widthFactor + 2D);
            } else {
                columnModel.getColumn(i).setPreferredWidth(maximum * 7); // valeur arbitraire
                columnWidth = Double.valueOf(maximum) * 7D;
            }

            if (isTableWiderThanScreen) {
                // If table wider than given width, calculates a column width
                Double tableWidthDouble = Double.valueOf(table.getWidth());
                Double ratio = widthDouble / tableWidthDouble;
                // Calcule le ratio entre la largeur donnée et la largeur du tableau
                int round = (int) Math.round(columnWidth * ratio);
                colWidth.put(i, round);
            }
        }
        if (isTableWiderThanScreen) {
            LOG.trace("Table is wider than Screen");
            // Sum of the calculates columns width
            Integer sum = colWidth.values().stream().mapToInt(Integer::intValue).sum();
            Double sumDouble = Double.valueOf(sum);
            Double columnCountDouble = Double.valueOf(columnCount);
            if (sum.compareTo(width) > 0) {
                LOG.trace("Too large");
                // If the calculates column are too large
                Double offset = (sumDouble - widthDouble) / columnCountDouble;
                colWidth.entrySet().stream().forEach(e -> e.setValue(e.getValue() - (int) Math.round(offset)));
            } else {
                LOG.trace("Too thin");
                // If the calculates column are too thin
                Double offset = (widthDouble - sumDouble) / columnCountDouble;
                colWidth.entrySet().stream().forEach(e -> e.setValue(e.getValue() + (int) Math.round(offset)));
            }
            // Set of the columns max/min width for each column
            colWidth.entrySet().stream().forEach(e -> columnModel.getColumn(e.getKey()).setMaxWidth(e.getValue()));
            colWidth.entrySet().stream().forEach(e -> columnModel.getColumn(e.getKey()).setMinWidth(e.getValue()));
        }
        LOG.trace("End setColumnsWidth: " + name);
    }

    /**
     * Finds in the data table model the selected row.
     *
     * @param <T> the model of the table
     * @param target the table selected
     * @param point the spot clicked
     * @return the selected row
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractModel> Optional<Vector<String>> getSelectedRow(JTable target, Point point) {
        LOG.debug("Start getSelectedRow");
        int rowAtPoint = target.rowAtPoint(SwingUtilities.convertPoint(target, point, target));
        Vector<String> selectedRow = null;
        if (rowAtPoint > -1) {
            LOG.debug("Found selectedRow");
            target.setRowSelectionInterval(rowAtPoint, rowAtPoint);
            selectedRow = (Vector<String>) ((T) target.getModel()).getDataVector()
                    .get(target.getRowSorter().convertRowIndexToModel(rowAtPoint));
        }
        LOG.debug("End getSelectedRow");
        return Optional.ofNullable(selectedRow);
    }

    /**
     * Converts a table data vector to a list of list of string.
     *
     * @param table the table holding the data to convert
     * @return a {@code List<List<String>>} representing row then column
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractModel> List<List<String>> convertDataVectorToList(JTable table) {
        List<List<String>> result = new ArrayList<>();
        TableModel model = table.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            Vector<Object> selectedRow = (Vector<Object>) ((T) model).getDataVector()
                    .get(table.getRowSorter().convertRowIndexToModel(i));
            List<String> row = new ArrayList<>();
            for (int j = 0; j < model.getColumnCount(); j++) {
                if (model.getColumnClass(j) == Boolean.class) {
                    row.add(String.valueOf(selectedRow.get(j)));
                } else if (model.getColumnClass(j) == Integer.class || model.getColumnClass(j) == Double.class
                        || model.getColumnClass(j) == Long.class) {
                    row.add(String.valueOf(selectedRow.get(j)));
                } else if (model.getColumnClass(j) == LocalDateTime.class) {
                    row.add(new Constant().getDateDTF().format((LocalDateTime) selectedRow.get(j)));
                } else {
                    row.add((String) selectedRow.get(j));
                }
            }
            result.add(row);
        }
        return result;
    }

    /**
     * Adds a key listener for table to select first row that match a letter
     * pressed.
     *
     * @param e the key event, ie key pressed
     * @param selectedRow the selected row
     * @param sortedColumn the sorted column (if artist go to first artist starting
     *            to the letter pressed)
     * @return the selected row, -1 if no result
     */
    @SuppressWarnings("unchecked")
    public static int keyShortcutAction(KeyEvent e, int selectedRow, Integer sortedColumn) {
        if (!Character.isLetter(e.getKeyChar()) && !Character.isDigit(e.getKeyChar())) {
            return selectedRow;
        }
        LOG.debug("Start keyShortcutAction");
        JTable target = (JTable) e.getSource();
        String keyChar = String.valueOf(e.getKeyChar());
        TableModel tableModel = target.getModel();
        int startRow = selectedRow;
        if (selectedRow == tableModel.getRowCount() - 1) {
            startRow = -1;// Go before start
        }
        // Check each cell to see if it starts with typed char.
        // if so set corresponding row selected and return.
        for (int row = startRow + 1; row < tableModel.getRowCount(); row++) {
            Vector<String> vector = (Vector<String>) ((AbstractModel) target.getModel()).getDataVector()
                    .get(target.getRowSorter().convertRowIndexToModel(row));
            Object value = vector.get(sortedColumn != null ? sortedColumn : 0);
            if (value != null
                    && ((value instanceof String && ((String) value).toLowerCase().startsWith(keyChar.toLowerCase()))
                            || ((value instanceof Integer || value instanceof Long)
                                    && String.valueOf(value).toLowerCase().startsWith(keyChar.toLowerCase())))) {
                target.getSelectionModel().clearSelection();
                target.getColumnModel().getSelectionModel().clearSelection();
                target.changeSelection(row, 0, true, false);
                target.setRowSelectionInterval(row, row);
                LOG.debug("End keyShortcutAction");
                return row;
            }
        }
        LOG.debug("End keyShortcutAction, no result");
        return selectedRow != -1 ? keyShortcutAction(e, -1, sortedColumn) : -1;
    }

    /**
     * Supprime les compositions sélectionnées du tableau et des fichiers XML.
     *
     * @param compoList a composition list displayed
     * @param uuids uuids of compositions to delete
     * @throws MyException
     */
    public static void deleteCompositionAction(List<Composition> compoList, List<String> uuids) throws MyException {
        LOG.debug("Start deleteCompositionAction");
        if (uuids == null || uuids.isEmpty()) {
            throw new MyException("Aucune composition sélectionnée !");
        }
        OngletPanel.getArtist().interruptUpdateArtist(true);
        List<Composition> importXML = ImportXML.importXML(Constant.getFinalFilePath());
        for (String uuid : uuids) {
            Composition toRemoveToFinal = null;
            Optional<Composition> found = CompositionUtils.findByUuid(importXML, Arrays.asList(uuid));
            if (found.isPresent()) {
                toRemoveToFinal = found.get();
            } else {
                throw new MyException("Can't find composition to delete in final file");
            }
            // Update JTable Data
            CompositionUtils.findByUuid(compoList, Arrays.asList(uuid)).ifPresent(c -> c.setDeleted(true));
            // Update xml files
            CompositionUtils.removeCompositionInFiles(toRemoveToFinal);
            // Update Final File
            toRemoveToFinal.setDeleted(true);
            try {
                for (Fichier file : toRemoveToFinal.getFiles()) {
                    // Update FichierPanel Data
                    List<Composition> compoListFromData = OngletPanel.getFichier().getCompoListFromData(file);
                    CompositionUtils.findByUuid(compoListFromData, toRemoveToFinal.getUuids()).ifPresent(c -> c.setDeleted(true));
                }
            } catch (Exception e) {
                LOG.warn("Erreur lors de la mise à jour du panel", e);
            }
        }
        try {
            ExportXML.exportXML(importXML, Constant.getFinalFile());
            OngletPanel.getArtist().updateArtistPanel();
        } catch (IOException e1) {
            throw new MyException("Erreur lors de l'export du fichier final !!", e1);
        }
        LOG.debug("End deleteCompositionAction");
    }

    /**
     * Launchs a dialog to modify the selected composition.
     *
     * @param selectedRow the selected composition
     * @param compositionList the composition list displayed
     * @param index column index of selected row
     * @return
     * @throws MyException if something went wrong
     */
    public static List<Composition> editCompositionAction(Vector<String> selectedRow, List<Composition> compositionList,
            ColumnIndex index) throws MyException {
        LOG.debug("Start editCompositionAction");
        if (selectedRow == null || selectedRow.isEmpty()) {
            throw new MyException("Aucune composition sélectionnée !");
        }
        OngletPanel.getArtist().interruptUpdateArtist(true);
        String uuid = MiscUtils.stringToUuids(selectedRow.get(index.get(Index.UUID))).get(0);
        Composition toModif;
        List<Composition> importXML;
        importXML = ImportXML.importXML(Constant.getFinalFilePath());
        // On récupère la composition à modifier
        Optional<Composition> found = CompositionUtils.findByUuid(importXML, Arrays.asList(uuid));
        if (found.isPresent()) {
            toModif = found.get();
        } else {
            throw new MyException("Can't find composition: " + selectedRow + " to edit in final file");
        }
        // Lancement de la popup de modification
        ModifyCompositionDialog md = new ModifyCompositionDialog(selectedRow, index);
        md.showModifyCompositionDialog();
        Vector<String> editedRow;
        if (md.isSendData()) {
            // On recupère la compo si elle a bien été modifiée
            LOG.debug("Composition modifiée");
            editedRow = md.getCompo();
        } else {
            LOG.debug("Aucune modification");
            return compositionList;
        }

        // On modifie la composition
        toModif.setArtist(editedRow.get(index.get(Index.ARTIST)));
        toModif.setTitre(editedRow.get(index.get(Index.TITLE)));
        toModif.setRecordType(RecordType.valueOf(editedRow.get(index.get(Index.TYPE))));
        toModif.setDeleted(Boolean.valueOf(editedRow.get(index.get(Index.DELETED))));

        // Edit final file and displayed list
        importXML = importXML.stream().filter(c -> !c.getUuids().contains(uuid)).collect(Collectors.toList());
        Optional<Composition> compoExist = ImportXML.findAndMergeComposition(importXML, toModif, true);
        if (!compoExist.isPresent()) {
            LOG.debug("Pas de regroupement");
            CompositionUtils.findByUuid(compositionList, Arrays.asList(uuid)).ifPresent(c -> {
                c.setArtist(toModif.getArtist());
                c.setTitre(toModif.getTitre());
                c.setRecordType(toModif.getRecordType());
                c.setDeleted(toModif.isDeleted());
            });
        } else {
            LOG.debug("La compo existe déjà, on regroupe");
            // regroupement pour la liste des compositions affichées
            compositionList = compositionList.stream().filter(c -> !c.getUuids().contains(uuid)).collect(Collectors.toList());
            ImportXML.findAndMergeComposition(compositionList, toModif, true);
        }

        try {
            ExportXML.exportXML(importXML, Constant.getFinalFile());
            OngletPanel.getArtist().updateArtistPanel();
        } catch (IOException e1) {
            throw new MyException("Error when exporting final file", e1);
        }

        // On modifie les fichiers xml en conséquence
        try {
            CompositionUtils.editCompositionsInFiles(toModif, compoExist.map(Composition::isDeleted).orElse(toModif.isDeleted()));
        } catch (MyException e1) {
            throw new MyException("Error editing a composition: " + editedRow, e1);
        }

        // Modification des données de fichier panel
        updateFichierPanelData(compoExist.map(c -> c).orElse(toModif));
        LOG.debug("End editCompositionAction");
        return compositionList;
    }

    /**
     * Updates fichier panel data when a row has been edited.
     * <p>
     * Recover composition's files by name and find composition edited by uuid. And then replace it by the given composition.
     * </p>
     *
     * @param edited the edited composition
     */
    private static void updateFichierPanelData(Composition edited) {
        for (Fichier file : edited.getFiles()) {
            CompositionUtils.findByUuid(OngletPanel.getFichier().getCompoListFromData(file).stream()
                    .filter(Objects::nonNull).collect(Collectors.toList()), edited.getUuids())
            .ifPresent(c -> CompositionUtils.copy(edited, c));
        }
    }

    /**
     * Crée un {@link JPanel} avec un layout de type {@link BoxLayout}.
     *
     * @param axis l'axe sur lequel les composants sont alignés.
     * @see {@link BoxLayout#X_AXIS} et {@link BoxLayout#Y_AXIS}
     * @return le panel crée
     */
    public static JPanel createBoxLayoutPanel(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        panel.add(Box.createRigidArea(new Dimension(100, 0)));
        return panel;
    }

    /**
     * Modifie la taille du composant donné.
     *
     * @param comp le composant
     * @param width la nouvelle largeur
     * @param height la nouvelle hauteur
     */
    public static void setSize(Component comp, double width, double height) {
        Dimension dimension = new Dimension();
        dimension.setSize(width, height);
        comp.setMinimumSize(dimension);
        comp.setPreferredSize(dimension);
        comp.setMaximumSize(comp.getPreferredSize());
    }

    /**
     * Ajoute un composant au panel.
     *
     * @param panel le futur panel parent
     * @param component le composant à ajouter
     * @param alignement l'alignement vertical
     * @param rigidSize la largeur de la rigid area
     */
    public static void addComponent(JPanel panel, JComponent component, float alignement, int rigidSize) {
        component.setAlignmentX(alignement);
        component.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(component);
        panel.add(Box.createRigidArea(new Dimension(rigidSize, 0)));
    }

    /**
     * Adds line border around given component. Useful for debugging views
     *
     * @param comp the component
     * @param c color of the border
     */
    public static void setBorder(JComponent comp, Color c) {
        comp.setBorder(BorderFactory.createLineBorder(c, 2));
    }

    /**
     * Sets the size of given component with percentage of screen size dimension.
     *
     * @param comp component to resize
     * @param percentWidth percent of screen width
     * @param percentHeight percent of screen height
     */
    public static void setSizeByScreenSize(Component comp, double percentWidth, double percentHeight) {
        double width = getPercent(widthBG, percentWidth);
        double height = getPercent(heightBG, percentHeight);
        setSize(comp, width, height);
    }

    private static double getPercent(final BigDecimal bg, double percent) {
        return bg.divide(BigDecimal.valueOf(100D), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(percent)).doubleValue();
    }
}
