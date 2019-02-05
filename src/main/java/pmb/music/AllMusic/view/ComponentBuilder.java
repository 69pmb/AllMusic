package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.swing.FontIcon;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputRange;
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
		} else if (config.getType().equals(MyInputRange.class)) {
			return buildMyInputRange();
		} else {
			return new JPanel();
		}
	}

	/**
	 * Builds a MyInputRange component.
	 * 
	 * @return the MyInputRange built
	 */
	private MyInputRange buildMyInputRange() {
		JPanel rangePanel;
		if (config.isFlowLayout()) {
			rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		} else {
			rangePanel = new JPanel();
		}
		PanelUtils.setSize(rangePanel, config.getPanelWidth(), ComponentBuilder.PANEL_HEIGHT);
		MyInputRange range = new MyInputRange(new MyInputText(JTextField.class, config.getComponentWidth() / 2),
				new MyInputText(JTextField.class, config.getComponentWidth() / 2));
		range.getFirst().getInput().addFocusListener(PanelUtils.selectAll);
		range.getSecond().getInput().addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// Nothing to do
			}

			@Override
			public void focusGained(FocusEvent e) {
				JTextField source = (JTextField) e.getSource();
				if (StringUtils.isNotBlank(range.getFirst().getText())) {
					source.setText(range.getFirst().getText());
					source.selectAll();
				}
			}
		});
		rangePanel.add(ComponentBuilder.buildJLabel(config.getLabel(), config.getLabelWidth()));
		rangePanel.add(range.getFirst());
		rangePanel.add(range.getSecond());
		config.getParent().add(rangePanel);
		return range;
	}

	/**
	 * Creates a JComboCheckBox and the layout around it.
	 * 
	 * @return the component built
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
	 * @return the component built
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
	 * @return the component built
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

	/**
	 * Configures the parent panel where the component will be added to.
	 * 
	 * @param parent the panel
	 * @return the builder
	 */
	public ComponentBuilder withParent(JPanel parent) {
		this.config.setParent(parent);
		return this;
	}

	/**
	 * Configures the values of the component.
	 * 
	 * @param values a list of string
	 * @return the builder
	 */
	public ComponentBuilder withValues(List<String> values) {
		this.config.setValues(values);
		return this;
	}

	/**
	 * Configures the label value.
	 * 
	 * @param label a string
	 * @return the builder
	 */
	public ComponentBuilder withLabel(String label) {
		this.config.setLabel(label);
		return this;
	}

	/**
	 * Configures if the panel will be created with a FlowLayout.
	 * 
	 * @param isFlowLayout if true, init panel with
	 *            {@code new FlowLayout(FlowLayout.CENTER, 0, 0)}
	 * @return the builder
	 */
	public ComponentBuilder withFlowLayout(boolean isFlowLayout) {
		this.config.setFlowLayout(isFlowLayout);
		return this;
	}

	/**
	 * Configures if the filter mode will be contains. Only for MyInputText
	 * component.
	 * 
	 * @param isFilterContains set filer mode to contains if true
	 * @return the builder
	 */
	public ComponentBuilder withFilterContains(boolean isFilterContains) {
		this.config.setFilterContains(isFilterContains);
		return this;
	}

	/**
	 * Configures panel width.
	 * 
	 * @param panelWidth an int
	 * @return the builder
	 */
	public ComponentBuilder withPanelWidth(int panelWidth) {
		this.config.setPanelWidth(panelWidth);
		return this;
	}

	/**
	 * Configures component width.
	 * 
	 * @param componentWidth and int
	 * @return the builder
	 */
	public ComponentBuilder withComponentWidth(int componentWidth) {
		this.config.setComponentWidth(componentWidth);
		return this;
	}

	/**
	 * Configures label width.
	 * 
	 * @param labelWidth an int
	 * @return the builder
	 */
	public ComponentBuilder withLabelWidth(int labelWidth) {
		this.config.setLabelWidth(labelWidth);
		return this;
	}
}
