import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;

public class FeeWindow extends JFrame {
    private final JTextField registration = new JTextField(12);
    private final JTextField amount = new JTextField(10);
    private final JLabel status = new JLabel("Enter a registration number and fee.");
    private final JButton pay = new JButton("Pay");
    private final JButton clear = new JButton("Clear");
    private final JButton receipt = new JButton("Receipt");
    private final FeeService service = new FeeService();
    private FeeService.Payment lastPayment;

    FeeWindow() {
        super("Student Fee Payment");
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        form.add(new JLabel("Registration")); form.add(registration);
        form.add(new JLabel("Amount (INR)")); form.add(amount);
        JPanel actions = new JPanel();
        for (JButton button : new JButton[]{pay, clear, receipt}) actions.add(button);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(actions, BorderLayout.NORTH);
        status.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bottom.add(status, BorderLayout.SOUTH);
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        pay.addActionListener(event -> pay());
        clear.addActionListener(event -> {
            registration.setText(""); amount.setText("");
            status.setText("Fields cleared."); registration.requestFocusInWindow();
        });
        receipt.addActionListener(event -> JOptionPane.showMessageDialog(this,
                lastPayment == null ? "No payment recorded." : lastPayment.summary(),
                "Receipt", JOptionPane.INFORMATION_MESSAGE));
        getRootPane().setDefaultButton(pay);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void pay() {
        String id = registration.getText();
        String value = amount.getText();
        pay.setEnabled(false); clear.setEnabled(false); receipt.setEnabled(false);
        status.setText("Recording payment...");
        new SwingWorker<FeeService.Payment, Void>() {
            @Override protected FeeService.Payment doInBackground() {
                return service.pay(id, value);
            }
            @Override protected void done() {
                try {
                    lastPayment = get();
                    status.setText("Paid. " + lastPayment.summary());
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    status.setText(cause instanceof IllegalArgumentException
                            ? cause.getMessage() : "Payment could not be recorded.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    status.setText("Payment interrupted.");
                } finally {
                    pay.setEnabled(true); clear.setEnabled(true); receipt.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}

final class FeeService {
    record Payment(int number, String registration, BigDecimal amount) {
        String summary() { return "Receipt " + number + " | " + registration + " | INR " + amount; }
    }

    private final java.util.List<Payment> payments = new ArrayList<>();

    synchronized Payment pay(String registration, String rawAmount) {
        String id = registration.strip().toUpperCase(Locale.ROOT);
        if (!id.matches("[A-Z0-9]{4,20}"))
            throw new IllegalArgumentException("Enter a 4-20 character alphanumeric registration number.");
        String value = rawAmount.strip();
        if (!value.matches("[0-9]{1,9}(\\.[0-9]{1,2})?") || new BigDecimal(value).signum() <= 0)
            throw new IllegalArgumentException("Enter a positive fee with up to two decimal places.");
        Payment payment = new Payment(payments.size() + 1, id, new BigDecimal(value).setScale(2));
        payments.add(payment);
        return payment;
    }
}
