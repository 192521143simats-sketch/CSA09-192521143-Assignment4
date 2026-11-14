import java.awt.event.*;

public class AnonymousConverter extends ConverterWindow {
    AnonymousConverter(boolean revised) {
        super("Anonymous inner class", revised);
        ActionListener handler = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { convert(); }
        };
        button.addActionListener(handler);
        if (revised) amount.addActionListener(handler);
    }

    public static void main(String[] args) { launch(args, AnonymousConverter::new); }
}
