package pmb.music.AllMusic.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
public class ComponentBuilder<T> {
	public static final int PANEL_HEIGHT = 70;
	public static final int COMPONENT_HEIGHT = 25;
	public static final int LABEL_HEIGHT = 15;
	private ComponentBuilderConfiguration<T> config;

	/**
	 * Creates a builder for a specific component. It will be parametrized by the
	 * {@code with..} methods.
	 * <ul>
	 * Allowed types are:
	 * </ul>
	 * <li>JComboCheckBox : A list with multiples selectable choices</li>
	 * <li>JComboBoxInput : An input text with a combo box</li>
	 * <li>MyInputText : An input text with suggestions</li>
	 * <li>JComboBox : A list of value with one selectable item</li>
	 * <li>JCheckBox : True / false</li>
	 * <li>MyInputRange : Two inputs together</li>
	 * <li>JTextField : A classic text field</li>
	 * <li>JLabel : A label</li>
	 * 
	 * @param type class of the component
	 */
	public ComponentBuilder(Class<? extends JComponent> type) {
		this.config = new ComponentBuilderConfiguration<>();
		this.config.setType(type);
	}

	/**
	 * Builds the component.
	 * 
	 * @return the component built
	 */
	public JComponent build() {
		if (config.getParent() == null) {
			throw new IllegalArgumentException("Component parent is required");
		}
		JComponent result;
		if (config.getType().equals(JComboCheckBox.class)) {
			// A list with multiples selectable choices
			requiredValues();
			result = buildJComboCheckBox();
		} else if (config.getType().equals(JComboBoxInput.class)) {
			// An input text with a combo box
			requiredValues();
			result = buildJComboBoxInput();
		} else if (config.getType().equals(MyInputText.class)) {
			// An input text with suggestions
			result = buildMyInputText();
		} else if (config.getType().equals(JComboBox.class)) {
			// A list of value with one selectable item
			requiredValues();
			result = buildJComboBox();
		} else if (config.getType().equals(JCheckBox.class)) {
			// True / false
			result = buildJCheckBox();
		} else if (config.getType().equals(MyInputRange.class)) {
			// Two inputs together
			result = buildMyInputRange();
		} else if (config.getType().equals(JTextField.class)) {
			// A classic text field
			result = buildJTextField();
		} else if (config.getType().equals(JLabel.class)) {
			// A label
			result = buildJLabel();
		} else {
			throw new IllegalArgumentException("Incorrect component type: " + config.getType().getName());
		}
		return result;
	}

	/**
	 * Creates a JComboCheckBox and the layout around it.
	 * 
	 * @return the component built
	 */
	@SuppressWarnings("unchecked")
	private JComboCheckBox buildJComboCheckBox() {
		JPanel boxPanel = buildComponentPanel();
		JComboCheckBox box = new JComboCheckBox((List<String>) Arrays.asList(config.getValues()));
		box.setPreferredSize(new Dimension(config.getComponentWidth(), COMPONENT_HEIGHT));
		boxPanel.add(buildJLabel(config.getLabel(), config.getLabelWidth()));
		boxPanel.add(box);
		return box;
	}

	/**
	 * Creates a JComboBoxInput and the layout around it.
	 * 
	 * @return the component built
	 */
	private JComboBoxInput<T> buildJComboBoxInput() {
		JPanel panel = buildComponentPanel();
		JComboBoxInput<T> input = new JComboBoxInput<>(new MyInputText(JTextField.class, config.getComponentWidth()),
				new JComboBox<>(config.getValues()));
		panel.add(buildJLabel(config.getLabel(), config.getLabelWidth()));
		panel.add(input);
		return input;
	}

	/**
	 * Creates a MyInputText and the layout around it.
	 * 
	 * @return the component built
	 */
	private MyInputText buildMyInputText() {
		JPanel inputPanel = buildComponentPanel();
		MyInputText input;
		if (config.getValues() != null && config.getValues().length > 0) {
			input = new MyInputText(JComboBox.class, config.getComponentWidth());
			AutoCompleteSupport<Object> install = AutoCompleteSupport.install((JComboBox<?>) input.getInput(),
					GlazedLists.eventListOf(config.getValues()));
			if (config.isFilterContains()) {
				install.setFilterMode(TextMatcherEditor.CONTAINS);
			}
		} else {
			input = new MyInputText(JTextField.class, config.getComponentWidth());
		}
		inputPanel.add(buildJLabel(config.getLabel(), config.getLabelWidth()));
		inputPanel.add(input);
		return input;
	}

