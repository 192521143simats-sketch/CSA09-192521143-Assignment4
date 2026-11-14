import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CampusRegistration extends JFrame {
    private static final int PAGE_SIZE = 25;
    private final RegistrationService service = new RegistrationService();
    private final JTextField studentId = new JTextField(18), studentName = new JTextField(18);
    private final JComboBox<RegistrationRepository.Event> event = new JComboBox<>();
    private final JComboBox<RegistrationRepository.Event> filter = new JComboBox<>();
    private final JButton register = new JButton("Register"), apply = new JButton("Apply filter");
    private final JButton prev = new JButton("Prev"), next = new JButton("Next");
    private final JMenuItem export = new JMenuItem("Export CSV...");
    private final JLabel status = new JLabel("Loading events...");
    private final JLabel page = new JLabel("Page 1");
    private final JTabbedPane tabs = new JTabbedPane();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Registration", "Student ID", "Student Name", "Event"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private RegistrationRepository.Event activeFilter;
    private int pageIndex;
    private boolean busy, more, ready;

    CampusRegistration() {
        super("Campus Event Registration");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File"), view = new JMenu("View");
        JMenuItem exit = new JMenuItem("Exit"), formTab = new JMenuItem("Register");
        JMenuItem listTab = new JMenuItem("Registrations");
        file.add(export); file.addSeparator(); file.add(exit);
        view.add(formTab); view.add(listTab);
        bar.add(file); bar.add(view); setJMenuBar(bar);
        exit.addActionListener(e -> dispose());
        formTab.addActionListener(e -> tabs.setSelectedIndex(0));
        listTab.addActionListener(e -> tabs.setSelectedIndex(1));
        export.addActionListener(e -> export());

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 10));
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        form.add(new JLabel("Student ID")); form.add(studentId);
        form.add(new JLabel("Student name")); form.add(studentName);
        form.add(new JLabel("Event")); form.add(event);
        form.add(new JLabel()); form.add(register);
        JPanel registerScreen = new JPanel(new BorderLayout());
        registerScreen.add(form, BorderLayout.NORTH);
        tabs.addTab("Register", registerScreen);

        JPanel list = new JPanel(new BorderLayout(8, 8));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Event")); controls.add(filter); controls.add(apply);
        JPanel navigation = new JPanel();
        navigation.add(prev); navigation.add(page); navigation.add(next);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        list.add(controls, BorderLayout.NORTH); list.add(new JScrollPane(table)); list.add(navigation, BorderLayout.SOUTH);
        tabs.addTab("Registrations", list);
        add(tabs);
        status.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        add(status, BorderLayout.SOUTH);
        register.addActionListener(e -> register());
        apply.addActionListener(e -> load(selectedFilter(), 0));
        prev.addActionListener(e -> load(activeFilter, pageIndex - 1));
        next.addActionListener(e -> load(activeFilter, pageIndex + 1));
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1 && !busy && ready) load(activeFilter, pageIndex);
        });
        setMinimumSize(new Dimension(610, 390)); setSize(720, 490); setLocationRelativeTo(null);
        run("Loading events...", service::events, events -> {
            filter.addItem(new RegistrationRepository.Event(0, "All events"));
            for (RegistrationRepository.Event item : events) { filter.addItem(item); event.addItem(item); }
            ready = true; status.setText("Ready.");
            if (tabs.getSelectedIndex() == 1) load(null, 0);
        });
    }

    private RegistrationRepository.Event selectedFilter() {
        RegistrationRepository.Event selection = (RegistrationRepository.Event) filter.getSelectedItem();
        return selection == null || selection.id() == 0 ? null : selection;
    }

    private void register() {
        String id = studentId.getText(), name = studentName.getText();
        var selection = (RegistrationRepository.Event) event.getSelectedItem();
        run("Saving registration...", () -> { service.register(id, name, selection); return id.strip(); }, saved -> {
            status.setText("Registered " + saved + " for " + selection.name() + ".");
            JOptionPane.showMessageDialog(this, status.getText(), "Registration confirmed", JOptionPane.INFORMATION_MESSAGE);
            if (tabs.getSelectedIndex() == 1) load(activeFilter, pageIndex);
        });
    }

    private void load(RegistrationRepository.Event selection, int index) {
        run("Loading registrations...", () -> service.page(selection, PAGE_SIZE + 1, (long) index * PAGE_SIZE),
                rows -> showPage(selection, index, rows));
    }

    private void showPage(RegistrationRepository.Event selection, int index, List<RegistrationRepository.Registration> rows) {
        activeFilter = selection; pageIndex = index; more = rows.size() > PAGE_SIZE;
        model.setRowCount(0);
        for (var row : rows.subList(0, Math.min(PAGE_SIZE, rows.size())))
            model.addRow(new Object[]{row.id(), row.studentId(), row.name(), row.event()});
        page.setText("Page " + (index + 1));
        status.setText(model.getRowCount() + " registrations on this page.");
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path destination = chooser.getSelectedFile().toPath();
        if (java.nio.file.Files.exists(destination) && JOptionPane.showConfirmDialog(this,
                "Replace the existing file?", "Export CSV", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        var selection = activeFilter;
        run("Exporting registrations...", () -> { service.export(selection, destination); return destination; }, path -> {
            status.setText("Exported " + path.getFileName() + ".");
            JOptionPane.showMessageDialog(this, "Saved " + path.getFileName(), "Export complete", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private <T> void run(String message, Callable<T> task, Consumer<T> success) {
        busy = true; updateControls(); status.setText(message);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.call(); }

            @Override protected void done() {
                busy = false;
                try {
                    T result = get();
                    if (isDisplayable()) success.accept(result);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (!(cause instanceof IllegalArgumentException)) cause.printStackTrace();
                    showError(cause instanceof IllegalArgumentException ? cause.getMessage()
                            : "Operation failed. Check the database connection or export destination.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Operation interrupted.");
                } finally {
                    updateControls();
                }
            }
        }.execute();
    }

    private void showError(String message) {
        status.setText(message);
        if (isDisplayable()) JOptionPane.showMessageDialog(this, message, "Registration desk", JOptionPane.ERROR_MESSAGE);
    }

    private void updateControls() {
        register.setEnabled(ready && !busy); apply.setEnabled(ready && !busy);
        export.setEnabled(ready && !busy); prev.setEnabled(!busy && pageIndex > 0); next.setEnabled(!busy && more);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CampusRegistration().setVisible(true));
    }
}
