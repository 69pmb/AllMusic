package pmb.music.AllMusic.view.component;

import javax.swing.JPanel;

/**
 * Custom component assembling two {@link MyInputText}. Usefull for range dates.
 */
public class MyInputRange extends JPanel {
    private static final long serialVersionUID = 3433759477031743510L;
    private MyInputText first;
    private MyInputText second;

    /**
     * Constructor of {@link MyInputRange}. Adds given {@link MyInputText} to the
     * created input range.
     * 
     * @param first a input text
     * @param second an other input text
     */
    public MyInputRange(MyInputText first, MyInputText second) {
        this.first = first;
        this.second = second;
        this.add(this.first);
        this.add(this.second);
    }

    public MyInputText getFirst() {
        return first;
    }

    public void setFirst(MyInputText first) {
        this.first = first;
    }

    public MyInputText getSecond() {
        return second;
    }

    public void setSecond(MyInputText second) {
        this.second = second;
    }
}