	/**
	 * Creates a JComboBox and the layout around it.
	 * 
	 * @return the component built
	 */
	private JComboBox<T> buildJComboBox() {
		JPanel panel = buildComponentPanel();
		JComboBox<T> box = new JComboBox<>(config.getValues());
		PanelUtils.setSize(box, config.getComponentWidth(), ComponentBuilder.COMPONENT_HEIGHT);
		if (config.getInitialValue() != null) {
			box.setSelectedItem(config.getInitialValue());
		}
		panel.add(buildJLabel(config.getLabel(), config.getLabelWidth()));
		panel.add(box);
		return box;
	}

	/**
	 * Builds a JCheckBox component.
	 * 
	 * @return the JCheckBox built
	 */
	private JCheckBox buildJCheckBox() {
		JPanel panel = buildComponentPanel();
		JCheckBox checkBox = new JCheckBox();
		PanelUtils.setSize(checkBox, config.getComponentWidth(), ComponentBuilder.COMPONENT_HEIGHT);
		if (config.getInitialValue() != null) {
			checkBox.setSelected((boolean) config.getInitialValue());
		}
		checkBox.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(ComponentBuilder.buildJLabel(config.getLabel(), config.getLabelWidth()));
		panel.add(checkBox);
		return checkBox;
	}

	/**
	 * Builds a MyInputRange component.
	 * 
	 * @return the MyInputRange built
	 */
	private MyInputRange buildMyInputRange() {
		JPanel rangePanel = buildComponentPanel();
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
		if (StringUtils.isNotBlank((String) config.getInitialValue())) {
			if (!StringUtils.contains((String) config.getInitialValue(), " - ")) {
				throw new IllegalArgumentException(
						config.getType().getName() + " initial value must be splittable by \' - \'");
			}
			String[] split = StringUtils.split((String) config.getInitialValue(), " - ");
			range.getFirst().setText(split[0]);
			range.getSecond().setText(split[1]);
		}
		rangePanel.add(ComponentBuilder.buildJLabel(config.getLabel(), config.getLabelWidth()));
		rangePanel.add(range.getFirst());
		rangePanel.add(range.getSecond());
		return range;
	}

	private JTextField buildJTextField() {
		JPanel panel = buildComponentPanel();
		JTextField text = new JTextField((String) config.getInitialValue());
		PanelUtils.setSize(text, config.getComponentWidth(), COMPONENT_HEIGHT);
		panel.add(ComponentBuilder.buildJLabel(config.getLabel(), config.getLabelWidth()));
		panel.add(text);
		return text;
	}

	private JLabel buildJLabel() {
		JPanel panel = buildComponentPanel();
		JLabel label = new JLabel(config.getLabel(), SwingConstants.CENTER);
		PanelUtils.setSize(label, config.getLabelWidth(), COMPONENT_HEIGHT);
		label.setForeground(config.getColor());
		if (config.getFontSize() > 0) {
			Font labelFont = label.getFont();
			label.setFont(new Font(labelFont.getName(), labelFont.getStyle(), config.getFontSize()));
		}
		label.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(label);
		return label;
	}

	public static JButton buildJButton(String label, int width, FontAwesome icon) {
		JButton btn = new JButton(label, FontIcon.of(icon));
		btn.setBackground(Color.white);
		btn.setPreferredSize(new Dimension(width, PANEL_HEIGHT));
		return btn;
	}

	public static JLabel buildJLabel(String text, int width) {
		JLabel jLabel = new JLabel(text, SwingConstants.CENTER);
		if (StringUtils.isNotBlank(text) && text.length() > 30) {
			PanelUtils.setSize(jLabel, width, COMPONENT_HEIGHT);
		} else {
			PanelUtils.setSize(jLabel, width, LABEL_HEIGHT);
		}
		return jLabel;
	}

	public static JTextPane initJTextPaneComponent(Color color, int fontSize) {
		JTextPane textPane = new JTextPane();
		textPane.setOpaque(false);
		textPane.setEditable(false);
		textPane.setBackground(UIManager.getColor("Label.background"));
		textPane.setFont(UIManager.getFont("Label.font"));
		textPane.setBorder(UIManager.getBorder("Label.border"));
		textPane.setForeground(color);
		Font labelFont = textPane.getFont();
		textPane.setFont(new Font(labelFont.getName(), labelFont.getStyle(), fontSize));
		StyledDocument doc = textPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		return textPane;
	}

