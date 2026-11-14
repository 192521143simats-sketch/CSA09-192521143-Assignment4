CREATE DATABASE IF NOT EXISTS campus_events CHARACTER SET utf8mb4;
USE campus_events;

CREATE TABLE IF NOT EXISTS events (
    id INT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

INSERT IGNORE INTO events (id, name) VALUES
    (1, 'Coding Sprint'), (2, 'Music Showcase'), (3, 'Photography Walk');

CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(9) NOT NULL,
    student_name VARCHAR(80) NOT NULL,
    event_id INT NOT NULL,
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT uq_student_event UNIQUE (student_id, event_id),
    INDEX ix_event_page (event_id, id)
);
