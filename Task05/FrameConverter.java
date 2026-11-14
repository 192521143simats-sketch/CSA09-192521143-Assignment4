import java.awt.event.*;

public class FrameConverter extends ConverterWindow implements ActionListener {
    FrameConverter(boolean revised) {
        super("Frame listener", revised);
        ActionListener handler = this;
        button.addActionListener(handler);
        if (revised) amount.addActionListener(handler);
    }

    @Override public void actionPerformed(ActionEvent e) { convert(); }
    public static void main(String[] args) { launch(args, FrameConverter::new); }
}
