package pmb.allmusic.view;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Configuration to build swing {@link JComponent} with {@code ComponentBuilder}.
 *
 * @param <T> type of the built component
 * @param <V> type of the items of the component to build
 */
public class ComponentBuilderConfiguration<T extends JComponent, V> {
  private Class<T> type;
  private JPanel parent;
  private V[] values;
  private String label;
  private boolean isFlowLayout;
  private boolean isFilterContains;
  private V initialValue;
  private int panelWidth;
  private int componentWidth;
  private int labelWidth;
  private int height;
  private Resize resize;
  private Color color;
  private int fontSize;
  private Supplier<V[]> asyncValues;
  private CompletableFuture<Void> async;

  /**
   * Constructor of {@link ComponentBuilderConfiguration} with default values sets (for instance
   * dimension, color).
   */
  public ComponentBuilderConfiguration() {
    super();
    label = "";
    color = Color.BLACK;
    panelWidth = 200;
    componentWidth = 130;
    height = ComponentBuilder.COMPONENT_HEIGHT;
  }

  public Class<T> getType() {
    return type;
  }

  public void setType(Class<T> type) {
    this.type = type;
  }

  public JPanel getParent() {
    return parent;
  }

  public void setParent(JPanel parent) {
    this.parent = parent;
  }

  public V[] getValues() {
    return values != null ? values.clone() : null;
  }

  public void setValues(V[] values) {
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

  public V getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(V initialValue) {
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

  public Resize getResize() {
    return resize;
  }

  public void setResize(Resize resize) {
    this.resize = resize;
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

  public Supplier<V[]> getAsyncValues() {
    return asyncValues;
  }

  public void setAsyncValues(Supplier<V[]> asyncValues) {
    this.asyncValues = asyncValues;
  }

  public CompletableFuture<Void> getAsync() {
    return async;
  }

  public void setAsync(CompletableFuture<Void> async) {
    this.async = async;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
