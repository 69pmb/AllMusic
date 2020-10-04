package pmb.music.AllMusic.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.swing.FontIcon;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import pmb.music.AllMusic.view.component.JComboBoxInput;
import pmb.music.AllMusic.view.component.JComboCheckBox;
import pmb.music.AllMusic.view.component.MyInputRange;
import pmb.music.AllMusic.view.component.MyInputText;

/**
 * Builder for various JComponent. Created by PBR on 4 févr. 2019.
 *
 * @param <T> type of the built component
 * @param <V> type of the items of the built component
 */
public class ComponentBuilder<T extends JComponent, V> {
    private static final Logger LOG = LogManager.getLogger(ComponentBuilder.class);
    public static final int PANEL_HEIGHT = 70;
    public static final int COMPONENT_HEIGHT = 25;
    public static final int LABEL_HEIGHT = 15;
    private final ComponentBuilderConfiguration<T, V> config;

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
    public ComponentBuilder(Class<T> type) {
        config = new ComponentBuilderConfiguration<>();
        config.setType(type);
    }

    /**
     * Builds the component.
     *
     * @return the component built
     */
    @SuppressWarnings("unchecked")
    public T build() {
        if (config.getParent() == null) {
            throw new IllegalArgumentException("Component parent is required");
        }
        if (config.getResize() != null) {
            config.setPanelWidth(config.getResize().getPanelWidth());
            config.setComponentWidth(config.getResize().getComponentWidth());
        }
        if (config.getPanelWidth() < config.getComponentWidth()) {
            LOG.info("Panel width must be greater than component width: {}  vs {}", config.getPanelWidth(),
                    config.getComponentWidth());
            config.setComponentWidth(config.getPanelWidth());
        }
        if (config.getLabelWidth() == 0) {
            config.setLabelWidth(config.getPanelWidth() - 20);
        }

        Map<Class<? extends JComponent>, Supplier<JComponent>> builderByType = Map.of(JComboCheckBox.class,
                this::buildJComboCheckBox, JComboBoxInput.class, this::buildJComboBoxInput, MyInputText.class,
                this::buildMyInputText, JComboBox.class, this::buildJComboBox, JCheckBox.class, this::buildJCheckBox,
                MyInputRange.class, this::buildMyInputRange, JTextField.class, this::buildJTextField, JLabel.class,
                this::buildJLabel);

        return (T) builderByType.getOrDefault(config.getType(), () -> {
            throw new IllegalArgumentException("Incorrect component type: " + config.getType().getName());
        }).get();
    }

    /**
     * Creates a JComboCheckBox and the layout around it.
     * A list with multiples selectable choices
     *
     * @return the component built
     */
    @SuppressWarnings("unchecked")
    private JComboCheckBox buildJComboCheckBox() {
        requiredValues();
        JComboCheckBox box = new JComboCheckBox((List<String>) Arrays.asList(config.getValues()));
        box.setPreferredSize(new Dimension(config.getComponentWidth() - JComboBoxInput.COMBO_BOX_WIDTH, COMPONENT_HEIGHT));
        buildJLabel(config.getLabel(), config.getLabelWidth()).ifPresent(label -> {
            JPanel boxPanel = buildComponentPanel();
            boxPanel.add(label);
            boxPanel.add(box);
        });
        return box;
    }

    /**
     * Creates a JComboBoxInput and the layout around it.
     * An input text with a combo box
     *
     * @return the component built
     */
    private JComboBoxInput<V> buildJComboBoxInput() {
        requiredValues();
        JComboBoxInput<V> input = new JComboBoxInput<>(
                new MyInputText(JTextField.class, config.getComponentWidth() - JComboBoxInput.COMBO_BOX_WIDTH * 2),
                new JComboBox<>(config.getValues()));
        buildJLabel(config.getLabel(), config.getLabelWidth()).ifPresent(label -> {
            JPanel panel = buildComponentPanel();
            panel.add(label);
            panel.add(input);
        });
        return input;
    }

    /**
     * Creates a MyInputText and the layout around it.
     * An input text with suggestions
     *
     * @return the component built
     */
    @SuppressWarnings("unchecked")
    private MyInputText buildMyInputText() {
        MyInputText input;
        if (ArrayUtils.isNotEmpty(config.getValues()) || config.getAsyncValues() != null) {
            input = new MyInputText(JComboBox.class, config.getComponentWidth() - 5);
            EventList<V> values = GlazedLists.eventListOf(config.getValues());
            AutoCompleteSupport<V> install = AutoCompleteSupport.install((JComboBox<V>) input.getInput(), values);
            if (config.isFilterContains()) {
                install.setFilterMode(TextMatcherEditor.CONTAINS);
            }
            config.getAsync()
            .whenCompleteAsync((v, e) -> values.addAll(GlazedLists.eventListOf(config.getAsyncValues().get())));
        } else {
            input = new MyInputText(JTextField.class, config.getComponentWidth() - 5);
        }
        buildJLabel(config.getLabel(), config.getLabelWidth()).ifPresent(label -> {
            JPanel inputPanel = buildComponentPanel();
            inputPanel.add(label);
            inputPanel.add(input);
        });
        return input;
    }

