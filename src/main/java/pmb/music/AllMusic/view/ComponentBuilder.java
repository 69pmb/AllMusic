package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.swing.FontIcon;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputText;

/**
 * Builder for various JComponent. Created by PBR on 4 févr. 2019.
 */
public class ComponentBuilder {
	public static final int PANEL_HEIGHT = 70;
	public static final int COMPONENT_HEIGHT = 25;
	public static final int LABEL_HEIGHT = 15;
	private ComponentBuilderConfiguration config;

	/**
	 * Creates a builder for a specific component. It will be parametrized by the
	 * {@code with..} methods.
	 * 
	 * @param type class of the component
	 */
	public ComponentBuilder(Class<? extends JComponent> type) {
		this.config = new ComponentBuilderConfiguration();
		this.config.setType(type);
	}

	/**
	 * Builds the component.
	 * 
	 * @return the component built
	 */
	public JComponent build() {
		if (config.getType().equals(JComboCheckBox.class)) {
			return buildJComboCheckBox();
		} else if (config.getType().equals(JComboBoxInput.class)) {
			return buildJComboBoxInput();
		} else if (config.getType().equals(MyInputText.class)) {
			return buildMyInputText();
		} else {
			return new JPanel();
		}
	}

	/**
	 * Creates a JComboCheckBox and the layout around it.
	 * 
	 * @param parent parent panel to be add
	 * @param values values of the box
	 * @param label label
	 * @param layout layout for the box panel
	 * @param widthPanel width of the box panel
	 * @param widthBox width of the box
	 * @param widthLabel with of the label
	 * @return
	 */
	private JComboCheckBox buildJComboCheckBox() {
		JPanel boxPanel;
		if (config.isFlowLayout()) {
			boxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		} else {
			boxPanel = new JPanel();
		}
		PanelUtils.setSize(boxPanel, config.getPanelWidth(), PANEL_HEIGHT);
		JComboCheckBox box = new JComboCheckBox(config.getValues());
		box.setPreferredSize(new Dimension(config.getComponentWidth(), COMPONENT_HEIGHT));
		boxPanel.add(buildJLabel(config.getLabel(), config.getLabelWidth()));
		boxPanel.add(box);
		config.getParent().add(boxPanel);
		return box;
	}

	/**
	 * Creates a JComboBoxInput and the layout around it.
	 * 
	 * @param parent parent panel to be add
	 * @param comboBoxValues values of the combo box
	 * @param label label
	 * @param widthPanel width of the box panel
	 * @param widthLabel with of the label
	 * @param inputWidth width of the input
	 * @return
	 */
	private JComboBoxInput buildJComboBoxInput() {
		MyInputText text = new MyInputText(JTextField.class, config.getComponentWidth());
		text.getInput().addFocusListener(PanelUtils.selectAll);
		JComboBox<String> box = new JComboBox<>(config.getValues().stream().toArray(String[]::new));
		PanelUtils.setSize(box, 45, COMPONENT_HEIGHT);
		JComboBoxInput input = new JComboBoxInput(text, box);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		PanelUtils.setSize(panel, config.getPanelWidth(), PANEL_HEIGHT);
		panel.add(buildJLabel(config.getLabel(), config.getLabelWidth()));
		panel.add(input);
		config.getParent().add(panel);
		return input;
	}

	/**
	 * Creates a MyInputText and the layout around it.
	 * 
	 * @param parent its parent panel
	 * @param values its values
	 * @param filterMode a TextMatcherEditor constant
	 * @param labelText its label
	 * @param labelWidth label width
	 * @param inputWidth its width
	 * @param panelWidth panel width
	 * @return
	 */
	private MyInputText buildMyInputText() {
		JPanel inputPanel;
		if (config.isFlowLayout()) {
			inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		} else {
			inputPanel = new JPanel();
		}
		JLabel label = buildJLabel(config.getLabel(), config.getLabelWidth());
		MyInputText input = new MyInputText(JComboBox.class, config.getComponentWidth());
		AutoCompleteSupport<Object> install = AutoCompleteSupport.install((JComboBox<?>) input.getInput(),
				GlazedLists.eventListOf(config.getValues().toArray()));
		if (config.isFilterContains()) {
			install.setFilterMode(TextMatcherEditor.CONTAINS);
		}
		PanelUtils.setSize(inputPanel, config.getPanelWidth(), PANEL_HEIGHT);
		inputPanel.add(label);
		inputPanel.add(input);
		config.getParent().add(inputPanel);
		return input;
	}

	public static JButton buildJButton(String label, int width, FontAwesome icon) {
		JButton btn = new JButton(label, FontIcon.of(icon));
		btn.setBackground(Color.white);
		btn.setPreferredSize(new Dimension(width, PANEL_HEIGHT));
		return btn;
	}

	public static JLabel buildJLabel(String text, int width) {
		JLabel jLabel = new JLabel(text, SwingConstants.CENTER);
		PanelUtils.setSize(jLabel, width, LABEL_HEIGHT);
		return jLabel;
	}

	public ComponentBuilder withParent(JPanel parent) {
		this.config.setParent(parent);
		return this;
	}

	public ComponentBuilder withValues(List<String> values) {
		this.config.setValues(values);
		return this;
	}

	public ComponentBuilder withLabel(String label) {
		this.config.setLabel(label);
		return this;
	}

	public ComponentBuilder withFlowLayout(boolean isFlowLayout) {
		this.config.setFlowLayout(isFlowLayout);
		return this;
	}

	public ComponentBuilder withFilterContains(boolean isFilterContains) {
		this.config.setFilterContains(isFilterContains);
		return this;
	}

	public ComponentBuilder withPanelWidth(int panelWidth) {
		this.config.setPanelWidth(panelWidth);
		return this;
	}

	public ComponentBuilder withComponentWidth(int componentWidth) {
		this.config.setComponentWidth(componentWidth);
		return this;
	}

	public ComponentBuilder withLabelWidth(int labelWidth) {
		this.config.setLabelWidth(labelWidth);
		return this;
	}
}
