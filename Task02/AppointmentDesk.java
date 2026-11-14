import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class AppointmentDesk extends JFrame {
    private final JTextField patient = new JTextField(12);
    private final Map<String, java.util.List<String>> directory = Map.of(
            "Cardiology", java.util.List.of("Dr Iyer", "Dr Rao"),
            "Dermatology", java.util.List.of("Dr Shah", "Dr Menon"),
            "General Medicine", java.util.List.of("Dr Kumar", "Dr Ali"));
    private final JComboBox<String> departments = new JComboBox<>(
            new String[]{"Cardiology", "Dermatology", "General Medicine"});
    private final JComboBox<String> doctors = new JComboBox<>();
    private final JComboBox<String> slots = new JComboBox<>(new String[]{"09:00", "09:30", "10:00"});
    private final JLabel status = new JLabel("Enter a patient ID to start.");
    private final Set<Slot> booked = new HashSet<>();
    private boolean dirty;
    private record Slot(String department, String doctor, String time) {}

    AppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));
        form.add(new JLabel("Patient ID")); form.add(patient);
        form.add(new JLabel("Department")); form.add(departments);
        form.add(new JLabel("Doctor")); form.add(doctors);
        form.add(new JLabel("Time slot")); form.add(slots);
        add(form);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        JButton book = new JButton("Book appointment");
        footer.add(book, BorderLayout.NORTH); footer.add(status, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        refreshDoctors();
        departments.addActionListener(e -> { refreshDoctors(); dirty = true; });
        doctors.addActionListener(e -> dirty = true);
        slots.addActionListener(e -> dirty = true);
        patient.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { dirty = true; }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { dirty = true; }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { dirty = true; }
        });
        book.addActionListener(e -> book());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (!dirty || JOptionPane.showConfirmDialog(AppointmentDesk.this,
                        "Discard the booking in progress?", "Close appointment desk",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose();
            }
        });
        pack(); setMinimumSize(new Dimension(480, 250)); setSize(500, 270);
        setLocationRelativeTo(null);
    }

    private void refreshDoctors() {
        doctors.setModel(new DefaultComboBoxModel<>(
                directory.get(departments.getSelectedItem()).toArray(String[]::new)));
    }

    private void book() {
        String id = patient.getText().strip();
        if (!id.matches("[A-Za-z0-9]{3,20}")) {
            status.setText("Error: enter a 3-20 character alphanumeric patient ID.");
            return;
        }
        Slot slot = new Slot((String) departments.getSelectedItem(),
                (String) doctors.getSelectedItem(), (String) slots.getSelectedItem());
        if (!booked.add(slot)) {
            status.setText("Error: that doctor and time slot are already booked.");
            return;
        }
        status.setText("Booked " + id + " with " + slot.doctor() + " at " + slot.time() + ".");
        dirty = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true));
    }
}