    /**
     * Creates a JComboBox and the layout around it.
     * A list of value with one selectable item
     *
     * @return the component built
     */
    private JComboBox<V> buildJComboBox() {
        requiredValues();
        JComboBox<V> box = new JComboBox<>(config.getValues());
        PanelUtils.setSize(box, config.getComponentWidth() - JComboBoxInput.COMBO_BOX_WIDTH, ComponentBuilder.COMPONENT_HEIGHT);
        if (config.getInitialValue() != null) {
            box.setSelectedItem(config.getInitialValue());
        }
        buildJLabel(config.getLabel(), config.getLabelWidth()).ifPresent(label -> {
            JPanel panel = buildComponentPanel();
            panel.add(label);
            panel.add(box);
        });
        return box;
    }

    /**
     * Builds a JCheckBox component.
     * True / false
     *
     * @return the JCheckBox built
     */
    private JCheckBox buildJCheckBox() {
        JCheckBox checkBox = new JCheckBox();
        PanelUtils.setSize(checkBox, config.getComponentWidth() / 2D, ComponentBuilder.COMPONENT_HEIGHT);
        if (config.getInitialValue() != null) {
            checkBox.setSelected((Boolean) config.getInitialValue());
        }
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        buildJLabel(config.getLabel(), config.getLabelWidth()).ifPresent(label -> {
            JPanel panel = buildComponentPanel();
            if(!config.isFlowLayout()) {
                panel.setLayout(new BorderLayout());
            }
            panel.add(label, BorderLayout.PAGE_START);
            panel.add(checkBox);
        });
        return checkBox;
    }

    /**
     * Builds a MyInputRange component.
     * Two inputs together
     *
     * @return the MyInputRange built
     */
    private MyInputRange buildMyInputRange() {
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
        buildJLabel(config.getLabel(), config.getLabelWidth() * 2).ifPresent(label -> {
            JPanel rangePanel = buildComponentPanel();
            rangePanel.add(label);
            rangePanel.add(range.getFirst());
            rangePanel.add(range.getSecond());
        });
        return range;
    }

    private JTextField buildJTextField() {
        JTextField text = new JTextField((String) config.getInitialValue());
        PanelUtils.setSize(text, config.getComponentWidth(), COMPONENT_HEIGHT);
        buildJLabel(config.getLabel(), config.getLabelWidth()).ifPresent(label -> {
            JPanel panel = buildComponentPanel();
            panel.add(label);
            panel.add(text);
        });
        return text;
    }

    private JLabel buildJLabel() {
        JLabel label = new JLabel(config.getLabel(), SwingConstants.CENTER);
        PanelUtils.setSize(label, config.getLabelWidth(), config.getHeight());
        label.setForeground(config.getColor());
        if (config.getFontSize() > 0) {
            Font labelFont = label.getFont();
            label.setFont(new Font(labelFont.getName(), labelFont.getStyle(), config.getFontSize()));
        }
        label.setVerticalAlignment(SwingConstants.CENTER);
        config.getParent().add(label);
        PanelUtils.setBorder(label, Color.green);
        return label;
    }

    /**
     * Creates a JButton.
     *
     * @param label label of the button
     * @param width width of the button
     * @param icon icon of the button
     * @return the button created
     */
    public static JButton buildJButton(String label, int width, FontAwesome icon) {
        JButton btn = new JButton(label);
        btn.setBackground(Color.white);
        btn.setPreferredSize(new Dimension(width, PANEL_HEIGHT));
        CompletableFuture.supplyAsync(() -> FontIcon.of(icon))
        .whenCompleteAsync((fontIcon, e) -> btn.setIcon(fontIcon));
        return btn;
    }

    /**
     * Creates a {@link JLabel}.
     *
     * @param text init text
     * @param width the width of the label
     * @return the label created, height is depending of the length of the given
     *         text
     */
    public static Optional<JLabel> buildJLabel(String text, int width) {
        Optional<JLabel> result;
        if (StringUtils.isBlank(text)) {
            result = Optional.empty();
        } else {
            JLabel jLabel = new JLabel(text, SwingConstants.CENTER);
            PanelUtils.setSize(jLabel, width, COMPONENT_HEIGHT);
            PanelUtils.setBorder(jLabel, Color.pink);
            result = Optional.of(jLabel);
        }
        return result;
    }

