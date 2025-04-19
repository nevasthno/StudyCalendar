package com.example.demo.javaSrc.eventsANDtask;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import com.example.demo.javaSrc.people.*;

class EventManager {
    private static final String URL = "jdbc:mysql://localhost:3306/PeopleAndEvents";
    private static final String USER = "root";
    private static final String PASSWORD = com.example.demo.javaSrc.Config.getPassword();
    public static void createEventByUser(Event event, List<String> invitedUserEmails, People creator) {
        switch (creator.getRole()) {
            case TEACHER:
                createEvent(event, invitedUserEmails);
                break;

            case STUDENT:
                if (event.getEventType() != Event.EventType.PERSONAL) {
                    System.out.println("Students can only create PERSONAL events.");
                    return;
                }
                List<String> allowedInvitees = invitedUserEmails.stream()
                        .filter(userEmail -> PeopleManager.getUserRoleByEmail(userEmail) == People.Role.STUDENT)
                        .toList();

                createEvent(event, allowedInvitees);
                break;

            case PARENT:
                System.out.println("You can’t create events as a parent.");
                break;
        }
    }
    public static void createEvent(Event event, List<String> invitedUserEmails) {
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
                    for (String userEmail : invitedUserEmails) {
                        int userId = PeopleManager.getUserIDByEmail(userEmail); 
                        if (userId != -1) { 
                            InvitationManager.createInvitation(eventId, userId);
                        } else {
                            System.out.println("User with email " + userEmail + " not found.");
                        }
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
    
    public List<Event> getEventsForUser(int userId, boolean showPast) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.id, e.title, e.content, e.location_or_link, e.duration, e.start_event, e.link, e.file, e.event_type " +
                     "FROM events e " +
                     "JOIN invitations i ON e.id = i.event_id " +
                     "WHERE i.user_id = ? AND e.start_event " + (showPast ? "<" : ">") + " NOW() " +
                     "ORDER BY e.start_event";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    File file = null;
                    Blob blob = rs.getBlob("file");
                    if (blob != null && blob.length() > 0) {
                        file = Files.createTempFile("event_" + rs.getInt("id"), ".bin").toFile();
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(blob.getBytes(1, (int) blob.length()));
                        }
                    }

                    Event event = new Event(
                            rs.getString("title"),
                            rs.getTimestamp("start_event"),
                            rs.getString("content"),
                            rs.getString("location_or_link"),
                            null, // Task not included in this query; extend if needed
                            rs.getInt("duration"),
                            rs.getString("link"),
                            file,
                            Event.EventType.valueOf(rs.getString("event_type").toUpperCase())
                    );
                    events.add(event);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return events;
    }
    
    public List<Invitation> getInvitationsForUser(int userId, String statusFilter) {
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT i.id, i.event_id, i.user_id, i.created_at, uis.status " +
                     "FROM invitations i " +
                     "LEFT JOIN user_invitations_status uis ON i.id = uis.invitation_id " +
                     "WHERE i.user_id = ?" +
                     (statusFilter != null ? " AND uis.status = ?" : "") +
                     " ORDER BY i.created_at";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            if (statusFilter != null) {
                stmt.setString(2, statusFilter.toLowerCase());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invitation invitation = new Invitation(
                            rs.getInt("id"),
                            rs.getInt("event_id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("created_at"),
                            Invitation.Status.valueOf(rs.getString("status").toUpperCase())
                    );
                    invitations.add(invitation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invitations;
    }

    public String commentEvent(int eventId, int userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Comment content cannot be empty.";
        }
        String sql = "INSERT INTO comments (event_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return "Comment added successfully.";
            } else {
                return "Failed to add comment.";
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { 
                if (e.getMessage().contains("event_id")) {
                    return "Error: Event with ID " + eventId + " does not exist.";
                } else if (e.getMessage().contains("user_id")) {
                    return "Error: User with ID " + userId + " does not exist.";
                }
            }
            e.printStackTrace();
            return "Error adding comment: " + e.getMessage();
        }
    }

}

