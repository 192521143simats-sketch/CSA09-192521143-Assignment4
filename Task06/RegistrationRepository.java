import java.sql.*;
import java.util.ArrayList;
import java.util.List;

final class RegistrationRepository {
    record Event(int id, String name) {
        @Override public String toString() { return name; }
    }
    record Registration(long id, String studentId, String name, String event) {}

    private Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("EVENT_DB_URL", "jdbc:mysql://localhost:3306/campus_events");
        String user = System.getenv().getOrDefault("EVENT_DB_USER", "root");
        return DriverManager.getConnection(url, user, System.getenv().getOrDefault("EVENT_DB_PASSWORD", ""));
    }

    List<Event> events() throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection db = connect();
             PreparedStatement statement = db.prepareStatement("SELECT id, name FROM events ORDER BY name");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) events.add(new Event(rows.getInt("id"), rows.getString("name")));
        }
        return events;
    }

    void insert(String id, String name, int event) throws SQLException {
        String sql = "INSERT INTO registrations (student_id, student_name, event_id) VALUES (?, ?, ?)";
        try (Connection db = connect(); PreparedStatement statement = db.prepareStatement(sql)) {
            statement.setString(1, id); statement.setString(2, name); statement.setInt(3, event);
            statement.executeUpdate();
        }
    }

    List<Registration> page(Integer event, int limit, long offset) throws SQLException {
        String sql = """
                SELECT r.id, r.student_id, r.student_name, e.name
                FROM registrations r JOIN events e ON r.event_id = e.id
                """ + (event == null ? "" : " WHERE r.event_id = ?")
                + " ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection db = connect(); PreparedStatement statement = db.prepareStatement(sql)) {
            int index = 1;
            if (event != null) statement.setInt(index++, event);
            statement.setInt(index++, limit); statement.setLong(index, offset);
            try (ResultSet rows = statement.executeQuery()) {
                List<Registration> page = new ArrayList<>();
                while (rows.next()) page.add(new Registration(rows.getLong(1), rows.getString(2),
                        rows.getString(3), rows.getString(4)));
                return page;
            }
        }
    }

    void export(Integer event, java.io.BufferedWriter out) throws SQLException, java.io.IOException {
        String sql = """
                SELECT r.id, r.student_id, r.student_name, e.name
                FROM registrations r JOIN events e ON r.event_id = e.id
                """ + (event == null ? "" : " WHERE r.event_id = ?") + " ORDER BY r.id";
        try (Connection db = connect(); PreparedStatement statement = db.prepareStatement(sql)) {
            statement.setFetchSize(256);
            if (event != null) statement.setInt(1, event);
            try (ResultSet rows = statement.executeQuery()) {
                out.write("Registration ID,Student ID,Student Name,Event\r\n");
                while (rows.next()) {
                    out.write(rows.getLong(1) + "," + csv(rows.getString(2)) + ","
                            + csv(rows.getString(3)) + "," + csv(rows.getString(4)) + "\r\n");
                }
            }
        }
    }

    private String csv(String value) {
        if (!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0) value = "'" + value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