    /**
     * Build and add to the given menu a {@link JMenuItem}.
     *
     * @param menu the menu where the item will be added
     * @param text text of the item
     * @param shortcut {@link KeyEvent} constant
     * @param action {@link ActionListener} of the item
     * @param keyStroke {@link ActionEvent} constant, if null set
     *        {@code ActionEvent.CTRL_MASK}
     */
    public static void buildMenuItem(JComponent menu, String text, int shortcut, ActionListener action,
            Integer keyStroke) {
        JMenuItem item = new JMenuItem(text);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut, keyStroke == null ? ActionEvent.CTRL_MASK : keyStroke));
        item.addActionListener(action);
        menu.add(item);
    }

    /**
     * Creates a JTextPane.
     *
     * @param color the font color
     * @param fontSize the font size
     * @return a JTextPane initialized with properties: editable -> false, opaque ->
     *         false, default value for background, font family and border, text
     *         align center
     */
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
        PanelUtils.setBorder(panel, Color.green);
        PanelUtils.setBorder(config.getParent(), Color.BLUE);
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
    public ComponentBuilder<T, V> withParent(JPanel parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent panel is required");
        }
        config.setParent(parent);
        return this;
    }

    /**
     * Configures the values of the component.
     *
     * @param values a list of string
     * @return the builder
     */
    public ComponentBuilder<T, V> withValues(V[] values) {
        if (!List.of(JComboCheckBox.class, JComboBoxInput.class, MyInputText.class, JComboBox.class)
                .contains(config.getType())) {
            throw new IllegalArgumentException(config.getType().getName() + " must not use the property Values");
        }
        config.setValues(values);
        return this;
    }

    /**
     * Configures the asynchronous values of the component.
     *
     * @param asyncValues supplier for an array of item
     * @param async the {@link CompletableFuture} that compute future values
     * @return the builder
     */
    public ComponentBuilder<T, V> withAsyncValues(Supplier<V[]> asyncValues, CompletableFuture<Void> async) {
        if (!List.of(JComboCheckBox.class, JComboBoxInput.class, MyInputText.class, JComboBox.class)
                .contains(config.getType())) {
            throw new IllegalArgumentException(config.getType().getName() + " must not use the property AsyncValues");
        }
        config.setAsync(async);
        config.setAsyncValues(asyncValues);
        return this;
    }

    /**
     * Configures the label value.
     *
     * @param label a string
     * @return the builder
     */
    public ComponentBuilder<T, V> withLabel(String label) {
        config.setLabel(label);
        return this;
    }

    /**
     * <p>
     * Configures the panel to be created with a FlowLayout:
     * </p>
     * <code>new FlowLayout(FlowLayout.CENTER, 0, 0)</code>
     *
     * @return the builder
     */
    public ComponentBuilder<T, V> withFlowLayout() {
        config.setFlowLayout(true);
        return this;
    }

    /**
     * Configures if the filter mode will be contains. Only for MyInputText
     * component.
     *
     * @param isFilterContains set filer mode to contains if true
     * @return the builder
     */
    public ComponentBuilder<T, V> withFilterContains(boolean isFilterContains) {
        if (!config.getType().equals(MyInputText.class)) {
            throw new IllegalArgumentException(
                    config.getType().getName() + " must not use the property Filter Contains");
        }
        config.setFilterContains(isFilterContains);
        return this;
    }

    /**
     * Configures {@link Resize}.
     *
     * @param resize a resize instance
     * @return the builder
     */
    public ComponentBuilder<T, V> withResize(Resize resize) {
        config.setResize(resize);
        return this;
    }

    /**
     * Configures panel width.
     *
     * @param panelWidth an int
     * @return the builder
     */
    public ComponentBuilder<T, V> withPanelWidth(int panelWidth) {
        config.setPanelWidth(panelWidth);
        config.setLabelWidth(panelWidth - 20);
        return this;
    }

    /**
     * Configures component width.
     *
     * @param componentWidth and int
     * @return the builder
     */
    public ComponentBuilder<T, V> withComponentWidth(int componentWidth) {
        if (config.getType().equals(JLabel.class)) {
            throw new IllegalArgumentException(
                    config.getType().getName() + " must not use the property Component Width");
        }
        config.setComponentWidth(componentWidth);
        return this;
    }

    /**
     * Configures panel height.
     *
     * @param height an int
     * @return the builder
     */
    public ComponentBuilder<T, V> withHeight(int height) {
        config.setHeight(height);
        return this;
    }

    /**
     * Configures font size.
     *
     * @param fontSize an int
     * @return the builder
     */
    public ComponentBuilder<T, V> withFontSize(int fontSize) {
        if (!config.getType().equals(JLabel.class)) {
            throw new IllegalArgumentException(config.getType().getName() + " must not use the property Font Size");
        }
        config.setFontSize(fontSize);
        return this;
    }

    /**
     * Configures color.
     *
     * @param color a Color
     * @return the builder
     */
    public ComponentBuilder<T, V> withColor(Color color) {
        if (!config.getType().equals(JLabel.class)) {
            throw new IllegalArgumentException(config.getType().getName() + " must not use the property Color");
        }
        config.setColor(color);
        return this;
    }

    /**
     * Configures initial value.
     *
     * @param initialValue the initial value
     * @return the builder
     */
    public ComponentBuilder<T, V> withInitialValue(V initialValue) {
        if (!List.of(JCheckBox.class, JComboBox.class, JTextField.class, MyInputRange.class)
                .contains(config.getType())) {
            throw new IllegalArgumentException(config.getType().getName() + " must not use the property Initial Value");
        }
        config.setInitialValue(initialValue);
        return this;
    }
}
