# Java Programming Assignment 4

**Student** R SARVESH
**Register number** 192521143
**Course** Programming in Java (CSA09), CO4
**Runtime used** Oracle JDK 21.0.12.1 on Windows

Run each command from the named task folder. Use a graphical desktop for all six tasks. Task 6 also requires a running MySQL server.

| Folder | Compile | Run | Result |
| --- | --- | --- | --- |
| Task01 | `javac FeeWindow.java` | `java FeeWindow` | Validates the fee, records it for the session and displays a receipt. |
| Task02 | `javac AppointmentDesk.java` | `java AppointmentDesk` | Refreshes the doctor list, books a free slot and confirms closing an edited draft. |
| Task03 | `javac LibrarySearch.java` | `java LibrarySearch` | Searches and pages through titles. The results pane expands at three tested sizes. |
| Task04 | `javac DrawPad.java` | `java DrawPad` | Five click marks and their count survive minimising and restoring. |
| Task05 | `javac *.java` | `java FrameConverter`, `java NamedConverter`, `java AnonymousConverter`, `java LambdaConverter`, or `java Converter` | Four listener styles accept Enter and display a dialog. The completed template displays valid and invalid results in its label. |
| Task06 | `javac *.java` | See MySQL setup below. | Registers a valid student, rejects invalid and duplicate submissions, filters and exports records. |

Append `--baseline` to the first four Task05 run commands for button-only conversion with a label result. Try 1000 INR to USD for 12.00 USD. Run `java Converter` separately for the supplied lambda template.

## Task 6 MySQL setup

1. Start MySQL and run `source schema.sql` from the MySQL client while in Task06. The script creates the database, tables, and sample events.
2. Obtain [MySQL Connector/J 8.4.0](https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/8.4.0/) and put the JAR in Task06 for the run command. Set your MySQL account credentials through environment variables.
3. Compile and run in PowerShell.

```powershell
$env:EVENT_DB_URL = 'jdbc:mysql://localhost:3306/campus_events?useCursorFetch=true'
$env:EVENT_DB_USER = 'your_mysql_user'
$env:EVENT_DB_PASSWORD = 'your_mysql_password'
javac *.java
java -cp '.;mysql-connector-j-8.4.0.jar' CampusRegistration
```

The app account needs SELECT on both tables and INSERT on registrations. For the demonstrated sequence, register 192521143 with the name Asha Student for Coding Sprint, try ABC as the ID, then retry 192521143 for Coding Sprint. In Registrations, select Coding Sprint and click Apply filter. File → Export CSV saves all rows matching the applied filter. The screenshots capture a fresh database.
