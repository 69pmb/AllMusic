package pmb.music.AllMusic.view;

import java.awt.Toolkit;

public class Resize {
    int componentWidth;
    int panelWidth;
    int count;

    public Resize(int count) {
        super();
        this.count = count;
        this.panelWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / count);
        this.componentWidth = panelWidth - 15;
    }

    public Resize(int componentWidth, int panelWidth, int count) {
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

    public Resize addWidth(int width) {
        return new Resize(componentWidth + width, panelWidth + width, count);
    }

    public Resize addComponentWidth(int width) {
        return new Resize(componentWidth + width, panelWidth, count);
    }

    public Resize addPanelWidth(int width) {
        return new Resize(componentWidth, panelWidth + width, count);
    }
}
