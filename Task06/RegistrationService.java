import java.io.BufferedWriter;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

final class RegistrationService {
    private final RegistrationRepository repository = new RegistrationRepository();

    List<RegistrationRepository.Event> events() throws SQLException { return repository.events(); }

    void register(String rawId, String rawName, RegistrationRepository.Event event) throws SQLException {
        String id = rawId.strip();
        String name = rawName.strip();
        if (!id.matches("[0-9]{9}")) throw new IllegalArgumentException("Student ID must contain 9 digits.");
        if (name.length() > 80 || !name.matches("\\p{L}[\\p{L}\\p{M} .'-]*"))
            throw new IllegalArgumentException("Enter a student name of 1 to 80 characters.");
        if (event == null) throw new IllegalArgumentException("Select an event.");
        try {
            repository.insert(id, name, event.id());
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062)
                throw new IllegalArgumentException("This student is already registered for " + event.name() + ".");
            throw ex;
        }
    }

    List<RegistrationRepository.Registration> page(RegistrationRepository.Event event, int size, long offset)
            throws SQLException {
        return repository.page(event == null ? null : event.id(), size, offset);
    }

    void export(RegistrationRepository.Event event, Path destination) throws Exception {
        Path temp = Files.createTempFile(destination.toAbsolutePath().getParent(), ".registrations-", ".csv");
        try {
            try (BufferedWriter out = Files.newBufferedWriter(temp)) {
                repository.export(event == null ? null : event.id(), out);
            }
            Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
