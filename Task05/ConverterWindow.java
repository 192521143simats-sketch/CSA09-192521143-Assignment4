import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.*;

abstract class ConverterWindow extends JFrame {
    protected final JTextField amount = new JTextField(12);
    protected final JComboBox<String> from = new JComboBox<>(new String[]{"INR", "USD", "EUR"});
    protected final JComboBox<String> to = new JComboBox<>(new String[]{"USD", "EUR", "INR"});
    protected final JButton button = new JButton("Convert");
    private final JLabel result = new JLabel("Enter an amount.");
    private final boolean revised;

    ConverterWindow(String style, boolean revised) {
        super("Currency Converter - " + style);
        this.revised = revised;
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        form.add(new JLabel("Amount")); form.add(amount);
        form.add(new JLabel("From")); form.add(from);
        form.add(new JLabel("To")); form.add(to); form.add(button);
        result.setBorder(BorderFactory.createEmptyBorder(4, 10, 12, 10));
        add(form); add(result, BorderLayout.SOUTH);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        pack(); setLocationRelativeTo(null);
    }

    protected void convert() {
        String message;
        boolean valid;
        try {
            BigDecimal value = new BigDecimal(amount.getText().strip());
            if (value.signum() < 0 || value.scale() > 12 || value.precision() > 20
                    || value.compareTo(new BigDecimal("1000000000000")) > 0)
                throw new NumberFormatException();
            String source = (String) from.getSelectedItem();
            String target = (String) to.getSelectedItem();
            BigDecimal converted = value.multiply(rate(target)).divide(rate(source), 2, RoundingMode.HALF_UP);
            message = converted.toPlainString() + " " + target;
            valid = true;
        } catch (NumberFormatException ex) {
            message = "Invalid amount";
            valid = false;
        }
        result.setText(message);
        if (revised) JOptionPane.showMessageDialog(this, message, "Conversion result",
                valid ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private static BigDecimal rate(String currency) {
        return new BigDecimal(switch (currency) {
            case "USD" -> "0.012";
            case "EUR" -> "0.011";
            default -> "1";
        });
    }

    protected static void launch(String[] args, java.util.function.Function<Boolean, ConverterWindow> factory) {
        boolean revised = args.length == 0 || !args[0].equals("--baseline");
        SwingUtilities.invokeLater(() -> factory.apply(revised).setVisible(true));
    }
}