	/**
	 * Builds the panel that will hold the component.
	 * 
	 * @return the panel added to the parent panel
	 */
	private JPanel buildComponentPanel() {
		JPanel panel;
		if (config.isFlowLayout()) {
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		} else {
			panel = new JPanel();
		}
		PanelUtils.setSize(panel, config.getPanelWidth(), ComponentBuilder.PANEL_HEIGHT);
		config.getParent().add(panel);
		return panel;
	}

	private void requiredValues() {
		if (config.getValues() == null || config.getValues().length == 0) {
			throw new IllegalArgumentException(
					"Component values are required for the type: " + config.getType().getName());
		}
	}

	/**
	 * Configures the parent panel where the component will be added to.
	 * 
	 * @param parent the panel
	 * @return the builder
	 */
	public ComponentBuilder<T> withParent(JPanel parent) {
		this.config.setParent(parent);
		return this;
	}

	/**
	 * Configures the values of the component.
	 * 
	 * @param values a list of string
	 * @return the builder
	 */
	public ComponentBuilder<T> withValues(T[] values) {
		if (!config.getType().equals(JComboCheckBox.class) && !config.getType().equals(JComboBoxInput.class)
				&& !config.getType().equals(MyInputText.class) && !config.getType().equals(JComboBox.class)) {
			throw new IllegalArgumentException(config.getType().getName() + " must not use the property Values");
		}
		this.config.setValues(values);
		return this;
	}

	/**
	 * Configures the label value.
	 * 
	 * @param label a string
	 * @return the builder
	 */
	public ComponentBuilder<T> withLabel(String label) {
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
	public ComponentBuilder<T> withFlowLayout(boolean isFlowLayout) {
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
	public ComponentBuilder<T> withFilterContains(boolean isFilterContains) {
		if (!config.getType().equals(MyInputText.class)) {
			throw new IllegalArgumentException(
					config.getType().getName() + " must not use the property Filter Contains");
		}
		this.config.setFilterContains(isFilterContains);
		return this;
	}

	/**
	 * Configures panel width.
	 * 
	 * @param panelWidth an int
	 * @return the builder
	 */
	public ComponentBuilder<T> withPanelWidth(int panelWidth) {
		this.config.setPanelWidth(panelWidth);
		return this;
	}

	/**
	 * Configures component width.
	 * 
	 * @param componentWidth and int
	 * @return the builder
	 */
	public ComponentBuilder<T> withComponentWidth(int componentWidth) {
		if (config.getType().equals(JLabel.class)) {
			throw new IllegalArgumentException(
					config.getType().getName() + " must not use the property Component Width");
		}
		this.config.setComponentWidth(componentWidth);
		return this;
	}

	/**
	 * Configures label width.
	 * 
	 * @param labelWidth an int
	 * @return the builder
	 */
	public ComponentBuilder<T> withLabelWidth(int labelWidth) {
		this.config.setLabelWidth(labelWidth);
		return this;
	}

	/**
	 * Configures font size.
	 * 
	 * @param fontSize an int
	 * @return the builder
	 */
	public ComponentBuilder<T> withFontSize(int fontSize) {
		if (!config.getType().equals(JLabel.class)) {
			throw new IllegalArgumentException(config.getType().getName() + " must not use the property Font Size");
		}
		this.config.setFontSize(fontSize);
		return this;
	}

	/**
	 * Configures color.
	 * 
	 * @param color a Color
	 * @return the builder
	 */
	public ComponentBuilder<T> withColor(Color color) {
		if (!config.getType().equals(JLabel.class)) {
			throw new IllegalArgumentException(config.getType().getName() + " must not use the property Color");
		}
		this.config.setColor(color);
		return this;
	}

	/**
	 * Configures initial value.
	 * 
	 * @param defaultValue
	 * @return the builder
	 */
	public ComponentBuilder<T> withInitialValue(T initialValue) {
		if (!config.getType().equals(JCheckBox.class) && !config.getType().equals(JComboBox.class)
				&& !config.getType().equals(JTextField.class) && !config.getType().equals(MyInputRange.class)) {
			throw new IllegalArgumentException(config.getType().getName() + " must not use the property Initial Value");
		}
		this.config.setInitialValue(initialValue);
		return this;
	}
}
