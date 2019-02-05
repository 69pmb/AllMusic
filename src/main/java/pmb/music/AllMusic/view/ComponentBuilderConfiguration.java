package pmb.music.AllMusic.view;

import java.awt.Color;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Created by PBR on 4 févr. 2019.
 */
public class ComponentBuilderConfiguration {
	private Class<? extends JComponent> type;
	private JPanel parent;
	private List<String> values;
	private String label;
	private boolean isFlowLayout;
	private boolean isFilterContains;
	private boolean defaultBooleanValue;
	private int panelWidth;
	private int componentWidth;
	private int labelWidth;
	private Color color;
	private int fontSize;

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

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
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

	public boolean getDefaultBooleanValue() {
		return defaultBooleanValue;
	}

	public void setDefaultBooleanValue(boolean defaultBooleanValue) {
		this.defaultBooleanValue = defaultBooleanValue;
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
