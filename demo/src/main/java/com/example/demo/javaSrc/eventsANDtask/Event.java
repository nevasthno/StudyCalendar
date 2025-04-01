package eventsANDtask;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.io.*;

public class Event {
       enum EventType {
        EXAM, TEST, SCHOOL_EVENT, PARENTS_MEETING, PERSONAL
    }

    String title, content, locationOrLink, link;
    Task task;
    int duration;
    Date start;
    File file;
    EventType eventType;

    public Event(String title, Date start, String content, String locationOrLink, Task task, int duration, String link, File file, EventType eventType) {
        this.title = title;
        this.start = start;
        this.content = content;
        this.locationOrLink = locationOrLink;
        this.task = task;
        this.duration = duration;
        this.link = link;
        this.file = file;
        this.eventType = eventType;
    }

    public String getTitle() { return title; }
    public Date getStart() { return start; }
    public String getContent() { return content; }
    public String getLocationOrLink() { return locationOrLink; }
    public Task getTask() { return task; }
    public int getDuration() { return duration; }
    public String getLink() { return link; }
    public File getFile() { return file; }
    public EventType getEventType() { return eventType; }
}

class EventManager {
    private static final String URL = "jdbc:mysql://localhost:3306/EventsDB";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

      public void createEvent(Event event, List<Integer> invitedUserIds) {
        String sql = "INSERT INTO events (title, content, locationOrLink, duration, startEvent, link, file, eventType) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    
            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getContent());
            stmt.setString(3, event.getLocationOrLink());
            stmt.setInt(4, event.getDuration());
            stmt.setTimestamp(5, new Timestamp(event.getStart().getTime()));
            stmt.setString(6, event.getLink() != null ? event.getLink() : null);
    
            // Обробка файлу
            if (event.getFile() != null && event.getFile().exists()) {
                try (FileInputStream fis = new FileInputStream(event.getFile())) {
                    stmt.setBinaryStream(7, fis, (int) event.getFile().length());
                }
            } else {
                stmt.setNull(7, Types.BINARY);
            }
    
            stmt.setString(8, event.getEventType().name()); 
    
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int eventId = rs.getInt(1);
                    for (int userId : invitedUserIds) {
                        InvitationManager.createInvitation(eventId, userId);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    public File getEventFile(int eventId, String outputPath) {
        String sql = "SELECT file FROM events WHERE id = ?";
        
        File outputFile = new File(outputPath);
    
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setInt(1, eventId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    InputStream input = rs.getBinaryStream("file");
    
                    if (input != null) {
                        try (FileOutputStream output = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                        }
                        System.out.println("Файл успішно збережено: " + outputPath);
                        return outputFile;
                    } else {
                        System.out.println("Файл не знайдено для події ID: " + eventId);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id, title, content, locationOrLink, duration, startEvent, link, file, eventType FROM events ORDER BY startEvent";
    
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                File file = null;
                Blob blob = rs.getBlob("file");
    
                if (blob != null && blob.length() > 0) {
                    // генерує унікальне ім'я для кожного файлу
                    file = Files.createTempFile("event_" + rs.getInt("id"), ".bin").toFile();
                    
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(blob.getBytes(1, (int) blob.length()));
                    }
                }
    
                Event event = new Event(
                        rs.getString("title"),
                        rs.getTimestamp("startEvent"),
                        rs.getString("content"),
                        rs.getString("locationOrLink"),
                        null,
                        rs.getInt("duration"),
                        rs.getString("link"),
                        file,
                        Event.EventType.valueOf(rs.getString("eventType"))
                );
                events.add(event);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return events;
    }
}
