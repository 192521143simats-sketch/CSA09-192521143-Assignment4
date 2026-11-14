import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class LibrarySearch extends Frame {
    private record Book(String title, String category) {}
    private final List<Book> catalog = List.of(new Book("Effective Java", "Programming"),
            new Book("Head First Java", "Programming"), new Book("Clean Code", "Programming"),
            new Book("Database System Concepts", "Databases"), new Book("SQL for Data Analysis", "Databases"),
            new Book("The Design of Everyday Things", "Design"), new Book("Universal Principles of Design", "Design"));
    private final TextField query = new TextField(22);
    private final Choice category = new Choice();
    private final java.awt.List results = new java.awt.List();
    private final Label status = new Label();
    private final Button prev = new Button("Prev"), next = new Button("Next"), reserve = new Button("Reserve");
    private final Set<Book> reservations = new HashSet<>();
    private List<Book> matches = List.of();
    private int page;
    private static final int PAGE_SIZE = 4;

    LibrarySearch() {
        super("Campus Library Search");
        Label title = new Label("Campus Library Search", Label.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        Panel header = new Panel(new BorderLayout());
        header.add(title, BorderLayout.NORTH);
        Panel search = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 9));
        for (String item : List.of("All categories", "Programming", "Databases", "Design")) category.add(item);
        Button find = new Button("Search");
        search.add(query); search.add(category); search.add(find);
        header.add(search, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(results, BorderLayout.CENTER);
        Panel footer = new Panel(new BorderLayout());
        Panel buttons = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(prev); buttons.add(next); buttons.add(reserve);
        footer.add(buttons, BorderLayout.NORTH); footer.add(status, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        find.addActionListener(e -> search()); query.addActionListener(e -> search());
        prev.addActionListener(e -> { page--; showPage(); });
        next.addActionListener(e -> { page++; showPage(); });
        reserve.addActionListener(e -> {
            int row = results.getSelectedIndex();
            if (row < 0) { status.setText(" Select a book to reserve."); return; }
            Book book = matches.get(page * PAGE_SIZE + row);
            status.setText((reservations.add(book) ? " Reserved: " : " Already reserved: ") + book.title());
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setMinimumSize(new Dimension(490, 260)); setSize(700, 460);
        setLocationRelativeTo(null); search();
    }

    private void search() {
        String text = query.getText().strip().toLowerCase(Locale.ROOT);
        String selected = category.getSelectedItem();
        matches = catalog.stream().filter(book -> book.title().toLowerCase(Locale.ROOT).contains(text))
                .filter(book -> selected.equals("All categories") || selected.equals(book.category())).toList();
        page = 0; showPage();
    }

    private void showPage() {
        results.removeAll();
        int start = page * PAGE_SIZE;
        for (int i = start; i < Math.min(matches.size(), start + PAGE_SIZE); i++) {
            Book book = matches.get(i);
            results.add(book.title() + "  |  " + book.category());
        }
        prev.setEnabled(page > 0);
        next.setEnabled(start + PAGE_SIZE < matches.size());
        reserve.setEnabled(!matches.isEmpty());
        status.setText(matches.isEmpty() ? " No matching books."
                : " " + matches.size() + " results  |  Page " + (page + 1)
                + " of " + ((matches.size() + PAGE_SIZE - 1) / PAGE_SIZE));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new LibrarySearch().setVisible(true));
    }
}
