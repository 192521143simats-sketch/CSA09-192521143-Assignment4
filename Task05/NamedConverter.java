import java.awt.event.*;

public class NamedConverter extends ConverterWindow {
    NamedConverter(boolean revised) {
        super("Named inner class", revised);
        ActionListener handler = new ConvertListener();
        button.addActionListener(handler);
        if (revised) amount.addActionListener(handler);
    }

    private final class ConvertListener implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) { convert(); }
    }
    public static void main(String[] args) { launch(args, NamedConverter::new); }
}
