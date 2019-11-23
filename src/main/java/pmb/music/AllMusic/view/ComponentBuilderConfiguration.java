package pmb.music.AllMusic.view;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Configuration to build swing {@link JComponent} with
 * {@code ComponentBuilder}.
 * 
 * @param <T> type of the items of the component to build
 */
public class ComponentBuilderConfiguration<T> {
    private Class<? extends JComponent> type;
    private JPanel parent;
    private T[] values;
    private String label;
    private boolean isFlowLayout;
    private boolean isFilterContains;
    private T initialValue;
    private int panelWidth;
    private int componentWidth;
    private int labelWidth;
    private Color color;
    private int fontSize;

    /**
     * Constructor of {@link ComponentBuilderConfiguration} with default values sets
     * (dimension, color).
     */
    public ComponentBuilderConfiguration() {
        super();
        label = "";
        color = Color.BLACK;
        panelWidth = 200;
        labelWidth = 170;
        componentWidth = 130;
    }

    public Class<? extends JComponent> getType() {
        return type;
    }

    public void setType(Class<? extends JComponent> type) {
        this.type = type;
    }

    public JPanel getParent() {
        return parent;
    }

    public void setParent(JPanel parent) {
        this.parent = parent;
    }

    public T[] getValues() {
        return values != null ? values.clone() : null;
    }

    public void setValues(T[] values) {
        this.values = values != null ? values.clone() : null;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isFlowLayout() {
        return isFlowLayout;
    }

    public void setFlowLayout(boolean isFlowLayout) {
        this.isFlowLayout = isFlowLayout;
    }

    public boolean isFilterContains() {
        return isFilterContains;
    }

    public void setFilterContains(boolean isFilterContains) {
        this.isFilterContains = isFilterContains;
    }

    public T getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(T initialValue) {
        this.initialValue = initialValue;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public int getComponentWidth() {
        return componentWidth;
    }

    public void setComponentWidth(int componentWidth) {
        this.componentWidth = componentWidth;
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
