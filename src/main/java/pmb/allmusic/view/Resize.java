package pmb.allmusic.view;

import java.awt.Toolkit;

/**
 * Settings class to use with {@link ComponentBuilder}. Sets components width
 * along with its panel and label.
 */
public class Resize {
    private int componentWidth;
    private int panelWidth;
    private int count;

    /**
     * Constructs a {@link Resize} by telling the number of wanted components by
     * given width.
     *
     * @param count number of components
     * @param width the width to divide
     */
    public Resize(int count, int width) {
        super();
        this.count = count;
        this.panelWidth = width / count - 10;
        this.componentWidth = panelWidth - 5;
    }

    /**
     * Constructs a {@link Resize} by telling the number of wanted components by
     * screen width.
     *
     * @param count number of components
     */
    public Resize(int count) {
        this(count, (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth());
    }

    private Resize(int componentWidth, int panelWidth, int count) {
        super();
        this.componentWidth = componentWidth;
        this.panelWidth = panelWidth;
        this.count = count;
    }

    public int getComponentWidth() {
        return componentWidth;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public int getCount() {
        return count;
    }

    /**
     * Adds given width to component & panel width.
     *
     * @param width to add, can be negative
     * @return a new instance of {@link Resize}
     */
    public Resize addWidth(int width) {
        return new Resize(componentWidth + width, panelWidth + width, count);
    }

    /**
     * Adds given width to component width.
     *
     * @param width to add, can be negative
     * @return a new instance of {@link Resize}
     */
    public Resize addComponentWidth(int width) {
        return new Resize(componentWidth + width, panelWidth, count);
    }

    /**
     * Adds given width to panel width.
     *
     * @param width to add, can be negative
     * @return a new instance of {@link Resize}
     */
    public Resize addPanelWidth(int width) {
        return new Resize(componentWidth, panelWidth + width, count);
    }
}
