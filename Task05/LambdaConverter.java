import java.awt.event.*;

public class LambdaConverter extends ConverterWindow {
    LambdaConverter(boolean revised) {
        super("Lambda listener", revised);
        ActionListener handler = e -> convert();
        button.addActionListener(handler);
        if (revised) amount.addActionListener(handler);
    }

    public static void main(String[] args) { launch(args, LambdaConverter::new); }
}
