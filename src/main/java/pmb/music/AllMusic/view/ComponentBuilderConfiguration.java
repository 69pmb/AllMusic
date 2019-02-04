package pmb.music.AllMusic.view;

import java.util.List;

import javax.swing.JPanel;

/**
 * Created by PBR on 4 févr. 2019.
 */
public class ComponentBuilderConfiguration {
	private JPanel parent;
	private List<String> values;
	private String label;
	private boolean isFlowLayout;
	private boolean isFilterContains;
	private int panelWidth;
	private int componentWidth;
	private int labelWidth;

	public ComponentBuilderConfiguration(JPanel parent, List<String> values, String label, boolean isFlowLayout,
			boolean isFilterContains, int panelWidth, int componentWidth, int labelWidth) {
		super();
		this.parent = parent;
		this.values = values;
		this.label = label;
		this.isFlowLayout = isFlowLayout;
		this.isFilterContains = isFilterContains;
		this.panelWidth = panelWidth;
		this.componentWidth = componentWidth;
		this.labelWidth = labelWidth;
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
}
