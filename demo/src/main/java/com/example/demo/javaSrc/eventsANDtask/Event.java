package com.example.demo.javaSrc.eventsANDtask;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Event {
	 String title, content, locationOrLink;
	 Task task;
	 int duration;
	  Date start;
    Event(String title, Date start, String content, String locationOrLink,Task task, int duration){
    	this.title = title;
        this.start = start;
        this.content = content;
        this.locationOrLink = locationOrLink;
        this.task = task;
        this.duration = duration;
    }
    public String getTitle() {
        return title;
    }

    public Date getStart() {
        return start;
    }

    public String getContent() {
        return content;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public Task getTask() {
        return task;
    }

    public int getDuration() {
        return duration;
    }

}
class EventManeger{
	private static final String URL = "jdbc:mysql://localhost:3306/EventsDB";
    private static final String USER = "root";
    private static final String PASSWORD = com.example.demo.javaSrc.Config.getPassword(); 

    public void createEvent(Event event, List<Integer> invitedUserIds) {
        String sql = "INSERT INTO events (title, content, locationOrLink, eventType, duration, startEvent) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getContent());
            stmt.setString(3, event.getLocationOrLink());
            stmt.setInt(4, event.getDuration());
            stmt.setTimestamp(5, new Timestamp(event.getStart().getTime()));
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int eventId = rs.getInt(1);
                    for (int userId : invitedUserIds) {
                    	InvitationManager.createInvitation(eventId, userId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY startEvent";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Event event = new Event(
                        rs.getString("title"),
                        rs.getTimestamp("startEvent"),
                        rs.getString("content"),
                        rs.getString("locationOrLink"),
                        null,
                        rs.getInt("duration")
                );
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
}